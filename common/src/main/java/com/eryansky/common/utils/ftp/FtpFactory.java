/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FTP 使用Resource方式注解 <br>
 * 例如：@Resource(name = "payFtp")
 *
 * @author Eryan
 * @date 2012-3-20 下午1:30:39
 */
public class FtpFactory {

    private static final Logger logger = LoggerFactory.getLogger(FtpFactory.class);
    public final static String CHARSET_UTF_8 = "UTF-8";
    public final static String CHARSET_ISO8859_1 = "iso-8859-1";
    /**
     * 服务器地址
     */
    private String url;
    /**
     * FTP端口
     */
    private int port;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;

    public FtpFactory() {
    }

    public FtpFactory(String url, int port, String username, String password) {
        this.url = url;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 创建FTPClient
     * ftp.logout();
     * ftp.disconnect();
     * @param path   根目录为""
     * @return
     * @throws IOException
     */
    public FTPClient createFTPClient(String path)
            throws IOException {

        FTPClient ftp = new FTPClient();
        int reply;
        ftp.connect(url, port);
        // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
        ftp.login(username, password);// 登录
//        ftp.enterRemotePassiveMode();
        ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
        }
        ftp.changeWorkingDirectory(path);// 转移到FTP服务器目录
        return ftp;
    }
    /**
     * 查询ftp服务器上指定路径所有文件名
     *
     * @param path       根目录为""
     * @param remotePath FTP服务器上的相对路径
     * @return
     */
    public List<String> listFiles(String path, String remotePath) {
        ArrayList<String> resultList = new ArrayList<String>();
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(url, port);
            // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);// 登录
//            ftp.enterRemotePassiveMode();
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return resultList;
            }
            ftp.changeWorkingDirectory(remotePath);// 转移到FTP服务器目录
            FTPFile[] fs = ftp.listFiles();
            for (FTPFile ff : fs) {
                resultList.add(ff.getName());
                // if (ff.getName().equals(fileName)) {
                // File localFile = new File(localPath + "/" + ff.getName());
                // OutputStream is = new FileOutputStream(localFile);
                // ftp.retrieveFile(ff.getName(), is);
                // is.close();
                // }
            }
            ftp.logout();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    logger.error(ioe.getMessage(), ioe);
                }
            }
        }
        return resultList;
    }

    /**
     * 判断ftp服务器文件是否存在
     *
     * @param path 根目录为""
     * @param ftp
     * @return
     * @throws IOException
     */
    public static boolean existFile(String path, FTPClient ftp) throws IOException {
        boolean flag = false;
        FTPFile[] ftpFileArr = ftp.listFiles(path);
        if (ftpFileArr.length > 0) {
            flag = true;
        }
        return flag;
    }

    /**
     * 创建多层目录文件，如果有ftp服务器已存在该文件，则不创建，如果无，则创建
     *
     * @param path   根目录为""
     * @param remote
     * @return
     * @throws IOException
     */
    public boolean createDirectory(String path, String remote)
            throws IOException {

        FTPClient ftp = new FTPClient();
        int reply;
        ftp.connect(url, port);
        // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
        ftp.login(username, password);// 登录
//        ftp.enterRemotePassiveMode();
        ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
        }
        ftp.changeWorkingDirectory(path);// 转移到FTP服务器目录

        boolean success = true;
        String directory = remote + File.separator;
        // String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
        // 如果远程目录不存在，则递归创建远程服务器目录
        if (!directory.equalsIgnoreCase(File.separator) && !changeWorkingDirectory(directory, ftp)) {
            int start = 0;
            int end = 0;
            if (directory.startsWith(File.separator)) {
                start = 1;
            } else {
                start = 0;
            }
            end = directory.indexOf(File.separator, start);
            String paths = "";
            while (true) {

                String subDirectory = remote.substring(start, end);
                path = path + File.separator + subDirectory;
                if (!existFile(path, ftp)) {
                    if (makeDirectory(subDirectory, ftp)) {
                        changeWorkingDirectory(subDirectory, ftp);
                    } else {
                        logger.warn("创建目录[" + subDirectory + "]失败");
                        changeWorkingDirectory(subDirectory, ftp);
                    }
                } else {
                    changeWorkingDirectory(subDirectory, ftp);
                }

                paths = paths + File.separator + subDirectory;
                start = end + 1;
                end = directory.indexOf(File.separator, start);
                // 检查所有目录是否创建完毕
                if (end <= start) {
                    break;
                }
            }
        }
        return success;
    }

    /**
     * 创建目录
     *
     * @param dir
     * @param ftp
     * @return
     */
    public static boolean makeDirectory(String dir, FTPClient ftp) {
        boolean flag = true;
        try {
            flag = ftp.makeDirectory(dir);
            if (flag) {
                logger.info("创建文件夹" + dir + " 成功！");
            } else {
                logger.warn("创建文件夹" + dir + " 失败！");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return flag;
    }


    /**
     * 改变目录路径
     *
     * @param directory
     * @param ftp
     * @return
     */
    public boolean changeWorkingDirectory(String directory, FTPClient ftp) {
        boolean flag = true;
        try {
            flag = ftp.changeWorkingDirectory(directory);
            if (flag) {
                logger.info("进入文件夹" + directory + " 成功！");
            } else {
                logger.warn("进入文件夹" + directory + " 失败！");
            }
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
        }
        return flag;
    }


    /**
     * 向FTP服务器上传文件.
     *
     * @param path     FTP服务器保存目录 根目录为""
     * @param filename 上传到FTP服务器上的文件名
     * @param input    输入流
     * @param encoding 默认值："UTF-8"
     * @return 成功返回true，否则返回false
     */
    public boolean uploadFile(String path, String filename, InputStream input, String encoding) {
        boolean success = false;
        FTPClient ftp = new FTPClient();
        ftp.setControlEncoding(null != encoding ? encoding : CHARSET_UTF_8);
        try {
            int reply;
            ftp.connect(url, port);
            ftp.login(username, password);
//            ftp.enterRemotePassiveMode();
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            // 转到指定上传目录
            ftp.changeWorkingDirectory(path);
            ftp.setBufferSize(8*1024);
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            // 将上传文件存储到指定目录
            ftp.storeFile(filename, input);
            input.close();
            ftp.logout();
            success = true;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return success;
    }

    /**
     * 向FTP服务器上传文件.
     *
     * @param path          FTP服务器保存目录 根目录为""
     * @param filename      上传到FTP服务器上的文件名
     * @param inputFilename 输入流
     * @param encoding      默认值："UTF-8"
     * @return 成功返回true，否则返回false
     */
    public boolean uploadFile(String path, String filename,
                              String inputFilename, String encoding) {
        File file = new File(inputFilename);
        try {
            return uploadFile(path, filename, new BufferedInputStream(
                    new FileInputStream(file)), encoding);
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    /**
     * 从FTP服务器下载文件.
     *
     * @param remotePath FTP服务器上的相对路径
     * @param fileName   要下载的文件名
     * @param localPath  下载后保存到本地的路径
     * @param encoding   默认值："UTF-8"
     * @return
     */
    public boolean downloadFile(String remotePath, String fileName,
                                String localPath, String encoding) {
        // 初始表示下载失败
        boolean success = false;
        // 创建FTPClient对象
        FTPClient ftp = new FTPClient();
        ftp.setControlEncoding(null != encoding ? encoding : CHARSET_UTF_8);
        try {
            int reply;
            // 连接FTP服务器
            // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.connect(url, port);
            // 登录ftp
            ftp.login(username, password);
//            ftp.enterLocalPassiveMode();
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            // 转到指定下载目录
            ftp.changeWorkingDirectory(remotePath);
            // 列出该目录下所有文件
            FTPFile[] fs = ftp.listFiles();
            // 遍历所有文件，找到指定的文件
            for (FTPFile ff : fs) {
                if (ff.getName().equals(fileName)) {
                    // 根据绝对路径初始化文件
                    File localFile = new File(localPath + File.separator + ff.getName());
                    // 输出流
                    try(OutputStream is = new FileOutputStream(localFile)){
                        // 下载文件
                        ftp.retrieveFile(ff.getName(), is);
                    }
                    success = true;
                }
            }
            ftp.logout();
            // 下载成功

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                    logger.error(ioe.getMessage(), ioe);
                }
            }
        }
        return success;
    }
}
