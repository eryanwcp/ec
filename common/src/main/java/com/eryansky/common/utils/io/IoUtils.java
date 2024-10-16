/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.io;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;


/**
 * IO操作工具类
 *
 * @author Eryan
 * @date 2012-8-16 下午1:31:16
 */
public class IoUtils extends IOUtils {

    public static byte[] readInputStream(InputStream inputStream, String inputStreamName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[16 * 1024];
        try {
            int bytesRead = inputStream.read(buffer);
            while (bytesRead != -1) {
                outputStream.write(buffer, 0, bytesRead);
                bytesRead = inputStream.read(buffer);
            }
        } catch (Exception e) {
//      throw new ServiceException("couldn't read input stream "+inputStreamName, e);
        }
        return outputStream.toByteArray();
    }

    public static String readFileAsString(String filePath) {
        byte[] buffer = new byte[(int) getFile(filePath).length()];
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(getFile(filePath)));
            inputStream.read(buffer);
        } catch (Exception e) {
//      throw new ServiceException("Couldn't read file " + filePath + ": " + e.getMessage());
        } finally {
            IoUtils.closeSilently(inputStream);
        }
        return new String(buffer);
    }

    public static File getFile(String filePath) {
        URL url = IoUtils.class.getClassLoader().getResource(filePath);
        try {
            return new File(url.toURI());
        } catch (Exception e) {
//      throw new ServiceException("Couldn't get file " + filePath + ": " + e.getMessage());
        }
        return null;
    }

    public static void writeStringToFile(String content, String filePath) {
        BufferedOutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(getFile(filePath)));
            outputStream.write(content.getBytes());
            outputStream.flush();
        } catch (Exception e) {
//      throw new ServiceException("Couldn't write file " + filePath, e);
        } finally {
            IoUtils.closeSilently(outputStream);
        }
    }


    /**
     * 将字节数据输出到输出流
     * @param data
     * @param outputStream
     */
    public static void copy(byte[] data, OutputStream outputStream) {
        try {
            outputStream.write(data);
            outputStream.flush();
        } catch (Exception e) {
//      throw new ServiceException("Couldn't write file " + filePath, e);
        } finally {
            IoUtils.closeSilently(outputStream);
        }
    }

    /**
     * Closes the given stream. The same as calling {@link java.io.InputStream#close()}, but
     * errors while closing are silently ignored.
     */
    public static void closeSilently(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException ignore) {
            // Exception is silently ignored
        }
    }

    /**
     * Closes the given stream. The same as calling {@link java.io.OutputStream#close()}, but
     * errors while closing are silently ignored.
     */
    public static void closeSilently(OutputStream outputStream) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException ignore) {
            // Exception is silently ignored
        }
    }

    /**
     * 流拷贝（指定位置）
     * @param input
     * @param output
     * @param start
     * @param end
     * @throws IOException
     */
    public static void copy(InputStream input, OutputStream output, long start, long end) throws IOException {
        long skipped = 0;
        skipped = input.skip(start);

        if (skipped < start) {
            throw new IOException("skip fail: skipped=" + Long.valueOf(skipped)+ ", start=" + Long.valueOf(start));
        }
        long bytesToRead = end - start + 1;
        byte[] buffer = new byte[2048];
        int len = buffer.length;
        while ((bytesToRead > 0) && (len >= buffer.length)) {
            try {
                len = input.read(buffer);
                if (bytesToRead >= len) {
                    output.write(buffer, 0, len);
                    bytesToRead -= len;
                } else {
                    output.write(buffer, 0, (int) bytesToRead);
                    bytesToRead = 0;
                }
            } catch (IOException e) {
                len = -1;
                throw e;
            }
            if (len < buffer.length)
                break;
        }

    }
}
