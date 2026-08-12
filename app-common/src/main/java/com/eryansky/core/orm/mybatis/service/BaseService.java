/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.orm.mybatis.service;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.configure.DBConfigurer;
import com.eryansky.core.orm.mybatis.entity.BaseEntity;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.modules.sys._enum.DataScope;
import com.eryansky.modules.sys.mapper.OrganExtend;
import com.eryansky.modules.sys.mapper.Role;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.utils.OrganUtils;
import com.eryansky.modules.sys.utils.RoleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Service基类
 * @author Eryan
 * @version 2014-05-16
 */
public abstract class BaseService {

    /**
     * 日志对象
     */
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 数据范围过滤
     * @param user 当前用户对象，通过“entity.getCurrentUser()”获取
     * @param officeAlias 机构表别名，多个用“,”逗号隔开。
     * @param userAlias 用户表别名，多个用“,”逗号隔开，传递空，忽略此参数
     * @return 标准连接条件对象
     */
    public static String dataScopeFilter(User user, String officeAlias, String userAlias) {
        // 如果是超级管理员，则不过滤数据
        if (user == null || user.getId().equals(User.SUPERUSER_ID)) {
            return "";
        }

        String sysPrefix = DBConfigurer.getMybatisProperty("sysPrefix");
        DataScopeContext ctx = new DataScopeContext(user);
        List<String> fragments = new ArrayList<>();
        boolean isDataScopeAll = false;

        for (Role role : ctx.getRoles()) {
            if (StringUtils.isBlank(role.getDataScope())) {
                continue;
            }

            String scope = role.getDataScope();
            if (DataScope.ALL.getValue().equals(scope)) {
                isDataScopeAll = true;
                break;
            }

            for (String oa : StringUtils.split(officeAlias, ",")) {
                if (StringUtils.isNotBlank(oa)) {
                    String fragment = buildDirectScopeSql(scope, ctx, oa, sysPrefix, role);
                    if (fragment != null) {
                        fragments.add(fragment);
                    }
                }
            }
        }

        if (isDataScopeAll) {
            return "";
        }

        if (!fragments.isEmpty()) {
            return " AND (" + String.join(" OR ", fragments) + ")";
        } else if (StringUtils.isNotBlank(userAlias)) {
            List<String> userFragments = new ArrayList<>();
            for (String ua : StringUtils.split(userAlias, ",")) {
                userFragments.add(ua + ".id = '" + user.getId() + "'");
            }
            return " AND (" + String.join(" OR ", userFragments) + ")";
        } else {
            List<String> fallbackFragments = new ArrayList<>();
            for (String oa : StringUtils.split(officeAlias, ",")) {
                fallbackFragments.add(oa + ".id IS NULL");
            }
            return " AND (" + String.join(" OR ", fallbackFragments) + ")";
        }
    }

    /**
     * 数据范围过滤（符合业务表字段不同的时候使用，采用exists方法）
     * @param entity 当前过滤的实体类
     * @param sqlMapKey sqlMap的键值，例如设置“dsf”时，调用方法：${sqlMap.sdf}
     * @param officeWheres office表条件，组成：部门表字段=业务表的部门字段
     * @param userWheres user表条件，组成：用户表字段=业务表的用户字段
     * @example
     * 		dataScopeFilter(user, "dsf", "id=a.office_id", "id=a.cjr"); // 适应于业务表关联不同字段时使用，如果关联的不是机构id是code。
     */
    public static void dataScopeFilter(BaseEntity<?> entity, String sqlMapKey, String officeWheres, String userWheres) {
        User user = SecurityUtils.getCurrentUser();
        // 如果是超级管理员，则不过滤数据
        if (user == null || user.getId().equals(User.SUPERUSER_ID)) {
            return;
        }

        DataScopeContext ctx = new DataScopeContext(user);
        String sysPrefix = DBConfigurer.getMybatisProperty("sysPrefix");

        String effectiveScope = null;
        Role effectiveRole = null;
        int minScopeValue = Integer.MAX_VALUE;

        for (Role r : ctx.getRoles()) {
            if (StringUtils.isBlank(r.getDataScope())) {
                continue;
            }
            int ds = Integer.valueOf(r.getDataScope());
            if (ds == Integer.valueOf(DataScope.CUSTOM.getValue())) {
                effectiveScope = r.getDataScope();
                effectiveRole = r;
                minScopeValue = ds;
                break;
            } else if (ds < minScopeValue) {
                effectiveScope = r.getDataScope();
                effectiveRole = r;
                minScopeValue = ds;
            }
        }

        if (effectiveScope == null) {
            effectiveScope = DataScope.SELF.getValue();
        }

        StringBuilder sqlString = new StringBuilder();

        for (String where : StringUtils.split(officeWheres, ",")) {
            String fragment = buildExistsScopeSql(effectiveScope, ctx, where, sysPrefix, effectiveRole);
            if (fragment != null) {
                sqlString.append(" ").append(fragment);
            }
        }

        for (String where : StringUtils.split(userWheres, ",")) {
            if (DataScope.SELF.getValue().equals(effectiveScope)) {
                sqlString.append(" AND EXISTS (SELECT 1 FROM ").append(sysPrefix).append("t_sys_user WHERE id='").append(user.getId()).append("' AND ").append(where).append(")");
            }
        }

        entity.getSqlMap().put(sqlMapKey, sqlString.toString());
    }

