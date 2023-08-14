/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.dao;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.MyBatisDao;
import com.eryansky.common.orm.persistence.CrudDao;

import com.eryansky.modules.sys.mapper.Role;

import java.util.List;

/**
 * 角色表
 *
 * @author Eryan
 * @date 2018-05-08
 */
@MyBatisDao
public interface RoleDao extends CrudDao<Role> {

    List<Role> findRolesByIds(Parameter parameter);

    List<Role> findOrganRolesAndSystemNormalRoles(Parameter parameter);

    Role getByCode(Parameter parameter);

    List<Role> findRolesByOrganId(Parameter parameter);

    List<Role> findRolesByResourceId(Parameter parameter);

    List<Role> findRolesByUserId(Parameter parameter);

    List<String> findRoleIdsByUserId(Parameter parameter);


    /**
     * 自定义SQL查询
     *
     * @param parameter
     * @return
     */
    List<Role> findByWhereSQL(Parameter parameter);

    /**
     * 自定义SQL查询
     *
     * @param parameter
     * @return
     */
    List<Role> findBySql(Parameter parameter);

    /**
     * 删除角色机构关联信息
     *
     * @param parameter id:角色ID
     */
    int deleteRoleOrgansByRoleId(Parameter parameter);

    /**
     * 插入角色机构关联信息
     *
     * @param parameter id:角色ID ids:机构IDS
     */
    int insertRoleOrgans(Parameter parameter);

    /**
     * 角色机构关联信息
     *
     * @param parameter
     * @return
     */
    List<String> findRoleOrganIds(Parameter parameter);



    /**
     * 删除角色机构关联信息（授权数据权限机构）
     *
     * @param parameter id:角色ID
     */
    int deleteRoleDataOrgansByRoleId(Parameter parameter);

    /**
     * 插入角色机构关联信息（授权数据权限机构）
     *
     * @param parameter id:角色ID ids:机构IDS
     */
    int insertRoleDataOrgans(Parameter parameter);

    /**
     * 角色机构关联信息（授权数据权限机构）
     *
     * @param parameter
     * @return
     */
    List<String> findRoleDataOrganIds(Parameter parameter);


    /**
     * 删除角色用户关联信息
     *
     * @param parameter id:角色ID
     */
    int deleteRoleUsersByRoleId(Parameter parameter);

    /**
     * 删除角色用户关联信息
     *
     * @param parameter id:角色ID ids:用户IDS
     */
    int deleteRoleUsersByRoleIdANDUserIds(Parameter parameter);

    /**
     * 插入角色用户关联信息
     *
     * @param parameter id:角色ID ids:用户IDS
     */
    int insertRoleUsers(Parameter parameter);


    /**
     * 删除角色资源关联信息
     *
     * @param parameter id:角色ID
     */
    int deleteRoleResourcesByRoleId(Parameter parameter);


    /**
     * 插入角色资源关联信息
     *
     * @param parameter id:角色ID ids:资源IDS
     */
    int insertRoleResources(Parameter parameter);

    /**
     * 删除角色资源关联信息
     *
     * @param parameter roleId:角色ID resourceId:资源ID
     */
    int deleteRoleResourceByResourceIdAndRoleId(Parameter parameter);
}
