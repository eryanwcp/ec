/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.dao;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.MyBatisDao;

import com.eryansky.core.orm.mybatis.dao.TreeDao;
import com.eryansky.modules.sys.mapper.Organ;
import com.eryansky.modules.sys.mapper.OrganExtend;

import java.util.List;

/**
 * 机构表
 *
 * @author Eryan
 * @date 2018-05-08
 */
@MyBatisDao
public interface OrganDao extends TreeDao<Organ> {

    List<Organ> findByIds(Parameter parameter);

    List<Organ> findByCodes(Parameter parameter);

    List<Organ> findBySysCodes(Parameter parameter);

    List<Organ> findCustomQuery(Parameter parameter);

    List<Organ> findWithInclude(Parameter parameter);

    List<Organ> findOwnerAndChild(Parameter parameter);

    List<String> findOwnerAndChildIds(Parameter parameter);

    List<Organ> findOwnerAndChilds(Parameter parameter);

    List<String> findOwnerAndChildsIds(Parameter parameter);

    Integer findChildCount(Parameter parameter);

    List<Organ> findChild(Parameter parameter);


    List<String> findChildIds(Parameter parameter);

    List<Organ> findChilds(Parameter parameter);

    List<String> findChildsIds(Parameter parameter);

    List<Organ> findOrgansByIds(Parameter parameter);

    List<String> findOrganUserIds(Parameter parameter);

    /**
     * 查找用户所属机构
     *
     * @param parameter userId：用户ID
     * @return
     */
    List<Organ> findOrgansByUserId(Parameter parameter);

    /**
     * 查找用户所属机构IDS
     *
     * @param parameter userId：用户ID
     * @return
     */
    List<String> findOrganIdsByUserId(Parameter parameter);


    List<Organ> findAssociationOrgansByPostId(Parameter parameter);

    List<String> findAssociationOrganIdsByPostId(Parameter parameter);


    Integer getMaxSort();

    Organ getByCode(Parameter parameter);

    Organ getByIdOrCode(Parameter parameter);

    Organ getDeleteByIdOrCode(Parameter parameter);


    Organ getBySysCode(Parameter parameter);

    Organ getByBizCode(Parameter parameter);

    int deleteOwnerAndChilds(Organ entity);

//    机构扩展表信息

    OrganExtend getOrganExtendByOrganId(Parameter parameter);

    OrganExtend getOrganExtendByCode(Parameter parameter);

    OrganExtend getOrganExtendByBizCode(Parameter parameter);

    OrganExtend getCompanyByOrganId(Parameter parameter);

    OrganExtend getHomeCompanyByOrganId(Parameter parameter);

    OrganExtend getOrganExtendByUserId(Parameter parameter);

    OrganExtend getOrganExtendByUserLoginName(Parameter parameter);

    OrganExtend getCompanyByUserId(Parameter parameter);

    OrganExtend getHomeCompanyByUserId(Parameter parameter);

    List<OrganExtend> findOrganExtends(Parameter parameter);

    /**
     * 查找机构下直属部门
     *
     * @param parameter
     * @return
     */
    List<OrganExtend> findDepartmentOrganExtendsByCompanyId(Parameter parameter);

    /**
     * 查找机构下直属部门ID
     *
     * @param parameter
     * @return
     */
    List<String> findDepartmentOrganIdsByCompanyId(Parameter parameter);

    /**
     * 查找机构下直属部门以及小组
     *
     * @param parameter
     * @return
     */
    List<OrganExtend> findDepartmentAndGroupOrganExtendsByCompanyId(Parameter parameter);

    /**
     * 查找机构下直属部门以及小组IDS
     *
     * @param parameter
     * @return
     */
    List<String> findDepartmentAndGroupOrganIdsByCompanyId(Parameter parameter);

    /**
     * 自定义SQL查询
     *
     * @param parameter
     * @return
     */
    List<Organ> findByWhereSQL(Parameter parameter);

    /**
     * 自定义SQL查询
     *
     * @param parameter
     * @return
     */
    List<Organ> findBySql(Parameter parameter);
}
