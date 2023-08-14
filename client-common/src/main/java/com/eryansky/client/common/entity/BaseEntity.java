/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.client.common.entity;

import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.persistence.AbstractBaseEntity;
import com.eryansky.common.orm.persistence.IUser;
import com.eryansky.common.utils.Identities;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlTransient;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity支持类
 * @author Eryan
 * @version 2014-05-16
 */
public abstract class BaseEntity<T> extends AbstractBaseEntity<T,String> {

    /**
     * 自定义属性
     */
    private Map<String, Object> attributes;

    public BaseEntity() {
        this.attributes = new HashMap<>();
    }

    public BaseEntity(String id) {
        super(id);
    }

    @Override
    public String getId() {
        return super.getId();
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    @JsonIgnore
    @XmlTransient
    @Override
    public IUser getCurrentUser() {
        return null;
    }

    @Override
    public void prePersist() {
        // 不限制ID为UUID，调用setIsNewRecord()使用自定义ID
        if (!this.isNewRecord){
            setId(Identities.uuid2());
        }
    }

    @Override
    public void preUpdate() {

    }

    @JsonIgnore
    @Override
    public boolean getIsNewRecord() {
        return super.getIsNewRecord();
    }

    @JsonIgnore
    @XmlTransient
    @Override
    public Page<T> getEntityPage() {
        return super.getEntityPage();
    }

    @JsonIgnore
    @XmlTransient
    @Override
    public Map<String, String> getSqlMap() {
        return super.getSqlMap();
    }

    @JsonIgnore
    @Override
    public String getDbName() {
        return super.getDbName();
    }


    /**
     * 添加自定义属性
     * @param key key
     * @param object 值
     * @return
     */
    public BaseEntity addAttribute(String key, Object object) {
        this.attributes.put(key, object);
        return this;
    }

    /**
     * 返回自定义属性
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * 自定义属性
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public BaseEntity setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }
}
