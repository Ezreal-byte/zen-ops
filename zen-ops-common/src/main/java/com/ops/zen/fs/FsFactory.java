package com.ops.zen.fs;


import com.ops.zen.fs.lfs.LocalTempFileService;

/**
 * @Author xiaoyingnan
 * @Date 2020/7/14 11:52
 * @Description
 */
public class FsFactory {

    public static LocalTempFileService tempFileService() {
        return LocalTempFileService.inst();
    }
}
