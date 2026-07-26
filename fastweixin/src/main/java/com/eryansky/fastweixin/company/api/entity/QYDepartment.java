package com.eryansky.fastweixin.company.api.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.eryansky.fastweixin.api.entity.BaseModel;

/**
 * 部门
 * @author Eryan
 * @date 2016-03-15
 */
public class QYDepartment extends BaseModel{

    @JSONField(name = "id")
    private Integer id;
    @JSONField(name = "name")
    private String name;
    /**
     * 英文名称，此字段从2019年12月30日起，对新创建第三方应用不再返回，2020年6月30日起，对所有历史第三方应用不再返回该字段
     */
    @JSONField(name = "name_en")
    private String nameEn;
    @JSONField(name = "parentid")
    private Integer parentId;
    @JSONField(name = "order")
    private Integer order;

    public QYDepartment() {
    }

    public QYDepartment(String name, Integer parentId, Integer order) {
        this.name = name;
        this.parentId = (parentId == null || parentId < 1) ? 1 : parentId;
        this.order = order;
    }

    public QYDepartment(Integer id, String name, Integer parentId, Integer order) {
        this.id = id;
        this.name = name;
        this.parentId = (parentId == null || parentId < 1) ? 1 : parentId;
        this.order = order;
    }

    public QYDepartment(Integer id, String name, String nameEn, Integer parentId, Integer order) {
        this.id = id;
        this.name = name;
        this.nameEn = nameEn;
        this.parentId = parentId;
        this.order = order;
    }

    public Integer getId() {
        return id;
    }

    public QYDepartment setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public QYDepartment setName(String name) {
        this.name = name;
        return this;
    }

    public String getNameEn() {
        return nameEn;
    }

    public QYDepartment setNameEn(String nameEn) {
        this.nameEn = nameEn;
        return this;
    }

    public Integer getParentId() {
        return parentId;
    }

    public QYDepartment setParentId(Integer parentId) {
        this.parentId = parentId;
        return this;
    }

    public Integer getOrder() {
        return order;
    }

    public QYDepartment setOrder(Integer order) {
        this.order = order;
        return this;
    }

    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", order=" + order +
                '}';
    }
}
