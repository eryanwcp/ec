/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.security;

import java.io.Serializable;

/**
 * 权限（菜单/功能）
 * @author Eryan
 * @date 2016-03-15 
 */
public class Permisson implements Serializable {
    /**
     * ID
     */
    private String id;
    /**
     * 编码
     */
    private String code;
    /**
     * URL地址
     */
    private String markUrl;

    public Permisson(String id, String code, String markUrl) {
        this.id = id;
        this.code = code;
        this.markUrl = markUrl;
    }

    public String getId() {
        return id;
    }

    public Permisson setId(String id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return code;
    }

    public Permisson setCode(String code) {
        this.code = code;
        return this;
    }

    public String getMarkUrl() {
        return markUrl;
    }

    public Permisson setMarkUrl(String markUrl) {
        this.markUrl = markUrl;
        return this;
    }
}
