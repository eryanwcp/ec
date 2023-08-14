/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.security;

import java.io.Serializable;

/**
 * 权限（角色）
 * @author Eryan
 * @date 2016-03-15 
 */
public class PermissonRole implements Serializable {
    /**
     * ID
     */
    private String id;
    /**
     * 编码
     */
    private String code;


    public PermissonRole(String code) {
        this.code = code;
    }

    public PermissonRole(String id, String code) {
        this.id = id;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public PermissonRole setId(String id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return code;
    }

    public PermissonRole setCode(String code) {
        this.code = code;
        return this;
    }
}
