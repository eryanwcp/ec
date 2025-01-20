package com.eryansky.modules.disk.extend;

import org.apache.commons.io.FileUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.nio.file.Files;

/**
 * 自定义上传文件对象
 */
public class CustomMultipartFile implements MultipartFile, Serializable {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件服务器只接受file参数
     */
    private String attachmentName = "file";


    private byte[] fileData;

    public CustomMultipartFile(String fileName,
                               byte[] fileData) {
        this.fileName = fileName;
        this.fileData = fileData;
    }

    public CustomMultipartFile(String attachmentName, String fileName,
                               byte[] fileData) {
        this.fileName = fileName;
        this.fileData = fileData;
        this.attachmentName = attachmentName;
    }

    public String getName() {
        return attachmentName;
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return new MimetypesFileTypeMap().getContentType(fileName);
    }

    @Override
    public boolean isEmpty() {
        return fileData == null || fileData.length == 0;
    }

    @Override
    public long getSize() {
        return fileData.length;
    }

    @Override
    public byte[] getBytes() {
        return fileData;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(fileData);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        FileUtils.writeByteArrayToFile(dest, fileData);
        if (dest.isAbsolute() && !dest.exists()) {
            FileCopyUtils.copy(this.getInputStream(), Files.newOutputStream(dest.toPath()));
        }
    }
}
