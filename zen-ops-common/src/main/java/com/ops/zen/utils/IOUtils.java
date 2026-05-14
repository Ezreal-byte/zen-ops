package com.ops.zen.utils;

import java.io.*;
import java.nio.charset.Charset;

public abstract class IOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 4096;

    public static final Charset UTF8_CHARSET = Charset.forName("utf-8");

    public static void write(byte[] input, Writer output) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        write((InputStream) in, (Writer) output);
    }

    public static void write(byte[] input, Writer output, String encoding) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        write((InputStream) in, output, encoding);
    }

    public static void write(byte[] input, OutputStream output) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        write((InputStream) in, (OutputStream) output);
    }

    public static int write(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;

        int n1;
        for (boolean n = false; -1 != (n1 = input.read(buffer)); count += n1) {
            output.write(buffer, 0, n1);
        }

        return count;
    }

    public static int write(Reader input, Writer output) throws IOException {
        char[] buffer = new char[4096];
        int count = 0;

        int n1;
        for (boolean n = false; -1 != (n1 = input.read(buffer)); count += n1) {
            output.write(buffer, 0, n1);
        }

        return count;
    }

    public static void write(InputStream input, Writer output) throws IOException {
        InputStreamReader in = new InputStreamReader(input);
        write((Reader) in, (Writer) output);
    }

    public static void write(InputStream input, Writer output, String encoding) throws IOException {
        InputStreamReader in = new InputStreamReader(input, encoding);
        write((Reader) in, (Writer) output);
    }

    public static void write(Reader input, OutputStream output) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(output);
        write((Reader) input, (Writer) out);
        out.flush();
    }

    public static void write(String input, OutputStream output) throws IOException {
        StringReader in = new StringReader(input);
        OutputStreamWriter out = new OutputStreamWriter(output);
        write((Reader) in, (Writer) out);
        out.flush();
    }

    @Deprecated
    public static void write(CharSequence input, OutputStream output) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(output));
        int i = 0;
        while (i < input.length()) {
            out.write(input.charAt(i));
            i++;
        }
        out.flush();
    }

    public static void write(String input, Writer output) throws IOException {
        output.write(input);
    }

    public static void close(Reader input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException arg1) {
                ;
            }

        }
    }

    public static void close(Writer output) {
        if (output != null) {
            try {
                output.close();
            } catch (IOException arg1) {
                ;
            }

        }
    }

    public static void close(OutputStream output) {
        if (output != null) {
            try {
                output.close();
            } catch (IOException arg1) {
                ;
            }

        }
    }

    public static void close(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException arg1) {
                ;
            }

        }
    }

    public static String toString(InputStream input) throws IOException {
        StringWriter sw = new StringWriter();
        write((InputStream) input, (Writer) sw);
        return sw.toString();
    }

    public static String toString(InputStream input, String encoding) throws IOException {
        if (input == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        write((InputStream) input, sw, encoding);
        return sw.toString();
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write((InputStream) input, (OutputStream) output);
        return output.toByteArray();
    }

    public static String toString(Reader input) throws IOException {
        StringWriter sw = new StringWriter();
        write((Reader) input, (Writer) sw);
        return sw.toString();
    }

    public static byte[] toByteArray(Reader input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write((Reader) input, (OutputStream) output);
        return output.toByteArray();
    }

    public static byte[] toByteArray(String input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write((String) input, (OutputStream) output);
        return output.toByteArray();
    }

    public static File writeBytesToFile(byte[] filedata, String filename) throws IOException {
        File file = new File(filename);
        File pFile = file.getParentFile();
        if (!pFile.exists()) {
            pFile.mkdirs();
        }

        FileOutputStream outStream = new FileOutputStream(file);

        try {
            outStream.write(filedata);
        } finally {
            outStream.close();
        }

        return file;
    }

    public static ByteArrayOutputStream getByteStreamFromFile(File newfile) throws IOException {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream stream = null;
        stream = new FileInputStream(newfile);
        boolean index = false;

        int index1;
        try {
            while ((index1 = stream.read(buffer, 0, 4096)) > 0) {
                out.write(buffer, 0, index1);
            }
        } finally {
            stream.close();
        }

        return out;
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        return copy((InputStream) input, (OutputStream) output, 4096);
    }

    public static int copyAndCloseInput(InputStream input, OutputStream output) throws IOException {
        int arg1;
        try {
            arg1 = copy((InputStream) input, (OutputStream) output, 4096);
        } finally {
            input.close();
        }

        return arg1;
    }

    public static int copyAndCloseInput(InputStream input, OutputStream output, int bufferSize) throws IOException {
        int arg2;
        try {
            arg2 = copy(input, output, bufferSize);
        } finally {
            input.close();
        }

        return arg2;
    }

    public static int copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        int avail = input.available();
        if (avail > 262144) {
            avail = 262144;
        }

        if (avail > bufferSize) {
            bufferSize = avail;
        }

        byte[] buffer = new byte[bufferSize];
        boolean n = false;
        int n1 = input.read(buffer);

        int total;
        for (total = 0; -1 != n1; n1 = input.read(buffer)) {
            output.write(buffer, 0, n1);
            total += n1;
        }

        return total;
    }

    public static void copy(Reader input, Writer output, int bufferSize) throws IOException {
        char[] buffer = new char[bufferSize];
        boolean n = false;

        for (int n1 = input.read(buffer); -1 != n1; n1 = input.read(buffer)) {
            output.write(buffer, 0, n1);
        }

    }

    public static long copyWithLimit(InputStream input, OutputStream output, long limit) throws IOException {
        int dataPos = 0;
//        Assert.isTrue(limit <= 2147483647L, "Too big limit %d", new Object[]{Long.valueOf(limit)});
        int dataTotalSize = (int) limit;
        int bufferSize = Math.min(dataTotalSize, 4096);

        int copied;
        for (byte[] buffer = new byte[bufferSize]; dataPos < dataTotalSize; dataPos += copied) {
            int dataSizeToRead = dataTotalSize - dataPos;
            dataSizeToRead = Math.min(dataSizeToRead, bufferSize);
            copied = input.read(buffer, 0, dataSizeToRead);
            if (copied < 0) {
                break;
            }

            output.write(buffer, 0, copied);
        }

//        Assert.isTrue(dataTotalSize == dataPos, "bad stream");
        return (long) dataPos;
    }

    public static String readStringFromStream(InputStream in) throws IOException {
        return readStringFromStream(in, (String) null);
    }

    public static String readStringFromStream(InputStream in, String charsetName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean i = true;

        int i1;
        while ((i1 = in.read()) != -1) {
            baos.write(i1);
        }

        return StringUtils.isNotEmpty(charsetName) ? baos.toString(charsetName) : baos.toString();
    }

    public static byte[] readBytesFromStream(InputStream in) throws IOException {
        int i = in.available();
        if (i < 4096) {
            i = 4096;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream(i);
        copy(in, bos);
        in.close();
        return bos.toByteArray();
    }

    public static void close(Closeable close) {
        try {
            if (close != null) {
                close.close();
            }
        } catch (Exception e) {

        }
    }

    public static void close(AutoCloseable close) {
        try {
            if (close != null) {
                close.close();
            }
        } catch (Exception e) {

        }
    }

}
