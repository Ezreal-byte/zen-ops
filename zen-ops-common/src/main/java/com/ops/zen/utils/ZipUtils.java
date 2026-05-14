package com.ops.zen.utils;


import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class ZipUtils {
    private static final int CACHE_SIZE = 1024;

    public static void unZip(String targetDir, String sourceZip) {
        unZip(targetDir, sourceZip, null);
    }

    public static void unZip(String targetDir, String sourceZip, String charset) {
        File target = new File(targetDir);
        if (!target.exists()) {
            target.mkdirs();
        }

        File zip = new File(sourceZip);
        unZip(target, zip, charset);
    }

    public static void unZip(File target, URL source) {
        unZip(target, source, null);
    }

    public static void unZip(File target, URL source, String charset) {
        try {
            unZip(target, source.openStream(), charset);
        } catch (IOException arg2) {
            throw new RuntimeException(arg2);
        }
    }

    public static void unZip(File target, File source) {
        unZip(target, source, null);
    }

    public static void unZip(File target, File source, String charset) {
        try {
            unZip((File) target, (InputStream) (new FileInputStream(source)), charset);
        } catch (FileNotFoundException arg2) {
            throw new RuntimeException(arg2);
        }
    }

    public static void unZip(File target, InputStream source) {
        unZip(target, source, null);
    }

    /**
     * @param target
     * @param source
     * @param charset 压缩包中的路径有中文时需要charset应为GBK
     */
    public static void unZip(File target, InputStream source, String charset) {
        try {
            String e = target.getAbsolutePath();
            e = FileUtils.uniformPath(e);
            e = FileUtils.appendFileSeparator(e);
            ZipInputStream zis = null;
            if (charset != null) {
                zis = new ZipInputStream(new BufferedInputStream(source), Charset.forName(charset));
            } else {
                zis = new ZipInputStream(new BufferedInputStream(source));
            }
            ZipEntry entry = null;

            while (true) {
                while ((entry = zis.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    entryName = FileUtils.uniformPath(entryName);
                    File f = null;
                    if (entry.isDirectory()) {//entry是目录的情况 Returns true if this is a directory entry. A directory entry is defined to be one whose name ends with a '/'
                        f = new File(e + entryName);
                        f.mkdirs();
                    } else {
                        f = new File(e + entryName);
                        File parent = f.getParentFile();
                        if (parent != null && !parent.exists()) {
                            parent.mkdirs();
                        }

                        f.createNewFile();
                        FileOutputStream fos = new FileOutputStream(f);
                        short buffer = 2048;
                        BufferedOutputStream bos = new BufferedOutputStream(fos, buffer);
                        byte[] data = new byte[buffer];
                        boolean count = false;

                        int count1;
                        while ((count1 = zis.read(data, 0, buffer)) != -1) {
                            bos.write(data, 0, count1);
                        }

                        bos.flush();
                        bos.close();
                    }
                }

                zis.close();
                return;
            }
        } catch (Exception arg12) {
            throw new RuntimeException(arg12);
        }
    }

    public static void zip(File srcDir, File dest) throws IOException {
        FileOutputStream out = new FileOutputStream(dest);
        zip(srcDir, (OutputStream) out);
    }

    public static void zip(File srcDir, OutputStream out) throws IOException {
        if (!srcDir.isDirectory()) {
            throw new RuntimeException("srcDir must be directory");
        } else {
            BufferedOutputStream bos = new BufferedOutputStream(out);// 缓冲输出流，不一定需要每次write都调用系统的write，降低用户态到内核态的转换
            ZipOutputStream zos = new ZipOutputStream(bos);// 压缩的中文没问题，不需要指定Charset
            String basePath = srcDir.getPath();

            try {
                zipFile(srcDir, basePath, zos);
            } finally {
                zos.closeEntry();
                zos.close();
                bos.close();
                out.close();
            }

        }
    }

    private static void zipFile(File parentFile, String basePath, ZipOutputStream zos) throws IOException {
        File[] files = new File[0];
        if (parentFile.isDirectory()) {
            files = parentFile.listFiles();
        } else {
            files = new File[]{parentFile};
        }

        byte[] cache = new byte[1024];
        File[] arr$ = files;
        int len$ = files.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            File file = arr$[i$];
            String pathName;
            if (file.isDirectory()) {//目录
                pathName = file.getPath().substring(basePath.length() + 1) + "/";
                pathName = pathName.replaceAll("\\\\", "/"); //Returns true if this is a directory entry. A directory entry is defined to be one whose name ends with a '/'
                zos.putNextEntry(new ZipEntry(pathName));
                zipFile(file, basePath, zos);
            } else {//文件压缩
                pathName = file.getPath().substring(basePath.length() + 1);
                FileInputStream is = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(is);
                zos.putNextEntry(new ZipEntry(pathName));//Begins writing a new ZIP file entry and positions the stream to the start of the entry data
                //开始写一个新的zip文件entry，将流的位置定位到这个entry数据的开始位置，后面调用zos.write写entry对应的数据
                boolean nRead = false;

                int arg12;
                while ((arg12 = bis.read(cache, 0, 1024)) != -1) {
                    zos.write(cache, 0, arg12);
                }

                bis.close();
                is.close();
            }
        }

    }
}
