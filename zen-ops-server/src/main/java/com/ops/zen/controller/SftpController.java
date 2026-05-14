package com.ops.zen.controller;

import com.ops.zen.controller.ws.ssh.fac.*;
import com.ops.zen.controller.ws.ssh.fac.*;
import com.ops.zen.controller.ws.ssh.sftp.Sftps;
import com.ops.zen.entity.request.SftpUploadFileRequest;
import com.ops.zen.entity.response.SftpInitVo;
import com.ops.zen.fs.FileService;
import com.ops.zen.fs.FsFactory;
import com.ops.zen.fs.TempFile;
import com.ops.zen.utils.*;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.ops.zen.support.JsonResult;
import com.ops.zen.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 1.创建文件，用上传方式做，需要判断文件是否已存在。
 * 2.文本文件编辑，编辑以后使用上传方式来变更修改。发出修改事件（供记录日志等扩展使用）
 * 3.改名 and so on
 *
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
@Controller
@RequestMapping("/commons/sftp")
public class SftpController {

    private final String HOME = "/home";

    private static Logger logger = LoggerFactory.getLogger(SftpController.class);

    private FileService fileService = FsFactory.tempFileService();

    private Sftps sftps = Sftps.inst();

    /**
     * 初始化连接
     *
     * @param body
     * @return
     * @throws IOException
     * @throws SftpException
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @ResponseBody
    public SftpInitVo init(@RequestBody String body) throws SftpException {
        SshConnCfg sshConnCfg = SShConnCfgFactory.inst().create(body);
        String uid = UUIDUtils.uuidWithoutHorizonBar();
        ChannelSftp channelSftp = sftps.getChannelSftp(sshConnCfg, uid);
        String path = sshConnCfg.getInitPath();
        if (StringUtils.isEmpty(path)) {
            path = HOME;
        }
        List<ChannelSftp.LsEntry> entries = null;
        try {
            entries = ls(uid, path, false);
        } catch (SftpException e) {
            // 处理没有权限的情况
            if (e.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
                path = HOME;
                entries = ls(uid, path, false);
            } else if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) { // 没有对应的目录，进入临时目录
                path = "/tmp";
                entries = ls(uid, path, false);
            } else {
                sftps.closeAndRemove(uid);
                throw e;
            }
        }
//        channelSftp.ls(path, new ChannelSftp.LsEntrySelector() {
//            @Override
//            public int select(ChannelSftp.LsEntry entry) {
//                entries.add(entry);
//                return ChannelSftp.LsEntrySelector.CONTINUE;
//            }
//        });
        return new SftpInitVo(uid, entries, path);
    }

    /**
     * 获取ssh连接信息
     * shell也会使用
     * 删除敏感信息
     *
     * @param body
     * @return
     * @throws IOException
     * @throws SftpException
     */
    @RequestMapping(value = "/info", method = RequestMethod.POST)
    @ResponseBody
    public DefaultSshConnCfg info(@RequestBody String body) throws IOException, SftpException {
        SshConnCfg sshConnCfg = SShConnCfgFactory.inst().create(body);
        if (sshConnCfg instanceof PrvKeySshConnCfg) {
            ((PrvKeySshConnCfg) sshConnCfg).setPassphrase(null);
            ((PrvKeySshConnCfg) sshConnCfg).setPrvKey(null);
        } else if (sshConnCfg instanceof PasswordSshConnCfg) {
            ((PasswordSshConnCfg) sshConnCfg).setPassword(null);
            ((PasswordSshConnCfg) sshConnCfg).setUsername(null);
        }
        return (DefaultSshConnCfg) sshConnCfg;
    }

    /**
     * ls命令
     *
     * @param id
     * @param path
     * @return
     * @throws IOException
     * @throws SftpException
     */
    @RequestMapping(value = "/ls/{id}", method = RequestMethod.GET)
    @ResponseBody
    public List<ChannelSftp.LsEntry> ls(@PathVariable String id, @RequestParam String path, @RequestParam boolean showHiddenFiles) throws SftpException {
        ChannelSftp channelSftp = sftps.getChannelSftp(id);
        sftps.updateDtLastVisit(id);
        List<ChannelSftp.LsEntry> entries = sftps.getLsEntries(channelSftp, path, showHiddenFiles);
        return entries;
    }


