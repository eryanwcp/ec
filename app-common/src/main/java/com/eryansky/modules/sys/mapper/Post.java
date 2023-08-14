/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.mapper;


import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.modules.sys.utils.OrganUtils;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * 岗位
 *
 * @author Eryan
 * @date 2018-05-08
 */
@JsonFilter(" ")
public class Post extends DataEntity<Post> {

    /**
     * 岗位名称
     */
    private String name;
    /**
     * 编码
     */
    private String code;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 备注
     */
    private String remark;
    /**
     * 机构ID
     */
    private String organId;

    /**
     * 附属机构
     */
    private List<String> organIds;

    private String query;


    public Post() {
        this.organIds = Lists.newArrayList();
    }

    public Post(String id) {
        super(id);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getSort() {
        return this.sort;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setOrganId(String organId) {
        this.organId = organId;
    }

    public String getOrganId() {
        return this.organId;
    }

    public List<String> getOrganIds() {
        return organIds;
    }

    public void setOrganIds(List<String> organIds) {
        this.organIds = organIds;
    }

    public String getOrganName() {
        return OrganUtils.getOrganName(organId);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
