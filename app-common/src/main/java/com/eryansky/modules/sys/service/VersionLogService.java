/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.service;

import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.core.orm.mybatis.service.CrudService;
import com.eryansky.modules.sys._enum.VersionLogType;
import com.eryansky.modules.sys._enum.YesOrNo;
import com.eryansky.modules.sys.dao.VersionLogDao;
import com.eryansky.modules.sys.mapper.VersionLog;
import com.eryansky.utils.AppConstants;
import org.springframework.stereotype.Service;

/**
 * 版本更新日志
 *
 * @author Eryan
 * @date 2015-09-29
 */
@Service
public class VersionLogService extends CrudService<VersionLogDao, VersionLog> {

    /**
     * 删除
     *
     * @param id
     */
    public void delete(String id) {
        dao.delete(new VersionLog(id));
    }

    public Page<VersionLog> findPage(Page<VersionLog> page, Parameter parameter) {
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put(VersionLog.FIELD_STATUS, VersionLog.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        page.setResult(dao.findQueryList(parameter));
        return page;
    }

    /**
     * 根据版本号查找
     *
     * @param app
     * @param versionLogType {@link VersionLogType}
     * @param versionCode
     * @return
     */
    public VersionLog getByVersionCode(String app,String versionLogType, String versionCode) {
        VersionLog versionLog = new VersionLog();
        versionLog.setApp(StringUtils.isNotBlank(app) ? app:VersionLog.DEFAULT_ID);
        versionLog.setVersionLogType(versionLogType);
        versionLog.setVersionCode(versionCode);
        return dao.getByVersionCode(versionLog);
    }

    /**
     * 根据版本号查找
     *
     * @param app
     * @param versionLogType {@link VersionLogType}
     * @param versionName
     * @return
     */
    public VersionLog getByVersionName(String app,String versionLogType, String versionName) {
        VersionLog versionLog = new VersionLog();
        versionLog.setApp(StringUtils.isNotBlank(app) ? app:VersionLog.DEFAULT_ID);
        versionLog.setVersionLogType(versionLogType);
        versionLog.setVersionName(versionName);
        return dao.getByVersionName(versionLog);
    }

    /**
     * 获取当前最新版本
     *
     * @param app
     * @param versionLogType
     * @return
     */
    public VersionLog getLatestVersionLog(String app,String versionLogType) {
        return getLatestVersionLog(app,versionLogType, YesOrNo.YES.getValue(),YesOrNo.NO.getValue());
    }

    /**
     * 获取当前最新版本
     *
     * @param app
     * @param versionLogType
     * @param isPub
     * @param isShelf
     * @return
     */
    public VersionLog getLatestVersionLog(String app,String versionLogType,String isPub,String isShelf) {
        VersionLog entity = new VersionLog();
        entity.setApp(StringUtils.isNotBlank(app) ? app:VersionLog.DEFAULT_ID);
        entity.setVersionLogType(versionLogType);
        entity.setIsPub(isPub);
        entity.setIsShelf(isShelf);
        return dao.getLatestVersionLog(entity);
    }
}
