package com.eryansky.modules.disk._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;
import com.eryansky.common.utils.StringUtils;

/**
 * 文件类型
 */
public enum FileType implements IGenericEnum<FileType> {
    /**
     * 文档
     */
    Document("Document", "文档"),
    /**
     * 图片
     */
    Image("Image", "图片"),
    /**
     * 媒体
     */
    Music("Music", "音乐"),
    /**
     * 媒体
     */
    Video("Video", "视频"),
    /**
     * 软件
     */
    Soft("Soft", "软件"),
    /**
     * 其它
     */
    Other("Other", "其它");

    /**
     * 值 String型
     */
    private final String value;
    /**
     * 描述 String型
     */
    private final String description;

    FileType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 获取值
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取描述信息
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    public static FileType getByValue(String value) {
        return GenericEnumUtils.getByValue(FileType.class,value);
    }

    public static FileType getByDescription(String description) {
        return GenericEnumUtils.getByDescription(FileType.class,description);
    }

    /**
     * 根据文件后缀名返回文件分类
     *
     * @param fileSuffix
     * @return
     */
    public static FileType getByFileSuffix(String fileSuffix) {
        FileType fileType = FileType.Other;
        if (StringUtils.isBlank(fileSuffix)) {
            return fileType;
        }
        if (StringUtils.containsAny(fileSuffix, "txt", "wps", "doc", "docx", "pdf", "ppt", "pptx", "xls", "xlsx")) {
            return Document;
        } else if (StringUtils.containsAny(fileSuffix, "jpg", "jpeg", "png", "gif", "bmp", "psd")) {
            return Image;
        } else if (StringUtils.containsAny(fileSuffix, "mp3", "wma")) {
            return Music;
        } else if (StringUtils.containsAny(fileSuffix, "avi", "rmvb", "mp4", "swf", "fla")) {
            return Video;
        } else if (StringUtils.containsAny(fileSuffix, "exe", "apk")) {
            return Soft;
        }
        return fileType;
    }
}