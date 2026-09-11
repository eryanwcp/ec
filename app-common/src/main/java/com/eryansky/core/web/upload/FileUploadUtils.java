/**
 *  Copyright (c) 2012-2026 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.web.upload;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.core.security.LogUtils;
import com.eryansky.core.web.upload.exception.FileNameLengthLimitExceededException;
import com.eryansky.core.web.upload.exception.InvalidExtensionException;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.utils.AppConstants;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 文件上传工具类
 * <p>Date: 2014-5-5 下午8:32
 * <p>Version: 1.0
 */
public class FileUploadUtils {
    // 默认大小 100M
    public static final long DEFAULT_MAX_SIZE = 100 * 1024 * 1024;
    // 默认上传的地址
    private static String defaultBaseDir = "disk";
    // 默认的文件名最大长度
    protected static final int DEFAULT_FILE_NAME_LENGTH = 200;

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
            // word excel powerpoint
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "wps", "et", "dps", "odt", "ods", "odp", "txt", "csv", "rtf",
            // 压缩文件
            "rar", "zip", "gz", "bz2", "7z", "tar",
            // pdf
            "pdf", "ofd",
            // APP
            "apk", "ipa", "plist"
    };

    /**
     * 定义支持偏移量的 Magic Byte 签名结构
     */
    public static class MagicSignature {
        public final int offset;          // 字节偏移量
        public final String hexSignature; // 十六进制签名

        public MagicSignature(int offset, String hexSignature) {
            this.offset = offset;
            this.hexSignature = hexSignature.toUpperCase(Locale.ENGLISH);
        }

        // 默认偏移量为 0 的便捷构造
        public MagicSignature(String hexSignature) {
            this(0, hexSignature);
        }
    }

    /**
     * 常见文件 Magic Bytes (文件头魔数) 签名表
     */
    private static final Map<String, List<MagicSignature>> MAGIC_BYTES_MAP = new HashMap<>();

    static {
        // 图片
        MAGIC_BYTES_MAP.put("jpg", Arrays.asList(new MagicSignature("FFD8FF")));
        MAGIC_BYTES_MAP.put("jpeg", Arrays.asList(new MagicSignature("FFD8FF")));
        MAGIC_BYTES_MAP.put("png", Arrays.asList(new MagicSignature("89504E47")));
        MAGIC_BYTES_MAP.put("gif", Arrays.asList(new MagicSignature("47494638")));
        MAGIC_BYTES_MAP.put("bmp", Arrays.asList(new MagicSignature("424D")));
        MAGIC_BYTES_MAP.put("webp", Arrays.asList(new MagicSignature("52494646"))); // RIFF header
        MAGIC_BYTES_MAP.put("ico", Arrays.asList(new MagicSignature("00000100")));
        MAGIC_BYTES_MAP.put("tif", Arrays.asList(new MagicSignature("49492A00"), new MagicSignature("4D4D002A")));
        MAGIC_BYTES_MAP.put("tiff", Arrays.asList(new MagicSignature("49492A00"), new MagicSignature("4D4D002A")));

        // 文档与压缩包
        MAGIC_BYTES_MAP.put("pdf", Arrays.asList(new MagicSignature("25504446"))); // %PDF
        MAGIC_BYTES_MAP.put("zip", Arrays.asList(new MagicSignature("504B0304"), new MagicSignature("504B0506"), new MagicSignature("504B0708"))); // PK..
        MAGIC_BYTES_MAP.put("rar", Arrays.asList(new MagicSignature("526172211A0700"), new MagicSignature("526172211A0701"))); // Rar!
        MAGIC_BYTES_MAP.put("7z", Arrays.asList(new MagicSignature("377ABCAF271C")));
        MAGIC_BYTES_MAP.put("gz", Arrays.asList(new MagicSignature("1F8B")));
        MAGIC_BYTES_MAP.put("tar", Arrays.asList(new MagicSignature(257, "7573746172"))); // tar 魔数带有257字节偏移

        // Office 文档 (OpenXML基于ZIP / OLE2二进制格式)
        MAGIC_BYTES_MAP.put("docx", Arrays.asList(new MagicSignature("504B0304")));
        MAGIC_BYTES_MAP.put("xlsx", Arrays.asList(new MagicSignature("504B0304")));
        MAGIC_BYTES_MAP.put("pptx", Arrays.asList(new MagicSignature("504B0304")));
        MAGIC_BYTES_MAP.put("apk", Arrays.asList(new MagicSignature("504B0304")));
        MAGIC_BYTES_MAP.put("doc", Arrays.asList(new MagicSignature("D0CF11E0A1B11AE1")));
        MAGIC_BYTES_MAP.put("xls", Arrays.asList(new MagicSignature("D0CF11E0A1B11AE1")));
        MAGIC_BYTES_MAP.put("ppt", Arrays.asList(new MagicSignature("D0CF11E0A1B11AE1")));
        MAGIC_BYTES_MAP.put("wps", Arrays.asList(new MagicSignature("D0CF11E0A1B11AE1"),new MagicSignature("504B0304")));
        MAGIC_BYTES_MAP.put("et", Arrays.asList(
                new MagicSignature("D0CF11E0A1B11AE1"),
                new MagicSignature("504B0304")
        ));
        MAGIC_BYTES_MAP.put("dps", Arrays.asList(
                new MagicSignature("D0CF11E0A1B11AE1"),
                new MagicSignature("504B0304")
        ));

        // 音视频与 Flash
        MAGIC_BYTES_MAP.put("mp3", Arrays.asList(new MagicSignature("494433"), new MagicSignature("FFFB"), new MagicSignature("FFF3"), new MagicSignature("FFF2")));
        MAGIC_BYTES_MAP.put("wav", Arrays.asList(new MagicSignature("52494646")));
        MAGIC_BYTES_MAP.put("avi", Arrays.asList(new MagicSignature("52494646")));
        MAGIC_BYTES_MAP.put("swf", Arrays.asList(new MagicSignature("435753"), new MagicSignature("465753"), new MagicSignature("5A5753")));
        MAGIC_BYTES_MAP.put("flv", Arrays.asList(new MagicSignature("464C56")));
        MAGIC_BYTES_MAP.put("mp4", Arrays.asList(new MagicSignature(4, "66747970"))); // mp4 'ftyp' 魔数带有4字节偏移
    }

    private static int counter = 0;

    public static void setDefaultBaseDir(String defaultBaseDir) {
        FileUploadUtils.defaultBaseDir = defaultBaseDir;
    }

    public static String getDefaultBaseDir() {
        return defaultBaseDir;
    }

    /**
     * 以默认配置进行文件上传
     *
     * @param request 当前请求
     * @param file    上传的文件
     * @param result  添加出错信息
     * @return 上传成功的文件路径/名称
     */
    public static String upload(HttpServletRequest request, MultipartFile file, BindingResult result) {
        return upload(request, file, result, DEFAULT_ALLOWED_EXTENSION);
    }


    /**
     * 以默认配置进行文件上传
     *
     * @param request          当前请求
     * @param file             上传的文件
     * @param result           添加出错信息
     * @param allowedExtension 允许上传的文件类型
     * @return
     */
    public static String upload(HttpServletRequest request, MultipartFile file, BindingResult result, String[] allowedExtension) {
        try {
            return upload(request, getDefaultBaseDir(), file, allowedExtension, DEFAULT_MAX_SIZE, true, null);
        } catch (IOException e) {
            LogUtils.logError("file upload error", e);
            result.reject("upload.server.error");
        } catch (InvalidExtensionException.InvalidImageExtensionException e) {
            result.reject("upload.not.allow.image.extension");
        } catch (InvalidExtensionException.InvalidFlashExtensionException e) {
            result.reject("upload.not.allow.flash.extension");
        } catch (InvalidExtensionException.InvalidMediaExtensionException e) {
            result.reject("upload.not.allow.media.extension");
        } catch (InvalidExtensionException e) {
            result.reject("upload.not.allow.extension");
        } catch (FileSizeLimitExceededException e) {
            result.reject("upload.exceed.maxSize");
        } catch (FileNameLengthLimitExceededException e) {
            result.reject("upload.filename.exceed.length");
        }
        return null;
    }

    /**
     * 文件上传
     *
     * @param request                   当前请求 从请求中提取 应用上下文根
     * @param dir                       当request不为空,入参为相对应用的基目录;当为空时,入参为除配置路径外的文件夹相对路径
     * @param file                      上传的文件
     * @param allowedExtension          允许的文件类型 null 表示允许所有
     * @param maxSize                   最大上传的大小 -1 表示不限制
     * @param needDatePathAndRandomName 是否需要日期目录和随机文件名前缀
     * @param _prefix                   文件名前缀 建议在needDatePathAndRandomName为false时使用
     * @return 返回上传成功的文件名
     */
    public static String upload(HttpServletRequest request, String dir, MultipartFile file, String[] allowedExtension, long maxSize, boolean needDatePathAndRandomName, String _prefix)
            throws InvalidExtensionException, IOException, FileNameLengthLimitExceededException, FileSizeLimitExceededException {
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

    public static String upload(HttpServletRequest request, String dir, File file, String[] allowedExtension, long maxSize, boolean needDatePathAndRandomName, String _prefix)
            throws InvalidExtensionException, IOException, FileNameLengthLimitExceededException, FileSizeLimitExceededException {
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
     *
     * @param relativePath 相对路径
     * @return 绝对路径字符串
     */
    public static String getBasePath(String relativePath) {
        StringBuffer path = new StringBuffer();
        if (StringUtils.isNotBlank(relativePath)) {
            path.append(AppConstants.getDiskBasePath())
                    .append(File.separator).append(relativePath);
        }
        return path.toString();
    }

    /**
     * 获取文件的绝对路径，拦截跨目录写入
     *
     * @param uploadDir 相对应用的基目录
     * @param filename  文件名
     * @return 安全的 File 对象
     * @throws IOException 路径不合法时抛出
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

    /**
     * 获取文件绝对路径
     *
     * @param request  HTTP请求对象
     * @param filename 文件名
     * @return File 对象
     * @throws IOException
     */
    public static File getAbsoluteFile(HttpServletRequest request, String filename) throws IOException {
        return getAbsoluteFile(extractUploadDir(request), filename);
    }

    /**
     * 提取文件名
     */
    public static String extractFilename(MultipartFile file, String baseDir, boolean needDatePathAndRandomName, String _prefix)
            throws UnsupportedEncodingException {
        String fileAllName = DiskUtils.getMultipartOriginalFilename(file);
        return extractFilename(fileAllName, baseDir, needDatePathAndRandomName, _prefix);
    }

    public static String extractFilename(File file, String baseDir, boolean needDatePathAndRandomName, String _prefix)
            throws UnsupportedEncodingException {
        String fileAllName = file.getName();
        return extractFilename(fileAllName, baseDir, needDatePathAndRandomName, _prefix);
    }

    public static String extractFilename(String fileAllName, String baseDir, boolean needDatePathAndRandomName, String _prefix)
            throws UnsupportedEncodingException {
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

    /**
     * 编码文件名
     */
    public static String encodingFilename(String filename) {
        filename = encodingFilenamePrefix(filename) + "_" + filename;
        return filename;
    }

    /**
     * 生成文件名前缀
     */
    public static String encodingFilenamePrefix(String filename) {
        filename = Encrypt.hash(filename + System.nanoTime() + counter++);
        return filename;
    }

    /**
     * 日期路径 即年/月/日 如2013/01/03
     */
    public static String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "MM");
    }

    /**
     * 是否允许文件上传 (MultipartFile)
     * 包含扩展名校验、文件大小校验以及核心的 Magic Bytes (文件二进制头魔数) 真实校验
     */
    public static void assertAllowed(MultipartFile file, String[] allowedExtension, long maxSize)
            throws InvalidExtensionException, FileSizeLimitExceededException {

        String filename = DiskUtils.getMultipartOriginalFilename(file);

        // 【安全修复】防御空字节注入、目录遍历异常字符
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
            throw new FileSizeLimitExceededException("not allowed upload size", maxSize, size);
        }

        // 3. 【核心安全修复】Magic Bytes 真实文件头部二进制校验
        try (InputStream is = file.getInputStream()) {
            if (!checkMagicBytes(is, extension)) {
                throwInvalidExtensionException(allowedExtension, extension, filename);
            }
        } catch (IOException e) {
            LogUtils.logError("Failed to check magic bytes for file: " + filename, e);
            throwInvalidExtensionException(allowedExtension, extension, filename);
        }
    }

    /**
     * 是否允许文件上传 (File)
     */
    public static void assertAllowed(File file, String[] allowedExtension, long maxSize)
            throws InvalidExtensionException, FileSizeLimitExceededException {

        String filename = file.getName();

        // 【安全修复】防御空字节注入、目录遍历异常字符
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
            throw new FileSizeLimitExceededException("not allowed upload size", maxSize, size);
        }

        // 3. 【核心安全修复】Magic Bytes 真实文件头部二进制校验
        try (InputStream is = Files.newInputStream(file.toPath())) {
            if (!checkMagicBytes(is, extension)) {
                throwInvalidExtensionException(allowedExtension, extension, filename);
            }
        } catch (IOException e) {
            LogUtils.logError("Failed to check magic bytes for file: " + filename, e);
            throwInvalidExtensionException(allowedExtension, extension, filename);
        }
    }

    /**
     * 抛出对应的扩展名异常
     */
    private static void throwInvalidExtensionException(String[] allowedExtension, String extension, String filename)
            throws InvalidExtensionException {
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
     * 读取 InputStream 前若干字节并校验是否匹配扩展名的 Magic Bytes 头部特征 (支持动态偏移量)
     *
     * @param inputStream 文件输入流
     * @param extension   扩展名
     * @return true: 匹配或未配置 Magic Bytes (放行文本等无魔数类型); false: 文件类型伪造
     */
    public static boolean checkMagicBytes(InputStream inputStream, String extension) {
        if (inputStream == null || StringUtils.isBlank(extension)) {
            return false;
        }

        String ext = extension.toLowerCase(Locale.ENGLISH);
        List<MagicSignature> signatures = MAGIC_BYTES_MAP.get(ext);

        // 未在魔数表中定义的类型（如 txt, csv 等无固定魔数的纯文本/数据文件），跳过魔数比对放行
        if (signatures == null || signatures.isEmpty()) {
            return true;
        }

        try {
            // 1. 动态计算该文件类型需要读取的最大字节深度 (最大偏移量 + 签名长度)
            int maxRequiredBytes = 0;
            for (MagicSignature sig : signatures) {
                int required = sig.offset + (sig.hexSignature.length() / 2);
                if (required > maxRequiredBytes) {
                    maxRequiredBytes = required;
                }
            }

            // 2. 保证缓冲数组足够大（至少读取 64 字节，如果有大偏移量如 tar 则根据最大需求动态扩容）
            int bufferSize = Math.max(64, maxRequiredBytes);
            byte[] header = new byte[bufferSize];

            // 3. 循环读取直到满足所需的字节数或流结束
            int bytesRead = 0;
            int read;
            while (bytesRead < bufferSize && (read = inputStream.read(header, bytesRead, bufferSize - bytesRead)) != -1) {
                bytesRead += read;
            }

            if (bytesRead == 0) {
                return false;
            }

            // 4. 将读取到的字节流转为十六进制字符串
            String fileHeaderHex = bytesToHex(header, bytesRead);

            // 5. 根据各个签名的【偏移量】精准比对
            for (MagicSignature signature : signatures) {
                int hexOffset = signature.offset * 2; // 字节偏移量转为16进制字符串偏移量 (1 byte = 2 hex chars)
                String expectedHex = signature.hexSignature;

                // 确保读取到的内容长度足够包含该签名
                if (fileHeaderHex.length() >= hexOffset + expectedHex.length()) {
                    String actualHex = fileHeaderHex.substring(hexOffset, hexOffset + expectedHex.length());
                    if (actualHex.equals(expectedHex)) {
                        return true;
                    }
                }
            }
            return false;

        } catch (IOException e) {
            LogUtils.logError("Read file magic bytes error", e);
            return false;
        }
    }

    /**
     * 字节数组转 Hex 字符串
     */
    private static String bytesToHex(byte[] src, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || length <= 0) {
            return "";
        }
        for (int i = 0; i < length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase(Locale.ENGLISH);
            if (hv.length() < 2) {
                stringBuilder.append('0');
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 判断扩展名是否符合白名单
     *
     * @param extension        扩展名
     * @param allowedExtension 允许的扩展名列表
     * @return 是否允许
     */
    public static boolean isAllowedExtension(String extension, String[] allowedExtension) {
        if (StringUtils.isBlank(extension) || allowedExtension == null) {
            return false;
        }
        return Arrays.stream(allowedExtension).anyMatch(ext -> ext.equalsIgnoreCase(extension));
    }

    /**
     * 提取上传的根目录 默认是应用的根
     *
     * @param request HTTP 请求
     * @return 实际存储根路径
     */
    public static String extractUploadDir(HttpServletRequest request) {
        return request.getSession().getServletContext().getRealPath("/");
    }

    /**
     * 删除物理文件
     *
     * @param request  HTTP 请求
     * @param fileName 相对或绝对文件名
     * @throws IOException
     */
    public static void delete(HttpServletRequest request, String fileName) throws IOException {
        if (StringUtils.isEmpty(fileName)) {
            return;
        }
        File desc = null;
        if (request == null) {
            String fileAbsoluteName = getBasePath(fileName);
            desc = getAbsoluteFile(fileAbsoluteName);
        } else {
            desc = getAbsoluteFile(extractUploadDir(request), fileName);
        }
        if (desc.exists()) {
            desc.delete();
        }
    }
}