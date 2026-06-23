package com.eryansky.modules.disk.extend;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;
import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.util.Objects;

/**
 * 自定义上传文件对象
 * 用于将内存字节流模拟为 MultipartFile 接口
 * @author Eryan
 */
public class CustomMultipartFile implements MultipartFile, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 默认附件参数名
     */
    private static final String DEFAULT_ATTACHMENT_NAME = "file";
    /**
     * 文件名称
     */
    private final String fileName;

    /**
     * 附件参数名
     */
    private final String attachmentName;

    private final byte[] fileData;

    // 缓存 MimeType 类型映射表，避免重复实例化
    private static final MimetypesFileTypeMap MIME_TYPE_MAP = new MimetypesFileTypeMap();
    public CustomMultipartFile(String fileName, byte[] fileData) {
        this(DEFAULT_ATTACHMENT_NAME, fileName, fileData);
    }

    public CustomMultipartFile(String attachmentName, String fileName, byte[] fileData) {
        this.attachmentName = Objects.requireNonNull(attachmentName, "attachmentName cannot be null");
        this.fileName = Objects.requireNonNull(fileName, "fileName cannot be null");
        this.fileData = Objects.requireNonNull(fileData, "fileData cannot be null");
    }

    @Override
    public String getName() {
        return attachmentName;
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        // 使用静态缓存的映射表提高性能
        return MIME_TYPE_MAP.getContentType(fileName);
    }

    @Override
    public boolean isEmpty() {
        return fileData == null || fileData.length == 0;
    }

    @Override
    public long getSize() {
        return fileData != null ? fileData.length : 0L;
    }

    @Override
    public byte[] getBytes() {
        return fileData;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (fileData == null) {
            throw new IOException("File data is null");
        }
        return new ByteArrayInputStream(fileData);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        // 直接使用 Commons IO 的高效方法，移除冗余且错误的判断逻辑
        FileUtils.writeByteArrayToFile(dest, fileData);
    }
}