    /**
     * 以文本方式查看文件内容
     *
     * @param id
     * @param path
     * @return
     * @throws IOException
     * @throws SftpException
     */
    @RequestMapping(value = "/file/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String file(@PathVariable String id, @RequestParam String path) throws IOException, SftpException {
        ChannelSftp channelSftp = sftps.getChannelSftp(id);
        InputStream inputStream = channelSftp.get(path, new SftpProgressMonitor() {
            // http://epaul.github.io/jsch-documentation/javadoc/com/jcraft/jsch/SftpProgressMonitor.html
            @Override
            public void init(int op, String src, String dest, long max) {

            }

            // 传输过程中返回false会中断传输，可能导致下载和上传的文件不完整
            @Override
            public boolean count(long count) {
                // true if the transfer should go on, false if the transfer should be cancelled.
                sftps.updateDtLastVisit(id);
                return true;
            }

            @Override
            public void end() {

            }
        });
        String s = IOUtils.toString(inputStream);
        inputStream.close();
        return s;
    }

    /**
     * 判断目录或文件是否存在
     *
     * @param id
     * @param path
     * @return
     * @throws SftpException
     */
    @RequestMapping(value = "/exists/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Boolean exists(@PathVariable String id, @RequestParam String path) throws SftpException {
        ChannelSftp channelSftp = sftps.getChannelSftp(id);
        return sftps.exists(channelSftp, path);
    }

    @RequestMapping(value = "/chmod/{id}", method = RequestMethod.POST)
    @ResponseBody
    public void chmod(@PathVariable String id, @RequestParam String path, @RequestParam String permissions) throws SftpException {
        ChannelSftp channelSftp = sftps.getChannelSftp(id);
        channelSftp.chmod(Integer.valueOf(permissions), path);
    }

    @RequestMapping(value = "/rm/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable String id, @RequestParam String path, @RequestParam boolean dir) throws SftpException {
        ChannelSftp channelSftp = sftps.getChannelSftp(id);
        if (dir) {
            channelSftp.rmdir(path);
        } else {
            channelSftp.rm(path);
        }
    }

    @RequestMapping(value = "/mkdir/{id}", method = RequestMethod.POST)
    @ResponseBody
    public void mkdir(@PathVariable String id, @RequestParam String path) throws SftpException {
        ChannelSftp channelSftp = sftps.getChannelSftp(id);
        channelSftp.mkdir(path);
    }

    @RequestMapping(value = "/createFile/{id}", method = RequestMethod.POST)
    @ResponseBody
    public void createFile(@PathVariable String id, @RequestParam String path) throws SftpException {
        ChannelSftp channelSftp = sftps.getChannelSftp(id);
        if (sftps.stat(channelSftp, path) == null) {
            // 使用文件上传的方式创建文件，上传一个空文件
            channelSftp.put(new ByteArrayInputStream(new byte[0]), path, ChannelSftp.OVERWRITE);
        } else {
            throw new RuntimeException("文件" + path + "已存在");
        }

    }

    @RequestMapping(value = "/modifyFile/{id}", method = RequestMethod.POST)
    @ResponseBody
    public void modifyFile(@RequestBody Map<String, String> body, @PathVariable String id, @RequestParam String path) throws SftpException {
        ChannelSftp channelSftp = sftps.getChannelSftp(id);
        // 使用文件上传的方式修改文件
        String fileContent = body.get("fileContent");
        channelSftp.put(new ByteArrayInputStream(fileContent.getBytes()), path, ChannelSftp.OVERWRITE);
    }

    @RequestMapping(value = "/rename/{id}", method = RequestMethod.POST)
    @ResponseBody
    public void modifyFile(@PathVariable String id, @RequestParam String path, @RequestParam String pathNew) throws SftpException {
        ChannelSftp channelSftp = sftps.getChannelSftp(id);
        channelSftp.rename(path, pathNew);
    }

