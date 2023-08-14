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
import com.eryansky.common.utils.reflection.ReflectionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity支持类
 * @author Eryan
 * @date 2019-01-02
 */
public abstract class PBaseEntity<T, PK extends Serializable> extends AbstractBaseEntity<T,PK> {


    /**
     * 自定义属性
     */
    private Map<String, Object> attributes;

    public PBaseEntity() {
        this.attributes = new HashMap<>();
    }

    public PBaseEntity(PK id) {
        super(id);
    }

    @JsonIgnore
    public Class getPKType() {
        return ReflectionUtils.getClassGenricType(getClass(), 1);
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
            Class idType = getPKType();
            if(Long.class == idType){
                setId((PK) Identities.uuid3());
            }else if(String.class == idType){
                setId((PK) Identities.uuid2());
            }
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
    public PBaseEntity addAttribute(String key, Object object) {
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

    public PBaseEntity setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }
}
