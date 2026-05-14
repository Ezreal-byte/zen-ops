package com.ops.zen.utils;



import java.io.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class FileUtils {

    public static final int BUFFER_SIZE = 4096;

    public static boolean delete(File file) {
        if (file == null) {
            return false;
        }
        return file.isFile() ? deleteFile(file) : (file.isDirectory() ? deleteDirectory(file) : false);
    }

    static boolean deleteFile(File file) {
        return file.delete();
    }

    static boolean deleteDirectory(File file) {
        boolean s = true;
        File[] arr$ = file.listFiles();
        int len$ = arr$.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            File f = arr$[i$];
            if (f.isFile()) {
                s = deleteFile(f);
            } else if (f.isDirectory()) {
                s = deleteDirectory(f);
            }

            if (!s) {
                break;
            }
        }

        if (s) {
            s = file.delete();
        }

        return s;
    }

    /**
     * 将路径中转义的\\转为/
     *
     * @param originPath
     * @return
     */
    public static String uniformPath(String originPath) {
        return originPath.replaceAll("\\\\", "/");
    }

    /**
     * 文件路径后追加/,如果路径结束字符为/则返回原始path
     *
     * @param path
     * @return
     */
    public static String appendFileSeparator(String path) {
        return path.lastIndexOf("/") == path.length() - 1 ? path : path + "/";
    }

    public static int copy(File in, File out) throws IOException {
        return copy((InputStream) (new BufferedInputStream(new FileInputStream(in))),
                (OutputStream) (new BufferedOutputStream(new FileOutputStream(out))));
    }

    public static int copy(InputStream in, OutputStream out) throws IOException {
        try {
            int byteCount = 0;
            byte[] buffer = new byte[4096];

            int bytesRead1;
            for (boolean bytesRead = true; (bytesRead1 = in.read(buffer)) != -1; byteCount += bytesRead1) {
                out.write(buffer, 0, bytesRead1);
            }

            out.flush();
            int arg4 = byteCount;
            return arg4;
        } finally {
            in.close();
            out.close();
        }
    }

    public static void copyFile(File source, File dest) throws IOException {
        Assert.notNull(source, "文件来源不允许为空");
        Assert.notNull(dest, "目标文件不允许为空");
        if (source.exists()) {
            if (source.isFile() && !dest.exists()) {
                dest.createNewFile();
                copyFile(source, dest);
            } else if (source.isDirectory() && !dest.exists()) {
                boolean mkdirs = dest.mkdirs();
                if (!mkdirs) {
                    throw new RuntimeException("目录无法创建" + dest.getPath()); // 文件创建在windows和linux下表现可能不同
                    // 相对路径的使用上不同，windows上成功linux失败时不抛出异常会导致stack overflow，栈溢出
                }
                copyFile(source, dest);
            } else if (source.isFile() && dest.isFile()) {
                copy(source, dest);
            } else if (source.isFile() && dest.isDirectory()) {
                copyFileToDirectory(source, dest);
            } else if (source.isDirectory() && dest.isDirectory()) {
                File[] arr$ = source.listFiles();
                int len$ = arr$.length;

                for (int i$ = 0; i$ < len$; ++i$) {
                    File file = arr$[i$];
                    if (file.isDirectory()) {
                        copyDirectoryToDirectory(file, dest);
                    } else if (file.isFile()) {
                        copyFileToDirectory(file, dest);
                    }
                }
            }

        }
    }

    static void copyFileToDirectory(File source, File dest) throws IOException {
        Assert.isTrue(source.isFile());
        Assert.isTrue(dest.isDirectory());
        File newFile = new File(dest, source.getName());
        copyFile(source, newFile);
    }

    static void copyDirectoryToDirectory(File source, File dest) throws IOException {
        Assert.isTrue(source.isDirectory());
        Assert.isTrue(dest.isDirectory());
        File newDest = new File(dest, source.getName());
        if (!newDest.exists()) {
            newDest.mkdirs();
        }

        copyFile(source, newDest);
    }

    public static File createNewFile(File file, boolean mkParentDirs) throws IOException {
        if (mkParentDirs) {
            (new File(file.getParent())).mkdirs();
            file.createNewFile();
        } else {
            file.createNewFile();
        }
        return file;
    }

    /**
     * @return
     */
    public static File makeTempDir(String prefix) {
        String tmpDir = System.getProperty("java.io.tmpdir");
        tmpDir = FileUtils.uniformPath(tmpDir);
        tmpDir = FileUtils.appendFileSeparator(tmpDir);
        String rootDir = tmpDir + (StringUtils.isNotEmpty(prefix) ? String.format("%s-", prefix) : "") + DateTimeUtils.format(LocalDateTime.now(), "yyyyMMddHHmmss");

        File file = new File(rootDir);
        file.mkdirs();
        return file;
    }


    /**
     * 使用完毕择机删除
     *
     * @return
     */
    public static File createTempFile() throws IOException {
        return createTempFile(true);
    }

    /**
     * 使用完毕择机删除
     *
     * @return
     */
    public static File createTempFile(boolean isCreate) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        tmpDir = FileUtils.uniformPath(tmpDir);
        tmpDir = FileUtils.appendFileSeparator(tmpDir);
        File file = new File(tmpDir, UUID.randomUUID().toString());
        if (isCreate) {
            file.createNewFile();
        }
        return file;
    }


    /**
     * 使用完毕择机删除
     *
     * @return
     */
    public static File createTempDirectory() throws IOException {
        return createTempDirectory(true);
    }

    public static File createTempDirectory(boolean isCreate) throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        tmpDir = FileUtils.uniformPath(tmpDir);
        tmpDir = FileUtils.appendFileSeparator(tmpDir);
        File file = new File(tmpDir, UUID.randomUUID().toString());
        if (isCreate) {
            boolean mkdirs = file.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException(file.getPath() + "临时目录创建失败");
            }
        }
        return file;
    }

    /**
     * 在consumer中使用完毕以后删除
     *
     * @return
     */
    public static void createTempFile(Consumer<File> consumer) throws IOException {
        Assert.notNull(consumer, "consumer不能为空");
        File file = createTempFile();
        consumer.accept(file);
        file.delete();
    }

    /**
     * @param dir
     * @param fileName
     * @return
     * @throws IOException
     */
    public static OutputStream createOutputStream(File dir, String fileName) throws IOException {
        File f = new File(dir, fileName);
        f.createNewFile();
        return new FileOutputStream(f);
    }

    /**
     * 删除dir下超过limit数量的旧文件（包括文件夹），旧文件通过lastModified来判断
     *
     * @param dir
     * @param limit
     * @param deleteChildDir 如果是文件夹是否删除
     */
    public static void deleteOverLimit(File dir, int limit, boolean deleteChildDir) {
        if (!dir.isDirectory()) throw new RuntimeException(dir.getPath() + "不是文件夹");
        // 删除超过数量的历史配置文件，保留limit条
        File[] filesArray = dir.listFiles();
        if (filesArray == null || filesArray.length <= limit) {
            return;
        }
        List<File> filesList = Arrays.asList(filesArray);
        // 将文件按列表单修改时间升序排列，索引越大时间值越靠近当前时间
        filesList.sort((f1, f2) -> (int) (f1.lastModified() - f2.lastModified()));
        int delNum = filesList.size() - limit;
        if (delNum > 0) {
            // 删除旧文件
            List<File> fileListDeleted = filesList.subList(0, delNum);
            fileListDeleted.forEach(f -> {
                if (deleteChildDir) {
                    FileUtils.delete(f);
                } else {
                    if (f.isFile()) {
                        FileUtils.delete(f);
                    }
                }
            });
        }
    }

    /**
     * 解析文件的扩展名
     *
     * @param filename
     * @return
     */
    public static String parseExtByFileName(String filename) {
        if (StringUtils.isEmpty(filename)) {
            return null;
        } else {
            int i = filename.lastIndexOf(".");
            if (i >= 0) {
                return filename.substring(i + 1, filename.length());
            } else {
                return null;
            }
        }
    }

    /**
     * 创建目录，创建异常抛出异常
     *
     * @param basePath
     * @param s
     * @return
     */
    public static File mkdir(String basePath, String s) {
        File file = new File(basePath, s);
        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException(String.format("目录创建失败：%s/%s", basePath, s));
        } else {
            return file;
        }
    }

    public static File mkdir(String basePath) {
        File file = new File(basePath);
        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException(String.format("目录创建失败：%s", basePath));
        } else {
            return file;
        }
    }

    public static File mkdir(File basePath, String s) {
        File file = new File(basePath, s);
        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException(String.format("目录创建失败：%s/%s", basePath, s));
        } else {
            return file;
        }
    }

    /**
     * 创建父目录，创建文件，写入脚本，创建目录和文件失败抛出异常
     *
     * @param installFile
     * @param content
     * @throws IOException
     */
    public static void createFileAndWrite(File installFile, String content) throws IOException {
        if (!installFile.getParentFile().exists())
            Assert.isTrue(installFile.getParentFile().mkdirs(), "父路径创建失败：" + installFile.getParentFile().getPath());
        if (!installFile.exists())
            Assert.isTrue(installFile.createNewFile(), "文件创建失败" + installFile.getPath());
        FileOutputStream output = new FileOutputStream(installFile);
        IOUtils.write(content, output);
        output.flush();
        output.close();
    }

    public static void createFileAndWrite(File installFile, byte[] content) throws IOException {
        if (!installFile.getParentFile().exists())
            Assert.isTrue(installFile.getParentFile().mkdirs(), "父路径创建失败：" + installFile.getParentFile().getPath());
        if (!installFile.exists())
            Assert.isTrue(installFile.createNewFile(), "文件创建失败" + installFile.getPath());
        FileOutputStream output = new FileOutputStream(installFile);
        IOUtils.write(content, output);
        output.flush();
        output.close();
    }

    public static String asString(File file, String charset) {
        try (FileInputStream input = new FileInputStream(file)) {
            return IOUtils.toString(input, charset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static File createTempFile(byte[] bytes) throws IOException {
        File tempFile = FileUtils.createTempFile();
        FileOutputStream output = new FileOutputStream(tempFile);
        IOUtils.write(bytes, output);
        output.flush();
        output.close();
        return tempFile;
    }

    /**
     * 删除尾部的多个正斜杠 /
     *
     * @param s
     * @return
     */
    public static String deleteEndDupForwardSlash(String s) {
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static File asTempFile(InputStream inputStream, boolean closeInputStream) {
        try {
            File tempFile = FileUtils.createTempFile();
            try (FileOutputStream output = new FileOutputStream(tempFile)) {
                IOUtils.write(inputStream, output);
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (closeInputStream) {
                IOUtils.close(inputStream);
            }
        }
    }

    public static void consumeOutputStreamUseTempFile(InputStream inputStream, boolean closeInputStream, boolean deleteTempFile, Consumer<FileOutputStream> osConsumer) {
        File file = asTempFile(inputStream, closeInputStream);
        try (FileOutputStream t = new FileOutputStream(file)) {
            osConsumer.accept(t);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            if (deleteTempFile) {
                try {
                    file.delete();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void deleteNoEx(File read) {
        try {
            if (read != null) {
                read.delete();
            }
        } catch (Exception e) {
        }
    }

    public static File createTempFileAndWrite(InputStream inputStream) {
        try {
            File f = createTempFile();
            try (FileOutputStream output = new FileOutputStream(f)) {
                IOUtils.write(inputStream, output);
                return f;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
