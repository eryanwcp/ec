/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.service;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.core.orm.mybatis.service.BaseService;
import com.eryansky.modules.sys.dao.SystemDao;
import com.eryansky.modules.sys.mapper.Area;
import com.eryansky.modules.sys.mapper.Organ;
import com.eryansky.modules.sys.utils.AreaUtils;
import com.eryansky.modules.sys.utils.OrganUtils;
import com.eryansky.utils.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统任务
 *
 * @author Eryan
 * @date 2017-09-19
 */
@Service
public class SystemService extends BaseService {

    @Autowired
    private SystemDao systemDao;
    @Autowired
    private OrganService organService;


    /**
     * organ表同步到扩展表
     *
     * @return
     */
    public int insertToOrganExtend() {
        return insertToOrganExtend(Parameter.newParameter());
    }

    /**
     * organ表同步到扩展表
     *
     * @param parameter 参数
     * @return
     */
    public int insertToOrganExtend(Parameter parameter) {
        return systemDao.insertToOrganExtend(parameter);
    }

    /**
     * organ表同步到扩展表
     *
     * @return
     */
    public int insertToOrganExtendByFunction() {
        return insertToOrganExtendByFunction(Parameter.newParameter());
    }


    /**
     * organ表同步到扩展表
     *
     * @param parameter 参数
     * @return
     */
    public int insertToOrganExtendByFunction(Parameter parameter) {
        return systemDao.insertToOrganExtendByFunction(parameter);
    }

    /**
     * 删除t_sys_organ_extend扩展表数据
     *
     * @return
     */
    public int deleteOrganExtend() {
        return deleteOrganExtend(Parameter.newParameter());
    }

    /**
     * 删除t_sys_organ_extend扩展表数据
     *
     * @param parameter 参数
     * @return
     */
    public int deleteOrganExtend(Parameter parameter) {
        return systemDao.deleteOrganExtend(parameter);
    }

    /**
     * 同步数据到t_sys_organ_extend表 批量
     */
    public void syncOrganToExtendAuto() {
        String dbType = AppConstants.getJdbcType();
        if ("mysql".equalsIgnoreCase(dbType) || "mariadb".equalsIgnoreCase(dbType)) {
            try {
                Parameter parameter = Parameter.newParameter();
                deleteOrganExtend(parameter);
                insertToOrganExtendByFunction(parameter);//使用存储过程
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                syncOrganToExtend();
            }
        } else {
            syncOrganToExtend();
        }
    }


    /**
     * 同步数据到t_sys_organ_extend表 单个
     * @param organ
     */
    public void syncOrganToExtendAuto(Organ organ) {
        String dbType = AppConstants.getJdbcType();
        if ("mysql".equalsIgnoreCase(dbType) || "mariadb".equalsIgnoreCase(dbType)) {
            try {
                Parameter parameter = Parameter.newParameter();
                parameter.put("id", organ.getId());
                deleteOrganExtend(parameter);
                insertToOrganExtendByFunction(parameter);//使用存储过程
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                syncOrganToExtend(organ);
            }
        } else {
            syncOrganToExtend(organ);
        }
    }

    public void syncOrganToExtend() {
        List<Organ> list = organService.findAllWithDelete();
        list.parallelStream().forEach(this::syncOrganToExtend);
    }

    public void syncOrganToExtend(Organ organ) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", organ.getId());
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        Organ parent = null;
        if(StringUtils.isNotBlank(organ.get_parentId())){
            parent = OrganUtils.getOrgan(organ.get_parentId());
        }

        Area area = AreaUtils.get(organ.getAreaId());
        Organ company = OrganUtils.getCompanyByRecursive(organ.getId());
        Organ homeCompany = OrganUtils.getHomeCompanyByRecursive(organ.getId());
        parameter.put("parentId", null != parent ? parent.getId():null);
        parameter.put("parentCode", null != parent ? parent.getCode():null);
        parameter.put("parentBizCode", null != parent ? parent.getBizCode():null);
        parameter.put("companyId", company.getId());
        parameter.put("companyCode", company.getCode());
        parameter.put("companyBizCode", company.getBizCode());
        parameter.put("homeCompanyId", homeCompany.getId());
        parameter.put("homeCompanyCode", homeCompany.getCode());
        parameter.put("homeCompanyBizCode", homeCompany.getBizCode());
        parameter.put("areaCode", null != area ? area.getCode():null);
        parameter.put("areaBizCode", null != area ? area.getBizCode():null);
        Integer level = StringUtils.isNotBlank(organ.getParentIds()) ? organ.getParentIds().split(",").length : null;
        parameter.put("treeLevel", level);
//        Integer childCount = organService.findChildCount(organ.getId());
//        parameter.put("isLeaf", null == childCount || childCount == 0);
        deleteOrganExtend(parameter);
        insertToOrganExtend(parameter);
    }

}
