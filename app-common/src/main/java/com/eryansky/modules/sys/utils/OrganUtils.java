/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.utils;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.modules.sys._enum.OrganType;
import com.eryansky.modules.sys.mapper.Area;
import com.eryansky.modules.sys.mapper.Organ;
import com.eryansky.modules.sys.mapper.OrganExtend;
import com.eryansky.modules.sys.service.OrganService;

import java.util.List;

/**
 * @author Eryan
 * @date 2014-11-25
 */
public class OrganUtils {

    private OrganUtils(){}

    /**
     * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
     */
    public static final class Static {
        private static final OrganService organService = SpringContextHolder.getBean(OrganService.class);
        private Static(){}
    }

    /**
     * 根据机构ID查找机构
     *
     * @param organId 机构ID
     * @return
     */
    public static Organ getOrgan(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        return Static.organService.get(organId);
    }

    /**
     * 根据机构编码查找机构
     *
     * @param organCode 机构编码
     * @return
     */
    public static Organ getByOrganCode(String organCode) {
        if (StringUtils.isBlank(organCode)) {
            return null;
        }
        return Static.organService.getByCode(organCode);
    }


    /**
     * 根据机构ID查找
     *
     * @param organId 机构ID
     * @return
     */
    public static OrganExtend getOrganExtend(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        return Static.organService.getOrganExtend(organId);
    }

    /**
     * 根据机构编码查找
     *
     * @param organCode 机构编码
     * @return
     */
    public static OrganExtend getOrganExtendByCode(String organCode) {
        if (StringUtils.isBlank(organCode)) {
            return null;
        }
        return Static.organService.getOrganExtendByCode(organCode);
    }

    /**
     * 根据机构编码查找
     *
     * @param organId 机构ID
     * @return
     */
    public static Integer getOrganLevelByOrganId(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        OrganExtend organExtend = getOrganExtend(organId);

        return null == organExtend ? null : organExtend.getTreeLevel();
    }

    /**
     * 根据机构编码查找
     *
     * @param organCode 机构编码
     * @return
     */
    public static Integer getOrganLevelByOrganCode(String organCode) {
        if (StringUtils.isBlank(organCode)) {
            return null;
        }
        OrganExtend organExtend = getOrganExtendByCode(organCode);

        return null == organExtend ? null : organExtend.getTreeLevel();
    }

    /**
     * 根据机构ID查找
     *
     * @param organId 机构ID
     * @return
     */
    public static OrganExtend getOrganCompany(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        return Static.organService.getOrganCompany(organId);
    }

