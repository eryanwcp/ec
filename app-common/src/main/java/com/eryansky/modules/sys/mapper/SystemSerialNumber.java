/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.mapper;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.modules.sys._enum.ResetType;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.eryansky.modules.sys.sn.MaxSerial;

/**
 * 序列号生成器
 *
 * @author Eryan
 * @date 2016-07-14
 */
public class SystemSerialNumber extends DataEntity<SystemSerialNumber> {

    public static final String DEFAULT_ID = "1";
    public static final String QUEUE_KEY = "queue_system_serial";
    public static final String LOCK_KEY = "lock_system_serial";
    public static final String LOCK_ITEM_KEY = "lock_system_serial_item";
    public static final String DEFAULT_KEY_MAX_SERIAL = "maxSerial";
    /**
     * APP标识
     */
    private String app;
    /**
     * 模块名称
     */
    private String moduleName;
    /**
     * 模块编码
     */
    private String moduleCode;
    /**
     * 流水号配置模板
     */
    private String configTemplate;
    /**
     * 系列号最大值
     */
    private MaxSerial maxSerial;
    /**
     * 重置类型 {@link ResetType}
     */
    private String resetType;
    /**
     * 是否自动增长标识 {@link YesOrNo}
     */
    private String isAutoIncrement;
    /**
     * 预生成流水号数量
     */
    private String preMaxNum;
    /**
     * 备注
     */
    private String remark;

    /**
     * 关键字
     */
    private String query;

    public SystemSerialNumber() {
        this.isAutoIncrement = YES;
        this.maxSerial = new MaxSerial();
        this.preMaxNum = "1";
    }

    @Override
    public void prePersist() {
        super.prePersist();
        if(StringUtils.isBlank(app)){
            this.app = DEFAULT_ID;
        }
    }

    @Override
    public void preUpdate() {
        super.preUpdate();
        if(StringUtils.isBlank(app)){
            this.app = DEFAULT_ID;
        }
    }

    public SystemSerialNumber(String id) {
        super(id);
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getConfigTemplate() {
        return configTemplate;
    }

    public void setConfigTemplate(String configTemplate) {
        this.configTemplate = configTemplate;
    }

    public MaxSerial getMaxSerial() {
        return maxSerial;
    }

    public void setMaxSerial(MaxSerial maxSerial) {
        this.maxSerial = maxSerial;
    }

    public String getIsAutoIncrement() {
        return isAutoIncrement;
    }

    public void setIsAutoIncrement(String isAutoIncrement) {
        this.isAutoIncrement = isAutoIncrement;
    }


    public String getIsAutoIncrementView() {
        return GenericEnumUtils.getDescriptionByValue(YesOrNo.class,isAutoIncrement,isAutoIncrement);
    }

    public String getResetType() {
        return resetType;
    }

    public void setResetType(String resetType) {
        this.resetType = resetType;
    }

    public String getResetTypeView() {
        return GenericEnumUtils.getDescriptionByValue(ResetType.class,resetType,resetType);
    }


    public String getPreMaxNum() {
        return preMaxNum;
    }

    public void setPreMaxNum(String preMaxNum) {
        this.preMaxNum = preMaxNum;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
