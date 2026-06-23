/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.disk.extend;

import com.eryansky.common.utils.io.FileUtils;
import com.eryansky.core.web.upload.FileUploadUtils;
import com.eryansky.modules.disk.mapper.Folder;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.utils.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 磁盘文件管理器
 *
 * @author Eryan
 * @date 2015-06-24
 */
public class DISKManager implements IFileManager {

    protected static final Logger logger = LoggerFactory.getLogger(DISKManager.class);

    @Override
    public void init() {
        // 初始化逻辑，例如检查基础存储目录是否存在
    }

    @Override
    public void destroy() {
    }

    /**
     * 获取绝对路径并进行简单的路径穿越安全检查
     */
    private Path resolvePath(String path) {
        Path basePath = Paths.get(AppConstants.getDiskBasePath());
        Path resolvedPath = basePath.resolve(path).normalize();
        if (!resolvedPath.startsWith(basePath)) {
            throw new SecurityException("Invalid path: " + path);
        }
        return resolvedPath;
    }

    @Override
    public UploadStatus saveFile(String path, String localPath, boolean coverFile) throws IOException {
        Path targetPath = resolvePath(path);
        File targetFile = targetPath.toFile();

        if (!coverFile && targetFile.exists()) {
            return UploadStatus.Upload_New_File_Success; // 假设存在该状态，否则需根据业务定义
        }

        FileUtils.copyFile(new File(localPath), targetFile);
        return UploadStatus.Upload_New_File_Success;
    }

    @Override
    public UploadStatus saveFile(String path, InputStream inputStream, boolean coverFile) throws IOException {
        Path targetPath = resolvePath(path);
        File targetFile = targetPath.toFile();

        if (!coverFile && targetFile.exists()) {
            return UploadStatus.Upload_New_File_Failed;
        }

        try {
            FileUtils.copyInputStreamToFile(inputStream, targetFile);
            return UploadStatus.Upload_New_File_Success;
        } catch (IOException e) {
            logger.error("Save file error: path={}, msg={}", path, e.getMessage());
            throw e;
        }
    }

    @Override
    public DownloadStatus loadFile(String path, String localPath) throws IOException {
        Path srcPath = resolvePath(path);
        File srcFile = srcPath.toFile();

        if (!srcFile.exists()) {
            return DownloadStatus.Download_New_Failed;
        }

        FileUtils.copyFile(srcFile, new File(localPath));
        return DownloadStatus.Download_New_Success;
    }

    @Override
    public UploadStatus deleteFile(String path) throws IOException {
        Path targetPath = resolvePath(path);
        try {
            FileUtils.deleteFile(targetPath.toFile());
            return UploadStatus.Delete_Remote_Success;
        } catch (Exception e) {
            logger.error("elete file error: path={}, msg={}", path, e.getMessage());
            throw e;
        }
    }

    @Override
    public String getStorePath(Folder folder, String userId, String fileName) {
        String dir = DiskUtils.getDISKStoreDir(folder, userId);
        String code = FileUploadUtils.encodingFilenamePrefix(userId, fileName);
        // 使用 Path 处理路径拼接，避免不同操作系统的分隔符问题
        return Paths.get(dir, code + "_" + fileName).toString();
    }
}