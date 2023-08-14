/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.security;

import java.io.Serializable;

/**
 * 权限（岗位）
 * @author Eryan
 * @date 2023-06-07
 */
public class PermissonPost implements Serializable {
    /**
     * ID
     */
    private String id;
    /**
     * 编码
     */
    private String code;
    /**
     * 名称
     */
    private String name;
    /**
     * 所属机构
     */
    private String organId;


    public PermissonPost(String code) {
        this.code = code;
    }

    public PermissonPost(String id, String code) {
        this.id = id;
        this.code = code;
    }

    public PermissonPost(String id, String code, String name, String organId) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.organId = organId;
    }

    public String getId() {
        return id;
    }

    public PermissonPost setId(String id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return code;
    }

    public PermissonPost setCode(String code) {
        this.code = code;
        return this;
    }

    public String getName() {
        return name;
    }

    public PermissonPost setName(String name) {
        this.name = name;
        return this;
    }

    public String getOrganId() {
        return organId;
    }

    public PermissonPost setOrganId(String organId) {
        this.organId = organId;
        return this;
    }
}