    /**
     * 根据用户ID查找
     *
     * @param userId 用户ID
     * @return
     */
    public static OrganExtend getOrganExtendByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        return Static.organService.getOrganExtendByUserId(userId);
    }


    /**
     * 根据用户登录账号查找
     *
     * @param loginName 用户账号
     * @return
     */
    public static OrganExtend getOrganExtendByUserLoginName(String loginName) {
        if (StringUtils.isBlank(loginName)) {
            return null;
        }
        return Static.organService.getOrganExtendByUserLoginName(loginName);
    }

    /**
     * 根据用户ID查找
     *
     * @param userId 用户ID
     * @return
     */
    public static OrganExtend getCompanyByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        return Static.organService.getCompanyByUserId(userId);
    }

    /**
     * 根据用户ID查找
     *
     * @param userId 用户ID
     * @return
     */
    public static OrganExtend getHomeCompanyByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        return Static.organService.getHomeCompanyByUserId(userId);
    }


    /**
     * 根据机构ID查找单位ID
     *
     * @param organId 机构ID
     * @return
     */
    public static String getOrganCompanyId(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        OrganExtend organExtend = getOrganCompany(organId);
        if (null != organExtend) {
            return organExtend.getId();
        }
        return null;
    }

    /**
     * 根据机构ID查找单位名称
     *
     * @param organId 机构ID
     * @return
     */
    public static String getOrganCompanyName(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        OrganExtend organExtend = getOrganCompany(organId);
        if (null != organExtend) {
            return organExtend.getName();
        }
        return null;
    }

    /**
     * 根据机构ID查找单位名称(简称)
     *
     * @param organId 机构ID
     * @return
     */
    public static String getOrganCompanyShortName(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        OrganExtend organExtend = getOrganCompany(organId);
        if (null != organExtend) {
            return organExtend.getShortName();
        }
        return null;
    }


    /**
     * 根据机构ID查找机构名称
     *
     * @param organId 机构ID
     * @return
     */
    public static String getOrganName(String organId) {
        Organ organ = getOrgan(organId);
        if (organ != null) {
            return organ.getName();
        }
        return null;
    }


    /**
     * 根据机构ID查找机构类型
     *
     * @param organId 机构ID
     * @return
     */
    public static String getOrganType(String organId) {
        Organ organ = getOrgan(organId);
        if (organ != null) {
            return organ.getType();
        }
        return null;
    }

    /**
     * 根据机构ID查找机构名称(简称)
     *
     * @param organId 机构ID
     * @return
     */
    public static String getOrganShortName(String organId) {
        Organ organ = getOrgan(organId);
        if (organ != null) {
            return organ.getShortName();
        }
        return null;
    }

    public static boolean hasChild(String organId) {
        Integer childCount = Static.organService.findChildCount(organId);
        return null != childCount && childCount != 0;
    }


    public static String getAreaId(String organId) {
        Organ organ = getOrgan(organId);
        if (organ != null) {
            return organ.getAreaId();
        }
        return null;
    }

    public static String getAreaCode(String organCode) {
        Organ organ = getByOrganCode(organCode);
        if (organ != null) {
            Area area = AreaUtils.get(organ.getAreaId());
            return area == null ? null : area.getCode();
        }
        return null;
    }

    public static String getAreaType(String organId) {
        Organ organ = getOrgan(organId);
        if (organ != null) {
            return organ.getType();
        }
        return null;
    }

//    递归方法

    /**
     * 根据机构ID查找单位 (递归)
     *
     * @param organId 机构ID
     * @return
     */
    public static Organ getCompanyByRecursive(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        Organ currentOrgan = getOrgan(organId);
        while (currentOrgan != null && !OrganType.organ.getValue().equals(currentOrgan.getType())) {
            currentOrgan = getOrgan(currentOrgan.getParentId());
        }
        return currentOrgan;
    }


    /**
     * 根据机构ID查找单位ID (递归)
     *
     * @param organId 机构ID
     * @return
     */
    public static String getCompanyIdByRecursive(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        Organ organ = getCompanyByRecursive(organId);
        return null == organ ? null : organ.getId();
    }

    /**
     * 根据机构ID查找单位CODE (递归)
     *
     * @param organId 机构ID
     * @return
     */
    public static String getCompanyCodeByRecursive(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        Organ organ = getCompanyByRecursive(organId);
        return null == organ ? null : organ.getCode();
    }


    /**
     * 根据机构ID查找上级单位(递归)
     *
     * @param organId 机构ID
     * @return
     */
    public static Organ getHomeCompanyByRecursive(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        Organ company = getCompanyByRecursive(organId);
        if (null != company && StringUtils.isNotBlank(company.getParentIds()) && company.getParentIds().split(",").length >= 3) {//区县
            return getCompanyByRecursive(company.getParentId());
        }
        return company;
    }


    /**
     * 根据机构ID查找上级单位ID (递归)
     *
     * @param organId 机构ID
     * @return
     */
    public static String getHomeCompanyIdByRecursive(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        Organ organ = getHomeCompanyByRecursive(organId);
        return null == organ ? null : organ.getId();
    }

    /**
     * 根据机构ID查找上级单位CODE (递归)
     *
     * @param organId 机构ID
     * @return
     */
    public static String getHomeCompanyCodeByRecursive(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        Organ organ = getHomeCompanyByRecursive(organId);
        return null == organ ? null : organ.getCode();
    }
}
