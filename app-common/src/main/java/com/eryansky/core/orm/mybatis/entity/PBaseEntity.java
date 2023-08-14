/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.orm.mybatis.entity;

import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.persistence.AbstractBaseEntity;
import com.eryansky.common.orm.persistence.IUser;
import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.reflection.ReflectionUtils;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.utils.AppConstants;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Map;

/**
 * Entity支持类
 * @author Eryan
 * @date 2019-01-02
 */
public abstract class PBaseEntity<T, PK extends Serializable> extends AbstractBaseEntity<T,PK> {


    public PBaseEntity() {
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
        return SecurityUtils.getCurrentUser();
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
        if(null == this.dbName){
            return AppConstants.getJdbcType();
        }
        return super.getDbName();
    }
}
