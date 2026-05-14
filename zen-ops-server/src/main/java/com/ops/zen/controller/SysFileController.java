package com.ops.zen.controller;

import com.ops.zen.fs.FileService;
import com.ops.zen.fs.FsFactory;
import com.ops.zen.fs.TempFile;
import com.ops.zen.utils.*;
import com.ops.zen.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

/**
 * @author xyn
 * @date 2025/4/11 11:21
 * @description
 **/
@Deprecated
@RestController
@RequestMapping("/sys/file")
@Slf4j
public class SysFileController {

    private FileService fileService = FsFactory.tempFileService();


    /**
     * 上传文件返回文件ID
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public String upload(@RequestBody MultipartFile file, HttpServletResponse response) throws IOException {
        log.info("上传文件到文件服务并返回UUID");
        //file.getContentType() == application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
        //file.getContentType() application/x-zip-compressed
        String originalFilename = file.getOriginalFilename();
        String fileExt = FileUtils.parseExtByFileName(originalFilename);
        String fileId = fileService.write(file.getInputStream());
        TempFile tempFile = fileService.getFile(fileId);
        tempFile.setOriginName(originalFilename);
        tempFile.setName(originalFilename);
        tempFile.setExtension(fileExt);
        return fileId;
    }

    /**
     * 删除文件
     * @param id
     * @throws IOException
     */
    @RequestMapping(value = "/deleteFile", method = RequestMethod.GET)
    @ResponseBody
    public void deleteFile(@RequestParam String id) throws IOException {
        log.info("删除已经上传的文件,文件ID【{}】", id);
        fileService.delete(id);
    }

    /**
     * 下载文件
     * @param id
     * @param name
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/download", method = RequestMethod.GET)
    @ResponseBody
    public void download(@RequestParam String id, String name, HttpServletResponse response) throws IOException {
        log.info("根据文件ID下载文件,文件ID【{}】", id);
        TempFile file = fileService.getFile(id);
        String fileNamei = StringUtils.isNotBlank(file.getName()) ? file.getName() : file.getOriginName();
        String fileName = StringUtils.isNotBlank(name) ? name : StringUtils.isNotBlank(fileNamei) ? fileNamei : DateTimeUtils.format(LocalDateTime.now(), "yyyy年MM月dd日HH时mm分下载");
        UpDownLoader.downLoad(fileName, file.getBody(), response);
    }

    /*
    将文件上传到指定服务器模
 */
    @RequestMapping(value = "/uploadToDir", method = RequestMethod.POST)
    @ResponseBody
    public String uploadToDir(@RequestBody MultipartFile file, @RequestParam String uploadDir, HttpServletResponse response) throws IOException {
        String originalFilename = file.getOriginalFilename();
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File f = new File(uploadDir, originalFilename);
        try (OutputStream os = new FileOutputStream(f)) {
            IOUtils.write(file.getInputStream(), os);
        }
        return "1";
    }

}
