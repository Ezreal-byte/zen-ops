package com.ops.zen.fs.lfs;


import com.ops.zen.fs.TempFile;
import com.google.common.collect.Maps;
import com.ops.zen.fs.FileService;
import com.ops.zen.utils.Assert;
import com.ops.zen.utils.DateTimeUtils;
import com.ops.zen.utils.IOUtils;
import org.slf4j.Logger;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Author xiaoyingnan
 * @Date 2020/7/14 10:31
 * @Description
 */
public class LocalTempFileService implements FileService {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(LocalTempFileService.class);

    /**
     * 内存中持有所有在本JVM创建的临时文件
     */
    private Map<String, TempIpfFile> tempFilesMap = Maps.newConcurrentMap();

    private volatile static LocalTempFileService _inst;

    public static LocalTempFileService inst() {
        if (_inst == null) {
            synchronized (LocalTempFileService.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new LocalTempFileService();
            }
        }
        return _inst;
    }

    private LocalTempFileService() {
        //定期删除超时的临时文件
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Iterator<TempIpfFile> itFiles = tempFilesMap.values().iterator();
                while (itFiles.hasNext()) {
                    TempIpfFile file = itFiles.next();
                    if (file.isTimeout()) {
                        String id = file.innerFile.getId();
                        logger.info("删除临时文件{}", id);
                        delete(id);
                        itFiles.remove();
                    }
                }
            }
        }, 1, 5, TimeUnit.MINUTES);
    }


    @Override
    public String write(TempFile file) throws IOException {
        String id = write(file.getBody());
        file.setId(id);
        putTempFile(id, new TempIpfFile(file));
        IOUtils.close(file.getBody());
        file.setBody(null);

        return id;
    }

    @Override
    public String write(InputStream inputStream) throws IOException {
        //写临时目录
        String uid = createFileUID();
        OutputStream os = getOutputStream(uid);
        IOUtils.write(inputStream, os);
        IOUtils.close(os);
        return uid;
    }

    @Override
    public OutputStream getOutputStream(String uid) throws IOException {
        String rootPath = getTempPath();
        File f = new File(rootPath, uid);
        f.createNewFile();
        putTempFile(uid, new TempIpfFile(new TempFile(uid)));//持有文件ID
        OutputStream os = new FileOutputStream(f);
        return os;
    }


    @Override
    public InputStream read(String id) throws FileNotFoundException {
        File f = getDiskFile(id);
        return new FileInputStream(f);
    }

    @Override
    public byte[] readByte(String id) throws IOException {
        return IOUtils.toByteArray(read(id));
    }

    private File getDiskFile(String id) {
        String rootPath = getTempPath();
        File f = new File(rootPath, id);
        Assert.isTrue(f.exists(), String.format("文件【%s】不存在", f.getPath()));
        return f;
    }

    @Override
    public TempFile getFile(String id) throws FileNotFoundException {
        TempFile innerFile = tempFilesMap.get(id).innerFile;
        innerFile.setBody(read(innerFile.getId()));
        innerFile.setDiskFile(getDiskFile(innerFile.getId()));
        return innerFile;
    }

    @Override
    public void delete(String id) {
        String rootPath = getTempPath();
        File f = new File(rootPath, id);
        if (f.exists()) {
            f.delete();
        } else {
            logger.warn("文件【{}】不存在，无法删除", f.getName());
        }
        tempFilesMap.remove(id);
    }

    public String createFileUID() {
        return UUID.randomUUID().toString();
    }

    private void putTempFile(String uid, TempIpfFile f) {
        tempFilesMap.put(uid, f);
    }

    private String getTempPath() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * 临时文件
     */
    static class TempIpfFile {

        private LocalDateTime createTime = null;

        public TempFile innerFile;

        public TempIpfFile(TempFile innerFile) {
            this.innerFile = innerFile;
            createTime = LocalDateTime.now();
        }

        /**
         * 超过一小时，删掉
         *
         * @return
         */
        public boolean isTimeout() {
            //超过一小时删除
            return DateTimeUtils.millisBetween(createTime, LocalDateTime.now()) > 60 * 60 * 1000;
        }
    }
}