    /**
     * 查查看文件属性
     *
     * @param id
     * @param path
     * @return
     * @throws SftpException
     */
    @RequestMapping(value = "/stat/{id}", method = RequestMethod.GET)
    @ResponseBody
    public SftpATTRS stat(@PathVariable String id, @RequestParam String path) throws SftpException {
        ChannelSftp channelSftp = sftps.getChannelSftp(id);
        return sftps.stat(channelSftp, path);
    }

    /**
     * 下载文件
     *
     * @param id
     * @param path
     * @return
     * @throws IOException
     * @throws SftpException
     */
    @RequestMapping(value = "/download/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String download(@PathVariable String id, @RequestParam String path) throws IOException, SftpException {
        ChannelSftp channelSftp = sftps.getChannelSftp(id);
        InputStream inputStream = channelSftp.get(path, new SftpProgressMonitor() {
            @Override
            public void init(int op, String src, String dest, long max) {

            }

            /**
             * 经测试一般是一个缓冲区的大小时调用一次
             * count = 4083，4096
             * @param count
             * @return
             */
            @Override
            public boolean count(long count) {
                sftps.updateDtLastVisit(id);
                return true;
            }

            @Override
            public void end() {

            }
        });
        TempFile pfile = new TempFile();
        pfile.setBody(inputStream);
        pfile.setName(path.substring(path.lastIndexOf("/") + 1, path.length()));
        String fileId = fileService.write(pfile);
        return fileId;
    }

    @RequestMapping(value = "/open_download", method = RequestMethod.GET)
    @ResponseBody
    public void download(@RequestParam String id, String name, HttpServletResponse response) throws IOException {
        logger.info("根据文件ID下载文件,文件ID【{}】", id);
        TempFile file = fileService.getFile(id);
        String fileName = StringUtils.isNotBlank(name) ? name : StringUtils.isNotBlank(file.getName()) ? file.getName() : DateTimeUtils.format(LocalDateTime.now(), "yyyy年MM月dd日HH时mm分下载");
        UpDownLoader.downLoad(fileName, file.getBody(), response);
    }


    /**
     * 上传文件
     *
     * @param file
     * @param id
     * @param path
     * @param response
     * @return
     * @throws IOException
     * @throws SftpException
     */
    @RequestMapping(value = "/upload/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Object upload(@RequestBody MultipartFile file, @PathVariable String id, @RequestParam String path, HttpServletResponse response) throws IOException, SftpException {
        InputStream inputStream = file.getInputStream();
        ChannelSftp channelSftp = sftps.getChannelSftp(id);
        String originalFilename = file.getOriginalFilename();
        try {
            channelSftp.put(inputStream, path + "/" + originalFilename, new SftpProgressMonitor() {
                @Override
                public void init(int op, String src, String dest, long max) {

                }

                @Override
                public boolean count(long count) {
                    sftps.updateDtLastVisit(id);
                    return true;
                }

                @Override
                public void end() {

                }
            }, ChannelSftp.OVERWRITE);// OVERWRITE，如果中间中断可能导致上传的文件不完整
            return "完成";
        } catch (SftpException e) {
            return new JsonResult<>(-1, "上传失败: " + e.getMessage());
        } finally {
            try { inputStream.close(); } catch (IOException e) { /* ignore */ }
        }
    }

    @RequestMapping(value = "/disconnect/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String disconnect(@PathVariable String id) {
        ChannelSftp channelSftp = sftps.getChannelSftp(id);
        Assert.notNull(channelSftp, "%s 的sftp连接不存在", id);
        sftps.closeAndRemove(id);
        return null;
    }

    @RequestMapping(value = "/upload_folder/{id}", method = RequestMethod.POST)
    @ResponseBody
    public String uploadFolder(@RequestBody List<SftpUploadFileRequest> list,
                               @PathVariable String id,
                               @RequestParam String path, HttpServletResponse response) throws IOException, SftpException {
        for (SftpUploadFileRequest f : list) {
            TempFile ipfFile = fileService.getFile(f.getFileId());
            f.setFile(ipfFile.getDiskFile());
        }
        sftps.uploadFolder(id, path, list);
        return "完成";
    }

}
