/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.mapper;

import com.eryansky.client.common.vo.ExtendAttr;
import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.eryansky.core.orm.mybatis.entity.TreeEntity;
import com.eryansky.modules.sys._enum.AreaType;
import org.hibernate.validator.constraints.Length;

/**
 * 行政区域
 *
 * @author Eryan
 * @date 2016-05-12
 */
public class Area extends TreeEntity<Area> {

    public static final String ROOT_ID = "1";
    /**
     * 简称
     */
    private String shortName;
    /**
     * 区域编码
     */
    private String code;
    /**
     * 信息分类编码
     */
    private String bizCode;
    /**
     * 区域类型 {@link com.eryansky.modules.sys._enum.AreaType}
     */
    private String type;
    /**
     * 自定义扩展数据
     */
    private ExtendAttr extendAttr;
    /**
     * 备注
     */
    private String remark;

    public Area() {
        super();
        this.sort = 30;
    }

    public Area(String id) {
        super(id);
    }

    @JsonIgnore
    public Area getParent() {
        return parent;
    }

    public void setParent(Area parent) {
        this.parent = parent;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Length(min = 0, max = 100)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    @Length(min = 1, max = 1)
    public String getType() {
        return type;
    }

    public String getTypeView() {
        return GenericEnumUtils.getDescriptionByValue(AreaType.class,type,type);
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getParentId() {
        return parent != null && parent.getId() != null ? parent.getId() : "0";
    }

    public ExtendAttr getExtendAttr() {
        return extendAttr;
    }

    public void setExtendAttr(ExtendAttr extendAttr) {
        this.extendAttr = extendAttr;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    public String getAttr(String key) {
        return null != this.extendAttr ? (String) this.extendAttr.get(key):null;
    }

    public Area setAttr(String key,Object value) {
        if(null != extendAttr){
            this.extendAttr.put(key,value);
        }
        return this;
    }
}