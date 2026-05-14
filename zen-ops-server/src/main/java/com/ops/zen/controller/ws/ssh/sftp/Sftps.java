package com.ops.zen.controller.ws.ssh.sftp;

import com.ops.zen.controller.ws.ssh.fac.SshConnCfg;
import com.ops.zen.controller.ws.ssh.jsch.ChannelFactory;
import com.ops.zen.entity.request.SftpUploadFileRequest;
import com.ops.zen.utils.Assert;
import com.ops.zen.utils.DateTimeUtils;
import com.ops.zen.utils.StringUtils;
import com.jcraft.jsch.*;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ChannelSftp帮助类
 *
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class Sftps {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(Sftps.class);

    private Map<String, ChannelSftpWrapper> ftps = new ConcurrentHashMap<>();

    private volatile static Sftps _inst;

    private Sftps() {
        // 两分钟运行一次清理任务
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::clearChannelScheduled, 60, 2 * 60, TimeUnit.SECONDS);
    }

    public static Sftps inst() {
        if (_inst == null) {
            synchronized (Sftps.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new Sftps();
            }
        }
        return _inst;
    }


    public void updateDtLastVisit(String id) {
        try {
            LocalDateTime now = LocalDateTime.now();
            if (logger.isTraceEnabled()) {
                logger.trace("更新channel {}最后使用时间{}", id, now);
            }
            ftps.get(id).setDtLastVisit(now);
        } catch (Exception e) {
        }
    }

    /**
     * 如果uid下存在Channel则直接返回该channel，如果不存在，尝试使用cfg创建channel
     *
     * @param cfg
     * @param uid
     * @return
     */
    public ChannelSftp getChannelSftp(SshConnCfg cfg, String uid) {
        ChannelSftpWrapper channelSftpWrapper = ftps.get(uid);
        if (channelSftpWrapper != null) {
            channelSftpWrapper.setDtLastVisit(LocalDateTime.now());
            return channelSftpWrapper.getFtp();
        }
        Channel channel = ChannelFactory.inst().createChannel(cfg);
        ChannelSftp ftp = (ChannelSftp) channel;
        ftps.put(uid, new ChannelSftpWrapper(ftp));
        return ftp;
    }

    /**
     * 获取缓存的ChannelSftp，获取不到抛出异常
     *
     * @param uid
     * @return
     */
    public ChannelSftp getChannelSftp(String uid) {
        ChannelSftpWrapper channelSftpWrapper = ftps.get(uid);
        Assert.notNull(channelSftpWrapper, "通道不存在，可能已被清理，请重新连接");
        return channelSftpWrapper.getFtp();
    }

    public void closeAndRemove(String uid) {
        ChannelSftpWrapper remove = ftps.remove(uid);
        try {
            if (remove != null) {
                remove.getFtp().disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 定时清理
     */
    private void clearChannelScheduled() {
        Iterator<Map.Entry<String, ChannelSftpWrapper>> iterator = ftps.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ChannelSftpWrapper> entry = iterator.next();
            ChannelSftpWrapper w = entry.getValue();
            if (DateTimeUtils.millisBetween(w.getDtLastVisit(), LocalDateTime.now()) > 30 * 60 * 1000) {// 超过30分钟没有使用则关闭
                iterator.remove();
                try {
                    logger.info("清理SFTP通道{}，最后一次访问时间{}", entry.getKey(), DateTimeUtils.format(w.getDtLastVisit(), "yyyy-MM-dd HH:mm:ss"));
                    w.getFtp().disconnect();
                } catch (Exception ex) {
                }
            }
        }
    }

    /*
    begin 工具方法
     */
    public SftpATTRS stat(ChannelSftp channelSftp, String path) throws SftpException {
        try {
            SftpATTRS lstat = channelSftp.lstat(path);
            return lstat;
        } catch (
                SftpException e) {
            // TODO
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                //if ("No such file".equals(e.getMessage())) {
                return null;
            } else {
                throw e;
            }
        }
    }

    public Boolean exists(ChannelSftp channelSftp, String path) throws SftpException {
        try {
            channelSftp.lstat(path);
            return true;
        } catch (SftpException e) {
            // TODO
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                //if ("No such file".equals(e.getMessage())) {
                return false;
            } else {
                throw e;
            }
        }
    }

    public List<ChannelSftp.LsEntry> getLsEntries(ChannelSftp channelSftp, String path, boolean showHiddenFiles) throws SftpException {
        List<ChannelSftp.LsEntry> entries = new ArrayList<>();
        channelSftp.ls(path, new ChannelSftp.LsEntrySelector() {
            @Override
            public int select(ChannelSftp.LsEntry entry) {
                if (showHiddenFiles) {
                    entries.add(entry);
                } else {
                    if (entry.getFilename().equals("..")) {
                        entries.add(entry);
                    } else if (!entry.getFilename().startsWith(".")) {
                        entries.add(entry);
                    }
                }
                return ChannelSftp.LsEntrySelector.CONTINUE;
            }
        });
        return entries;
    }

    /**
     *
     * @param sftpId sftp连接ID
     * @param path 要上传到的目录 path
     * @param list 文件集合
     */
    public void uploadFolder(String sftpId, String path, List<SftpUploadFileRequest> list) throws IOException, SftpException {
        ChannelSftp channelSftp = getChannelSftp(sftpId);
        for (SftpUploadFileRequest ipfUploadFile : list) {
            String fileId = ipfUploadFile.getFileId();
            String p = path + "/" + ipfUploadFile.getWebkitRelativePath();
            String dirPath = p.substring(0, p.lastIndexOf("/"));
            List<String> dirPathArr = Arrays.asList(dirPath.split("/")).stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
            for (int i = 0; i < dirPathArr.size(); i++) {
                mkdir(channelSftp, "/" + StringUtils.concate(dirPathArr.subList(0, i + 1), "/"));
            }
            mkdir(channelSftp, dirPath);
            try (InputStream is = new FileInputStream(ipfUploadFile.getFile())) {
                channelSftp.put(is, p, new SftpProgressMonitor() {
                    @Override
                    public void init(int op, String src, String dest, long max) {
                    }
                    @Override
                    public boolean count(long count) {
                        updateDtLastVisit(sftpId);
                        return true;
                    }
                    @Override
                    public void end() {
                    }
                }, ChannelSftp.OVERWRITE);// OVERWRITE，如果中间中断可能导致上传的文件不完整
            }
        }
    }

    /**
     * 创建目录  不允许越级创建目录  父目录必须存在
     * @param channelSftp
     * @param dirPath
     * @throws SftpException
     */
    public void mkdir(ChannelSftp channelSftp, String dirPath) throws SftpException {
        try {
            SftpATTRS stat = channelSftp.stat(dirPath);
            if (!stat.isDir()) {
                channelSftp.mkdir(dirPath);
            }
        } catch (SftpException e) {
            channelSftp.mkdir(dirPath);
        }
    }

    public void deleteFolder(ChannelSftp sftp, String folderPath) throws SftpException {
        if (!exists(sftp, folderPath)) {
            return;
        }
        sftp.cd(folderPath);
        for (Object obj : sftp.ls(".")) {
            ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) obj;
            if (!entry.getFilename().equals(".") && !entry.getFilename().equals("..")) {
                if (entry.getAttrs().isDir()) {
                    deleteFolder(sftp, entry.getFilename());
                } else {
                    sftp.rm(entry.getFilename());
                }
            }
        }
        sftp.cd("..");
        sftp.rmdir(folderPath);
    }

}
