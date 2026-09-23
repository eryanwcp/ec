/**
 *  Copyright (c) 2012-2026 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.web.upload;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.security.LogUtils;
import com.eryansky.core.web.upload.exception.FileNameLengthLimitExceededException;
import com.eryansky.core.web.upload.exception.InvalidExtensionException;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.utils.AppConstants;
import org.apache.commons.fileupload2.core.FileUploadSizeException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 文件上传工具类（Apache Tika 智能类型识别版）
 * <p>Date: 2014-5-5 下午8:32
 * <p>Version: 2.0
 */
public class FileUploadUtils {
    // 默认大小 100M
    public static final long DEFAULT_MAX_SIZE = 100 * 1024 * 1024;
    private static final Logger log = LoggerFactory.getLogger(FileUploadUtils.class);
    // 默认上传的地址
    private static String defaultBaseDir = "disk";
    // 默认的文件名最大长度
    protected static final int DEFAULT_FILE_NAME_LENGTH = 200;

    // 单例复用 Tika 实例（内部线程安全）
    private static final Tika TIKA = new Tika();

    public static final String[] IMAGE_EXTENSION = {
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "tif", "tiff", "ico"
    };

    public static final String[] FLASH_EXTENSION = {
            "swf", "flv"
    };

    public static final String[] MEDIA_EXTENSION = {
            "swf", "flv", "mp3", "wav", "wma", "wmv", "mid", "avi", "mpg", "asf", "rm", "rmvb", "mp4"
    };

    public static final String[] DEFAULT_ALLOWED_EXTENSION = {
            // 图片
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "tif", "tiff", "ico",
            // word excel powerpoint wps
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "wps", "et", "dps", "odt", "ods", "odp", "txt", "csv", "rtf",
            // 压缩文件
            "rar", "zip", "gz", "bz2", "7z", "tar",
            // pdf
            "pdf", "ofd",
            // APP
            "apk", "ipa", "plist"
    };

    /**
     * 扩展名与允许的 MIME 类型映射关系表
     * 当 Tika 检测出的 MIME 类型在列表中时判定合法
     */
    private static final Map<String, Set<String>> EXTENSION_MIME_MAP = new HashMap<>();