    private static String buildDirectScopeSql(String scope, DataScopeContext ctx, String alias, String sysPrefix, Role role) {
        if (DataScope.HOME_COMPANY_AND_CHILD.getValue().equals(scope)) {
            OrganExtend company = ctx.getHomeCompany();
            return alias + ".id = '" + company.getId() + "' OR " + alias + ".parent_ids LIKE '" + company.getParentIds() + company.getId() + ",%'";
        } else if (DataScope.HOME_COMPANY.getValue().equals(scope)) {
            OrganExtend company = ctx.getHomeCompany();
            return alias + ".home_company_id = '" + company.getId() + "'";
        } else if (DataScope.COMPANY_AND_CHILD.getValue().equals(scope)) {
            OrganExtend company = ctx.getCompany();
            return alias + ".id = '" + company.getId() + "' OR " + alias + ".parent_ids LIKE '" + company.getParentIds() + company.getId() + ",%'";
        } else if (DataScope.COMPANY.getValue().equals(scope)) {
            OrganExtend company = ctx.getCompany();
            return alias + ".company_id = '" + company.getId() + "'";
        } else if (DataScope.OFFICE_AND_CHILD.getValue().equals(scope)) {
            OrganExtend organ = ctx.getOffice();
            return alias + ".id = '" + organ.getId() + "' OR " + alias + ".parent_ids LIKE '" + organ.getParentIds() + organ.getId() + ",%'";
        } else if (DataScope.OFFICE.getValue().equals(scope)) {
            OrganExtend organ = ctx.getOffice();
            return alias + ".id = '" + organ.getId() + "'";
        } else if (DataScope.CUSTOM.getValue().equals(scope)) {
            return "EXISTS (SELECT 1 FROM " + sysPrefix + "t_sys_role_data_organ WHERE role_id = '" + role.getId() + "' AND organ_id = " + alias + ".id)";
        }
        return null;
    }

    private static String buildExistsScopeSql(String scope, DataScopeContext ctx, String where, String sysPrefix, Role role) {
        if (DataScope.HOME_COMPANY_AND_CHILD.getValue().equals(scope)) {
            OrganExtend company = ctx.getHomeCompany();
            return " AND EXISTS (SELECT 1 FROM " + sysPrefix + "t_sys_organ WHERE (id = '" + company.getId() + "' OR parent_ids LIKE '" + company.getParentIds() + company.getId() + ",%') AND " + where + ")";
        } else if (DataScope.HOME_COMPANY.getValue().equals(scope)) {
            OrganExtend company = ctx.getHomeCompany();
            return " AND EXISTS (SELECT 1 FROM " + sysPrefix + "t_sys_organ_extend WHERE home_company_id = '" + company.getId() + "' AND " + where + ")";
        } else if (DataScope.COMPANY_AND_CHILD.getValue().equals(scope)) {
            OrganExtend company = ctx.getCompany();
            return " AND EXISTS (SELECT 1 FROM " + sysPrefix + "t_sys_organ WHERE (id = '" + company.getId() + "' OR parent_ids LIKE '" + company.getParentIds() + company.getId() + ",%') AND " + where + ")";
        } else if (DataScope.COMPANY.getValue().equals(scope)) {
            OrganExtend company = ctx.getCompany();
            return " AND EXISTS (SELECT 1 FROM " + sysPrefix + "t_sys_organ_extend WHERE company_id = '" + company.getId() + "' AND " + where + ")";
        } else if (DataScope.OFFICE_AND_CHILD.getValue().equals(scope)) {
            OrganExtend organ = ctx.getOffice();
            return " AND EXISTS (SELECT 1 FROM " + sysPrefix + "t_sys_organ WHERE (id = '" + organ.getId() + "' OR parent_ids LIKE '" + organ.getParentIds() + organ.getId() + ",%') AND " + where + ")";
        } else if (DataScope.OFFICE.getValue().equals(scope)) {
            OrganExtend organ = ctx.getOffice();
            return " AND EXISTS (SELECT 1 FROM " + sysPrefix + "t_sys_organ WHERE id = '" + organ.getId() + "' AND " + where + ")";
        } else if (DataScope.CUSTOM.getValue().equals(scope)) {
            return " AND EXISTS (SELECT 1 FROM " + sysPrefix + "t_sys_role_data_organ ro123456, " + sysPrefix + "t_sys_organ o123456 WHERE ro123456.organ_id = o123456.id AND ro123456.role_id = '" + role.getId() + "' AND o123456." + where + ")";
        }
        return null;
    }

    /**
     * 数据范围上下文，预取权限相关数据，避免在循环中重复调用工具类
     */
    private static class DataScopeContext {
        private final User user;
        private List<Role> roles;
        private OrganExtend homeCompany;
        private OrganExtend company;
        private OrganExtend office;

        DataScopeContext(User user) {
            this.user = user;
        }

        List<Role> getRoles() {
            if (roles == null) {
                roles = RoleUtils.findRolesByUserId(user.getId());
            }
            return roles;
        }

        OrganExtend getHomeCompany() {
            if (homeCompany == null) {
                homeCompany = OrganUtils.getHomeCompanyByUserId(user.getId());
            }
            return homeCompany;
        }

        OrganExtend getCompany() {
            if (company == null) {
                company = OrganUtils.getCompanyByUserId(user.getId());
            }
            return company;
        }

        OrganExtend getOffice() {
            if (office == null) {
                office = OrganUtils.getOrganExtendByUserId(user.getId());
            }
            return office;
        }
    }
}