    static {
        // 图片类
//        registerMimeTypes("jpg", "image/jpeg");
//        registerMimeTypes("jpeg", "image/jpeg");
//        registerMimeTypes("png", "image/png");
//        registerMimeTypes("gif", "image/gif");
//        registerMimeTypes("bmp", "image/bmp", "image/x-ms-bmp");
//        registerMimeTypes("webp", "image/webp");
//        registerMimeTypes("ico", "image/x-icon", "image/vnd.microsoft.icon");
//        registerMimeTypes("tif", "image/tiff");
//        registerMimeTypes("tiff", "image/tiff");
//        registerMimeTypes("heic", "image/heic", "image/heif");
//        registerMimeTypes("heif", "image/heic", "image/heif");


        registerMimeTypes("jpg", "image/jpeg","image/png","image/png","image/gif","image/bmp","image/x-ms-bmp","image/webp","image/x-icon", "image/vnd.microsoft.icon","image/tiff","image/heic","image/heif");//兼容写法
        registerMimeTypes("jpeg", "image/jpeg","image/png","image/png","image/gif","image/bmp","image/x-ms-bmp","image/webp","image/x-icon", "image/vnd.microsoft.icon","image/tiff","image/heic","image/heif");//兼容写法
        registerMimeTypes("png", "image/jpeg","image/png","image/png","image/gif","image/bmp","image/x-ms-bmp","image/webp","image/x-icon", "image/vnd.microsoft.icon","image/tiff","image/heic","image/heif");//兼容写法
        registerMimeTypes("gif", "image/jpeg","image/png","image/png","image/gif","image/bmp","image/x-ms-bmp","image/webp","image/x-icon", "image/vnd.microsoft.icon","image/tiff","image/heic","image/heif");//兼容写法
        registerMimeTypes("bmp", "image/jpeg","image/png","image/png","image/gif","image/bmp","image/x-ms-bmp","image/webp","image/x-icon", "image/vnd.microsoft.icon","image/tiff","image/heic","image/heif");//兼容写法
        registerMimeTypes("webp", "image/jpeg","image/png","image/png","image/gif","image/bmp","image/x-ms-bmp","image/webp","image/x-icon", "image/vnd.microsoft.icon","image/tiff","image/heic","image/heif");//兼容写法
        registerMimeTypes("ico", "image/jpeg","image/png","image/png","image/gif","image/bmp","image/x-ms-bmp","image/webp","image/x-icon", "image/vnd.microsoft.icon","image/tiff","image/heic","image/heif");//兼容写法
        registerMimeTypes("tif", "image/jpeg","image/png","image/png","image/gif","image/bmp","image/x-ms-bmp","image/webp","image/x-icon", "image/vnd.microsoft.icon","image/tiff","image/heic","image/heif");//兼容写法
        registerMimeTypes("tiff", "image/jpeg","image/png","image/png","image/gif","image/bmp","image/x-ms-bmp","image/webp","image/x-icon", "image/vnd.microsoft.icon","image/tiff","image/heic","image/heif");//兼容写法
        registerMimeTypes("heic", "image/jpeg","image/png","image/png","image/gif","image/bmp","image/x-ms-bmp","image/webp","image/x-icon", "image/vnd.microsoft.icon","image/tiff","image/heic","image/heif");//兼容写法
        registerMimeTypes("image/jpeg","image/png","image/png","image/gif","image/bmp","image/x-ms-bmp","image/webp","image/x-icon", "image/vnd.microsoft.icon","image/tiff","image/heic","image/heif");//兼容写法

        // 文档类
        registerMimeTypes("pdf", "application/pdf");
        registerMimeTypes("doc", "application/msword", "application/x-tika-msoffice");
        registerMimeTypes("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/zip");
        registerMimeTypes("xls", "application/vnd.ms-excel", "application/x-tika-msoffice");
        registerMimeTypes("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/zip");
        registerMimeTypes("ppt", "application/vnd.ms-powerpoint", "application/x-tika-msoffice");
        registerMimeTypes("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/zip");

        // WPS 专属与兼容支持
        registerMimeTypes("wps", "application/kswps", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/x-tika-msoffice", "application/zip");
        registerMimeTypes("et", "application/kset", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/x-tika-msoffice", "application/zip");
        registerMimeTypes("dps", "application/ksdps", "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/x-tika-msoffice", "application/zip");

        // 压缩包与归档类
        registerMimeTypes("zip", "application/zip", "application/x-zip-compressed");
        registerMimeTypes("rar", "application/x-rar-compressed", "application/vnd.rar");
        registerMimeTypes("7z", "application/x-7z-compressed");
        registerMimeTypes("tar", "application/x-tar");
        registerMimeTypes("gz", "application/gzip", "application/x-gzip");

        // 音视频与 Flash
        registerMimeTypes("mp3", "audio/mpeg", "audio/mp3");
        registerMimeTypes("mp4", "video/mp4");
        registerMimeTypes("wav", "audio/wav", "audio/x-wav");
        registerMimeTypes("avi", "video/x-msvideo");
        registerMimeTypes("flv", "video/x-flv");
        registerMimeTypes("swf", "application/x-shockwave-flash");

        // 应用安装包
        registerMimeTypes("apk", "application/vnd.android.package-archive", "application/zip");
    }

    private static void registerMimeTypes(String extension, String... mimeTypes) {
        EXTENSION_MIME_MAP.computeIfAbsent(extension.toLowerCase(Locale.ENGLISH), k -> new HashSet<>())
                .addAll(Arrays.asList(mimeTypes));
    }

    public static void setDefaultBaseDir(String defaultBaseDir) {
        FileUploadUtils.defaultBaseDir = defaultBaseDir;
    }

    public static String getDefaultBaseDir() {
        return defaultBaseDir;
    }

    /**
     * 以默认配置进行文件上传
     */
    public static String upload(HttpServletRequest request, MultipartFile file, BindingResult result) {
        return upload(request, file, result, DEFAULT_ALLOWED_EXTENSION);
    }

    /**
     * 以默认配置进行文件上传
     */
    public static String upload(HttpServletRequest request, MultipartFile file, BindingResult result, String[] allowedExtension) {
        try {
            return upload(request, getDefaultBaseDir(), file, allowedExtension, DEFAULT_MAX_SIZE, true, null);
        } catch (InvalidExtensionException.InvalidImageExtensionException e) {
            result.reject("upload.not.allow.image.extension");
        } catch (InvalidExtensionException.InvalidFlashExtensionException e) {
            result.reject("upload.not.allow.flash.extension");
        } catch (InvalidExtensionException.InvalidMediaExtensionException e) {
            result.reject("upload.not.allow.media.extension");
        } catch (InvalidExtensionException e) {
            result.reject("upload.not.allow.extension");
        } catch (FileUploadSizeException e) {
            result.reject("upload.exceed.maxSize");
        } catch (FileNameLengthLimitExceededException e) {
            result.reject("upload.filename.exceed.length");
        } catch (IOException e) {
            LogUtils.logError("file upload error", e);
            result.reject("upload.server.error");
        }
        return null;
    }

    /**
     * 文件上传 (MultipartFile)
     */
    public static String upload(HttpServletRequest request, String dir, MultipartFile file, String[] allowedExtension,
                                long maxSize, boolean needDatePathAndRandomName, String _prefix) throws InvalidExtensionException, FileUploadSizeException, IOException, FileNameLengthLimitExceededException {

        String originalFilename = DiskUtils.getMultipartOriginalFilename(file);
        int fileNamelength = originalFilename.length();
        if (fileNamelength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH) {
            throw new FileNameLengthLimitExceededException(originalFilename, fileNamelength, FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
        }

        File desc = null;
        String filename = null;

        assertAllowed(file, allowedExtension, maxSize);

        if (request != null) {
            filename = extractFilename(file, dir, needDatePathAndRandomName, _prefix);
            desc = getAbsoluteFile(extractUploadDir(request), filename);
        } else {
            filename = extractFilename(file, dir, needDatePathAndRandomName, _prefix);
            String fileBasePath = getBasePath(filename);
            desc = getAbsoluteFile(fileBasePath);
        }
        file.transferTo(desc);
        return filename;
    }

    /**
     * 文件上传 (File)
     */
    public static String upload(HttpServletRequest request, String dir, File file, String[] allowedExtension,
                                long maxSize, boolean needDatePathAndRandomName, String _prefix) throws InvalidExtensionException, FileUploadSizeException, IOException, FileNameLengthLimitExceededException {

        int fileNamelength = file.getName().length();
        if (fileNamelength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH) {
            throw new FileNameLengthLimitExceededException(file.getName(), fileNamelength, FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
        }

        File desc = null;
        String filename = null;

        assertAllowed(file, allowedExtension, maxSize);

        if (request != null) {
            filename = extractFilename(file, dir, needDatePathAndRandomName, _prefix);
            desc = getAbsoluteFile(extractUploadDir(request), filename);
        } else {
            filename = extractFilename(file, dir, needDatePathAndRandomName, _prefix);
            String fileBasePath = getBasePath(filename);
            desc = getAbsoluteFile(fileBasePath);
        }
        if (!file.isDirectory()) {
            FileUtils.copyFile(file, desc);
        }
        return filename;
    }

    public static void upload(String path, String local) throws IOException {
        FileUtils.copyFile(new File(local), new File(path));
    }

    public static void upload(String path, InputStream inputStream) throws IOException {
        FileUtils.copyInputStreamToFile(inputStream, new File(path));
    }

    /**
     * 根据相对路径创建绝对路径
     */
    public static String getBasePath(String relativePath) {
        StringBuilder path = new StringBuilder();
        if (StringUtils.isNotBlank(relativePath)) {
            path.append(AppConstants.getDiskBasePath())
                    .append(File.separator).append(relativePath);
        }
        return path.toString();
    }

    /**
     * 获取文件的绝对路径，拦截跨目录写入
     */
    private static File getAbsoluteFile(String uploadDir, String filename) throws IOException {
        uploadDir = FilenameUtils.normalizeNoEndSeparator(uploadDir);
        File baseDirFile = new File(uploadDir);
        File desc = new File(baseDirFile, filename);

        // 【安全修复】路径跨越拦截：校验规范化路径，严格防止目录遍历漏洞 (Path Traversal)
        String canonicalDesc = desc.getCanonicalPath();
        String canonicalBase = baseDirFile.getCanonicalPath();

        if (!canonicalDesc.startsWith(canonicalBase + File.separator) && !canonicalDesc.equals(canonicalBase)) {
            throw new IOException("Security Error: Attempt to write outside of target directory.");
        }

        if (!desc.getParentFile().exists()) {
            desc.getParentFile().mkdirs();
        }
        if (!desc.exists()) {
            desc.createNewFile();
        }
        return desc;
    }

    public static File getAbsoluteFile(String fileName) throws IOException {
        File desc = new File(fileName);
        if (!desc.getParentFile().exists()) {
            desc.getParentFile().mkdirs();
        }
        if (!desc.exists()) {
            desc.createNewFile();
        }
        return desc;
    }

    public static File getAbsoluteFile(HttpServletRequest request, String filename) throws IOException {
        return getAbsoluteFile(extractUploadDir(request), filename);
    }

    public static String extractFilename(MultipartFile file, String baseDir, boolean needDatePathAndRandomName, String _prefix) throws UnsupportedEncodingException {
        String fileAllName = DiskUtils.getMultipartOriginalFilename(file);
        return extractFilename(fileAllName, baseDir, needDatePathAndRandomName, _prefix);
    }

    public static String extractFilename(File file, String baseDir, boolean needDatePathAndRandomName, String _prefix) throws UnsupportedEncodingException {
        String fileAllName = file.getName();
        return extractFilename(fileAllName, baseDir, needDatePathAndRandomName, _prefix);
    }

    public static String extractFilename(String fileAllName, String baseDir, boolean needDatePathAndRandomName, String _prefix) throws UnsupportedEncodingException {
        int slashIndex = fileAllName.indexOf("/");
        if (slashIndex >= 0) {
            fileAllName = fileAllName.substring(slashIndex + 1);
        }
        if (StringUtils.isNotBlank(_prefix)) {
            fileAllName = _prefix + "_" + fileAllName;
        }
        if (needDatePathAndRandomName) {
            fileAllName = baseDir + File.separator + FileUploadUtils.datePath() + File.separator + FileUploadUtils.encodingFilename(fileAllName);
        } else {
            fileAllName = baseDir + File.separator + fileAllName;
        }
        return fileAllName;
    }

    public static String encodingFilename(String filename) {
        filename = encodingFilenamePrefix(filename) + "_" + filename;
        return filename;
    }

    public static String encodingFilenamePrefix(String filename) {
        filename = Encrypt.hash(filename + System.nanoTime());
        return filename;
    }

    public static String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "MM");
    }

    /**
     * 是否允许文件上传 (MultipartFile)
     * @param file
     * @param allowedExtension
     * @param maxSize
     * @throws InvalidExtensionException
     * @throws FileUploadSizeException
     */
    public static void assertAllowed(MultipartFile file, String[] allowedExtension, long maxSize) throws FileUploadSizeException, InvalidExtensionException {
        assertAllowed(file, allowedExtension, maxSize,false);
    }

    /**
     * 是否允许文件上传 (MultipartFile)
     * 包含扩展名校验、文件大小校验以及核心的 Magic Bytes (文件二进制头魔数) 真实校验
     * @param file
     * @param allowedExtension
     * @param maxSize
     * @param checkMagicBytes 是否开启文件二进制头魔数校验
     * @throws InvalidExtensionException
     * @throws FileUploadSizeException
     */
    public static void assertAllowed(MultipartFile file, String[] allowedExtension, long maxSize,boolean checkMagicBytes)
            throws InvalidExtensionException, FileUploadSizeException {
        String filename = DiskUtils.getMultipartOriginalFilename(file);
        if (StringUtils.isBlank(filename) || filename.contains("../") || filename.contains("..\\") || filename.indexOf('\0') != -1) {
            throw new IllegalArgumentException("Invalid filename format.");
        }
        filename = FilenameUtils.getName(filename);
        String extension = FilenameUtils.getExtension(filename);

        // 1. 扩展名白名单校验
        if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension)) {
            throwInvalidExtensionException(allowedExtension, extension, filename);
        }

        // 2. 文件大小校验
        long size = file.getSize();
        if (maxSize != -1 && size > maxSize) {
            throw new FileUploadSizeException("not allowed upload size", maxSize, size);
        }

        // 3. 【Apache Tika 智能类型校验】
        if(checkMagicBytes){
            try (InputStream is = file.getInputStream()) {
                if (!checkMagicBytes(is, extension, filename)) {
                    throwInvalidExtensionException(allowedExtension, extension, filename);
                }
            } catch (IOException e) {
                LogUtils.logError("Failed to check magic bytes for file: " + filename, e);
                throwInvalidExtensionException(allowedExtension, extension, filename);
            }
        }
    }
    /**
     * 是否允许文件上传 (MultipartFile)
     * @param file
     * @param allowedExtension
     * @param maxSize
     * @throws InvalidExtensionException
     * @throws FileUploadSizeException
     */
    public static void assertAllowed(File file, String[] allowedExtension, long maxSize) throws InvalidExtensionException, FileUploadSizeException {
        assertAllowed(file,allowedExtension,maxSize,false);
    }
    /**
     * 是否允许文件上传 (MultipartFile)
     * 包含扩展名校验、文件大小校验以及核心的 Magic Bytes (文件二进制头魔数) 真实校验
     * @param file
     * @param allowedExtension
     * @param maxSize
     * @param checkMagicBytes 是否开启文件二进制头魔数校验
     * @throws InvalidExtensionException
     * @throws FileUploadSizeException
     */
    public static void assertAllowed(File file, String[] allowedExtension, long maxSize,boolean checkMagicBytes) throws InvalidExtensionException, FileUploadSizeException {
        String filename = file.getName();
        if (StringUtils.isBlank(filename) || filename.contains("../") || filename.contains("..\\") || filename.indexOf('\0') != -1) {
            throw new IllegalArgumentException("Invalid filename format.");
        }
        filename = FilenameUtils.getName(filename);
        String extension = FilenameUtils.getExtension(filename);

        // 1. 扩展名白名单校验
        if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension)) {
            throwInvalidExtensionException(allowedExtension, extension, filename);
        }

        // 2. 文件大小校验
        long size = file.length();
        if (maxSize != -1 && size > maxSize) {
            throw new FileUploadSizeException("not allowed upload size", maxSize, size);
        }

        // 3. 【Apache Tika 智能类型校验】
        if(checkMagicBytes){
            try (InputStream is = new FileInputStream(file)) {
                if (!checkMagicBytes(is, extension, filename)) {
                    throwInvalidExtensionException(allowedExtension, extension, filename);
                }
            } catch (IOException e) {
                LogUtils.logError("Failed to check magic bytes for file: " + filename, e);
                throwInvalidExtensionException(allowedExtension, extension, filename);
            }
        }

    }

    private static void throwInvalidExtensionException(String[] allowedExtension, String extension, String filename) throws InvalidExtensionException {
        if (allowedExtension == IMAGE_EXTENSION) {
            throw new InvalidExtensionException.InvalidImageExtensionException(allowedExtension, extension, filename);
        } else if (allowedExtension == FLASH_EXTENSION) {
            throw new InvalidExtensionException.InvalidFlashExtensionException(allowedExtension, extension, filename);
        } else if (allowedExtension == MEDIA_EXTENSION) {
            throw new InvalidExtensionException.InvalidMediaExtensionException(allowedExtension, extension, filename);
        } else {
            throw new InvalidExtensionException(allowedExtension, extension, filename);
        }
    }

    /**
     * 使用 Apache Tika 校验流的真实 MIME 类型与扩展名是否相符
     *
     * @param inputStream 文件流
     * @param extension   文件扩展名
     * @param filename    原始文件名（辅助 Tika 提高检测准确度）
     * @return true校验通过，false校验失败
     */
    public static boolean checkMagicBytes(InputStream inputStream, String extension, String filename) {
        if (inputStream == null || StringUtils.isBlank(extension)) {
            return false;
        }

        String ext = extension.toLowerCase(Locale.ENGLISH);
        Set<String> allowedMimes = EXTENSION_MIME_MAP.get(ext);

        // 1. 对于未配置 MIME 校验规则的扩展名（例如 txt, csv 等无魔数文件），直接放行
        if (allowedMimes == null || allowedMimes.isEmpty()) {
            return true;
        }
        // 标记并重置流指针，防止 Tika 读取后影响后续文件保存
        InputStream markableStream = inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
        markableStream.mark(8192); // 预留 8KB 探测缓冲区
        try {
            // 2. 利用 Tika 自动探测二进制文件的真实 MIME 类型
            // 传入 filename 可以提供后缀线索，提高分析容器类文件（如 epub, zip, office 等）的效率
            String detectedMimeType = TIKA.detect(markableStream, filename);

            if (StringUtils.isBlank(detectedMimeType)) {
                log.warn("Tika failed to detect MIME type for file. filename={}, extension={}", filename, ext);
                return false;
            }

            // 3. 判断探测出来的 MIME 是否在扩展名允许列表中
            boolean isMatched = allowedMimes.contains(detectedMimeType.toLowerCase(Locale.ENGLISH));
            if(!isMatched){
                log.warn("File MIME type mismatch detected! filename={}, extension={}, detectedMime={}, allowedMimes={}",filename,ext,detectedMimeType, JsonMapper.toJsonString(allowedMimes));
            }
            return isMatched;
        } catch (IOException e) {
            LogUtils.logError("Tika detect file error", e);
            return false;
        } finally {
            try {
                // 恢复流指针到初始位置
                markableStream.reset();
            } catch (IOException e) {
                log.error("Failed to reset input stream after Tika detection", e);
            }
        }
    }

    /**
     * 兼容重载：无需文件名入参的魔数校验接口
     */
    public static boolean checkMagicBytes(InputStream inputStream, String extension) {
        return checkMagicBytes(inputStream, extension, null);
    }

    public static boolean isAllowedExtension(String extension, String[] allowedExtension) {
        if (StringUtils.isBlank(extension) || allowedExtension == null) {
            return false;
        }
        return Arrays.stream(allowedExtension).anyMatch(ext -> ext.equalsIgnoreCase(extension));
    }

    public static String extractUploadDir(HttpServletRequest request) {
        return request.getSession().getServletContext().getRealPath("/");
    }

    public static void delete(HttpServletRequest request, String fileName) throws IOException {
        if (StringUtils.isEmpty(fileName)) {
            return;
        }
        // 清理路径中的非法字符/相对路径，防止路径穿越攻击
        String safeFileName = FilenameUtils.getName(fileName);

        File desc = null;
        if (request == null) {
            String fileAbsoluteName = getBasePath(safeFileName);
            desc = getAbsoluteFile(fileAbsoluteName);
        } else {
            desc = getAbsoluteFile(extractUploadDir(request), safeFileName);
        }
        if (desc.exists()) {
            desc.delete();
        }
    }
}