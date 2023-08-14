/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.service;

import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.exception.SystemException;
import com.eryansky.common.model.Result;
import com.eryansky.common.model.TreeNode;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.utils.encode.Encryption;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.core.security.SecurityType;
import com.eryansky.modules.sys._enum.SexType;
import com.eryansky.modules.sys._enum.UserType;
import com.eryansky.modules.sys.mapper.Organ;
import com.eryansky.modules.sys.mapper.OrganExtend;
import com.eryansky.modules.sys.utils.OrganUtils;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.CacheConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.Validate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.dao.UserDao;
import com.eryansky.core.orm.mybatis.service.CrudService;
import org.springframework.util.Assert;

import java.util.*;

import static com.eryansky.modules.sys.service.OrganService.*;

/**
 * 用户表 service
 *
 * @author Eryan
 * @date 2018-05-08
 */
@Service
public class UserService extends CrudService<UserDao, User> {

    /**
     * 清空缓存 非Manager调用
     */
    @CacheEvict(value = {CacheConstants.ROLE_ALL_CACHE,
            CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE,
            CacheConstants.RESOURCE_USER_MENU_TREE_CACHE,
            CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE,
            CacheConstants.ORGAN_USER_TREE_1_CACHE,
            CacheConstants.ORGAN_USER_TREE_2_CACHE,
            CacheConstants.CACHE_OrganDao,
            CacheConstants.CACHE_UserDao,
            CacheConstants.CACHE_ResourceDao}, allEntries = true)
    public void clearCache() {
        logger.debug("清空缓存:{}", CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE
                + "," + CacheConstants.RESOURCE_USER_MENU_TREE_CACHE
                + "," + CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE
                + "," + CacheConstants.ORGAN_USER_TREE_1_CACHE
                + "," + CacheConstants.ORGAN_USER_TREE_2_CACHE
                + "," + CacheConstants.CACHE_OrganDao
                + "," + CacheConstants.CACHE_UserDao
                + "," + CacheConstants.CACHE_ResourceDao);
    }

    /**
     * 新增或修改角色.
     * <br>修改角色的时候 会给角色重新授权菜单 更新导航菜单缓存.
     */
    @CacheEvict(value = {CacheConstants.ROLE_ALL_CACHE,
            CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE,
            CacheConstants.RESOURCE_USER_MENU_TREE_CACHE,
            CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE,
            CacheConstants.ORGAN_USER_TREE_1_CACHE,
            CacheConstants.ORGAN_USER_TREE_2_CACHE,
            CacheConstants.CACHE_OrganDao,
            CacheConstants.CACHE_UserDao,
            CacheConstants.CACHE_ResourceDao}, allEntries = true)
    public void saveUser(User entity) {
        logger.debug("清空缓存:{}", CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE
                + "," + CacheConstants.RESOURCE_USER_MENU_TREE_CACHE
                + "," + CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE
                + "," + CacheConstants.ORGAN_USER_TREE_1_CACHE
                + "," + CacheConstants.ORGAN_USER_TREE_2_CACHE
                + "," + CacheConstants.CACHE_OrganDao
                + "," + CacheConstants.CACHE_UserDao
                + "," + CacheConstants.CACHE_ResourceDao);
        super.save(entity);
        //保存用户机构信息
//        List<String> oldOrganIds = organService.findOrganIdsByUserId(entity.getId());
        Set<String> organIds = Sets.newHashSet();
        if (StringUtils.isNotBlank(entity.getDefaultOrganId())) {
            organIds.add(entity.getDefaultOrganId());
        }
        saveUserOrgans(entity.getId(), organIds);
    }

    /**
     * 密码初始化（AOP切面拦截）
     * @param id 用户ID
     * @param password 密码 {@link Encrypt#e(String)}
     * @param originalPassword 原始密码 {@link Encryption#encrypt(String)}
     * @return
     */
    public User updatePasswordFirstByUserId(String id, String password, String originalPassword){
        return updatePassword(id,null,password,originalPassword);
    }


    /**
     * 修改密码（AOP切面拦截）
     * @param id 用户ID
     * @param password 密码 {@link Encrypt#e(String)}
     * @param originalPassword 原始密码 {@link Encryption#encrypt(String)}
     * @return
     */
    public User updatePasswordByUserId(String id, String password, String originalPassword){
        return updatePassword(id,null,password,originalPassword);
    }

    /**
     * 修改密码（AOP切面拦截）
     * @param loginName 账号
     * @param password 密码 {@link Encrypt#e(String)}
     * @param originalPassword 原始密码 {@link Encryption#encrypt(String)}
     * @return
     */
    public User updatePasswordByLoginName(String loginName, String password, String originalPassword){
        return updatePassword(null,loginName,password,originalPassword);
    }

    /**
     * 修改密码
     * @param id 用户ID
     * @param loginName 账号
     * @param password 密码 {@link Encrypt#e(String)}
     * @param originalPassword 原始密码 {@link Encryption#encrypt(String)}
     * @return
     */
    public User updatePassword(String id,String loginName,String password,String originalPassword){
        User entity = new User(id);
        entity.setLoginName(loginName);
        entity.setPassword(password);
        entity.setOriginalPassword(originalPassword);
        entity.preUpdate();
        dao.updatePassword(entity);
        return null != id ? get(id):getUserByLoginName(loginName);
    }

    /**
     * 新增或修改角色.
     * <br>修改角色的时候 会给角色重新授权菜单 更新导航菜单缓存.
     */
    @CacheEvict(value = {CacheConstants.ROLE_ALL_CACHE,
            CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE,
            CacheConstants.RESOURCE_USER_MENU_TREE_CACHE,
            CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE,
            CacheConstants.ORGAN_USER_TREE_1_CACHE,
            CacheConstants.ORGAN_USER_TREE_2_CACHE,
            CacheConstants.CACHE_OrganDao,
            CacheConstants.CACHE_UserDao,
            CacheConstants.CACHE_ResourceDao}, allEntries = true)
    public void save(User entity) {
        logger.debug("清空缓存:{}", CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE
                + "," + CacheConstants.RESOURCE_USER_MENU_TREE_CACHE
                + "," + CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE
                + "," + CacheConstants.ORGAN_USER_TREE_1_CACHE
                + "," + CacheConstants.ORGAN_USER_TREE_2_CACHE
                + "," + CacheConstants.CACHE_OrganDao
                + "," + CacheConstants.CACHE_UserDao
                + "," + CacheConstants.CACHE_ResourceDao);
        super.save(entity);
    }


    /**
     * 自定义删除方法.
     */
    @CacheEvict(value = {CacheConstants.ROLE_ALL_CACHE,
            CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE,
            CacheConstants.RESOURCE_USER_MENU_TREE_CACHE,
            CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE,
            CacheConstants.ORGAN_USER_TREE_1_CACHE,
            CacheConstants.ORGAN_USER_TREE_2_CACHE,
            CacheConstants.CACHE_OrganDao,
            CacheConstants.CACHE_UserDao,
            CacheConstants.CACHE_ResourceDao}, allEntries = true)
    public void deleteByIds(Collection<String> ids) {
        if (Collections3.isNotEmpty(ids)) {
            for (String id : ids) {
                deleteById(id);
            }
        } else {
            logger.warn("参数[ids]为空.");
        }
    }

    @CacheEvict(value = {CacheConstants.ROLE_ALL_CACHE,
            CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE,
            CacheConstants.RESOURCE_USER_MENU_TREE_CACHE,
            CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE,
            CacheConstants.ORGAN_USER_TREE_1_CACHE,
            CacheConstants.ORGAN_USER_TREE_2_CACHE,
            CacheConstants.CACHE_OrganDao,
            CacheConstants.CACHE_UserDao,
            CacheConstants.CACHE_ResourceDao}, allEntries = true)
    public void deleteById(String id) {
        logger.debug("清空缓存:{}", CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE
                + "," + CacheConstants.RESOURCE_USER_MENU_TREE_CACHE
                + "," + CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE
                + "," + CacheConstants.ORGAN_USER_TREE_1_CACHE
                + "," + CacheConstants.ORGAN_USER_TREE_2_CACHE
                + "," + CacheConstants.CACHE_OrganDao
                + "," + CacheConstants.CACHE_UserDao
                + "," + CacheConstants.CACHE_ResourceDao);
        if (isSuperUser(id)) {
            throw new SystemException("不允许删除超级用户!");
        }
        dao.delete(new User(id));
    }

    /**
     * 得到超级用户.
     *
     * @return
     */
    public User getSuperUser() {
        User superUser = dao.get(User.SUPERUSER_ID);//超级用户ID为1
        if (superUser == null) {
            throw new SystemException("系统未设置超级用户.");
        }
        return superUser;
    }


    /**
     * 判断用户是否是超级用户
     *
     * @param userId 用户Id
     * @return
     */
    public boolean isSuperUser(String userId) {
        User superUser = getSuperUser();
        return userId.equals(superUser.getId());
    }


    /**
     * 根据登录名、密码查找用户.
     * <br/>排除已删除的用户
     *
     * @param loginName 登录名
     * @param password  密码
     * @return
     */
    @SuppressWarnings("unchecked")
    public User getUserByLP(String loginName, String password) {
        return getUserByLP(loginName,password,null);
    }

    /**
     * 根据登录名、密码查找用户.
     * <br/>排除已删除的用户
     *
     * @param loginName 登录名
     * @param password  密码
     * @return
     */
    @SuppressWarnings("unchecked")
    public User getUserByLP(String loginName, String password,String securityToken) {
        Assert.notNull(loginName, "参数[loginName]为空!");
        Assert.notNull(password, "参数[password]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("loginName", loginName);
        parameter.put("password", password);
        parameter.put("securityToken", securityToken);
        List<User> list = dao.findLoginUser(parameter);
        return list.isEmpty() ? null : list.get(0);
    }


    /**
     * 根据手机号和密码验证
     *
     * @param mobile   电话号码
     * @param password 密码
     * @return
     */
    public User getUserByMP(String mobile, String password) {
        Assert.notNull(mobile, "参数[mobile]为空!");
        Assert.notNull(password, "参数[password]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("loginName", mobile);
        parameter.put("password", password);
        List<User> list = dao.findLoginUser(parameter);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 根据登录名查找.
     * <br>注：排除已删除的对象
     *
     * @param loginName 登录名
     * @return
     */
    @SuppressWarnings("unchecked")
    public User getUserByLoginName(String loginName) {
        return getUserByLoginName(loginName, DataEntity.STATUS_NORMAL);
    }

    /**
     * 根据登录名查找.
     * <br>注：排除已删除的对象
     *
     * @param loginName 登录名
     * @param status
     * @return
     */
    @SuppressWarnings("unchecked")
    public User getUserByLoginName(String loginName, String status) {
        Assert.notNull(loginName, "参数[loginName]不能为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, status);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("loginName", loginName);
        return dao.getUserByLoginName(parameter);
    }


    /**
     * 根据ID或登录名名查找.
     * <br>注：排除已删除的对象
     *
     * @param idOrLoginName ID或登录名
     * @return
     */
    @SuppressWarnings("unchecked")
    public User getUserByIdOrLoginName(String idOrLoginName) {
        return getUserByIdOrLoginName(idOrLoginName, DataEntity.STATUS_NORMAL);
    }

    /**
     * 根据ID或登录名查找.
     * <br>注：排除已删除的对象
     *
     * @param idOrLoginName ID或登录名
     * @param status
     * @return
     */
    @SuppressWarnings("unchecked")
    public User getUserByIdOrLoginName(String idOrLoginName, String status) {
        Assert.notNull(idOrLoginName, "参数[idOrLoginName]不能为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, status);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("idOrLoginName", idOrLoginName);
        return dao.getUserByIdOrLoginName(parameter);
    }

    /**
     * 根据登录名或手机号查找.
     *
     * @param loginNameOrMobile 登录名或手机号
     * @return
     */
    @SuppressWarnings("unchecked")
    public User getUserByLoginNameOrMobile(String loginNameOrMobile) {
        return getUserByLoginNameOrMobile(loginNameOrMobile, DataEntity.STATUS_NORMAL);
    }


    /**
     * 根据登录名或手机号查找.
     * <br>注：排除已删除的对象
     *
     * @param loginNameOrMobile 登录名或手机号
     * @param status
     * @return
     */
    @SuppressWarnings("unchecked")
    public User getUserByLoginNameOrMobile(String loginNameOrMobile, String status) {
        Assert.notNull(loginNameOrMobile, "参数[loginNameOrMobile]不能为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, status);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("loginNameOrMobile", loginNameOrMobile);
        return dao.getUserByLoginNameOrMobile(parameter);
    }

    /**
     * 根据登录名或手机号查找.
     *
     * @param loginName 登录名
     * @param mobile 手机号
     * @return
     */
    public User getUserByLoginNameOrMobileOther(String loginName,String mobile) {
        List<User> list =  findByLoginNameOrMobile(loginName, mobile);
        return list.isEmpty() ? null : list.get(0);
    }


    /**
     * 根据ID或手机号名名查找.
     * <br>注：排除已删除的对象
     *
     * @param idOrMobile ID或手机号
     * @return
     */
    @SuppressWarnings("unchecked")
    public User getUserByIdOrMobile(String idOrMobile) {
        return getUserByIdOrMobile(idOrMobile, DataEntity.STATUS_NORMAL);
    }

    /**
     * 根据ID或手机号查找.
     * <br>注：排除已删除的对象
     *
     * @param idOrMobile ID或手机号
     * @param status
     * @return
     */
    @SuppressWarnings("unchecked")
    public User getUserByIdOrMobile(String idOrMobile, String status) {
        Assert.notNull(idOrMobile, "参数[idOrMobile]不能为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, status);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("idOrMobile", idOrMobile);
        return dao.getUserByIdOrMobile(parameter);
    }

    /**
     * 根据编号查找.
     * <br>注：排除已删除的对象
     *
     * @param code 编号
     * @return
     */
    public List<User> findByCode(String code) {
        return findByCode(code, DataEntity.STATUS_NORMAL);
    }

    /**
     * 根据编号查找.
     * <br>注：排除已删除的对象
     *
     * @param code 编号
     * @param status
     * @return
     */
    public List<User> findByCode(String code, String status) {
        Assert.notNull(code, "参数[code]不能为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, status);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("code", code);
        return dao.findByCode(parameter);
    }

    /**
     * 根据编号查找.
     * <br>注：排除已删除的对象
     *
     * @param name 名称
     * @return
     */
    public List<User> findByName(String name) {
        return findByName(name, DataEntity.STATUS_NORMAL,null,null);
    }

    /**
     * 根据编号查找.
     * <br>注：排除已删除的对象
     *
     * @param name 编号
     * @param status
     * @return
     */
    public List<User> findByName(String name, String status,String companyId,String homeCompanyId) {
        Assert.notNull(name, "参数[name]不能为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, status);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("name", name);
        parameter.put("companyId", companyId);
        parameter.put("homeCompanyId", homeCompanyId);
        return dao.findByName(parameter);
    }

    /**
     * 根据账号或编号查找.
     *
     * @param loginName 账号
     * @param code 编码
     * @return
     */
    public List<User> findByLoginNameOrCode(String loginName, String code) {
        Assert.notNull(loginName, "参数[loginName]不能为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("loginName", loginName);
        parameter.put("code", code);
        return dao.findByLoginNameOrCode(parameter);
    }

    /**
     * 根据账号或手机号查找.
     *
     * @param loginName 账号
     * @param mobile 手机号
     * @return
     */
    public List<User> findByLoginNameOrMobile(String loginName, String mobile) {
        Assert.notNull(loginName, "参数[loginName]不能为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("loginName", loginName);
        parameter.put("mobile", mobile);
        return dao.findByLoginNameOrMobile(parameter);
    }


    /**
     * 根据账号、员工编号或手机号查找.
     *
     * @param loginName 账号
     * @param code 员工编号
     * @param mobile 手机号
     * @return
     */
    public List<User> findByLoginNameOrCodeOrMobile(String loginName,String code, String mobile) {
        Assert.notNull(loginName, "参数[loginName]不能为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("loginName", loginName);
        parameter.put("code", code);
        parameter.put("mobile", mobile);
        return dao.findByLoginNameOrCodeOrMobile(parameter);
    }


    /**
     * 根据密码查找.
     *
     * @param passwords 账号
     * @return
     */
    public List<User> findByPasswords(Collection<String> passwords) {
        Assert.notEmpty(passwords, "参数[passwords]不能为空!");
        Parameter parameter = new Parameter();
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("passwords", passwords);
        return dao.findByPasswords(parameter);
    }

    /**
     * 根据手机号查找.
     * <br>注：排除已删除的对象
     *
     * @param mobile 手机号
     * @return
     */
    @SuppressWarnings("unchecked")
    public User getUserByMobile(String mobile) {
        return getUserByMobile(mobile, DataEntity.STATUS_NORMAL);
    }

    /**
     * 根据手机号查找.
     * <br>注：排除已删除的对象
     *
     * @param mobile 手机号
     * @param status
     * @return
     */
    @SuppressWarnings("unchecked")
    public User getUserByMobile(String mobile, String status) {
        Assert.notNull(mobile, "参数[mobile]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, status);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("mobile", mobile);
        return dao.getUserByMobile(parameter);
    }


    /**
     * 获得所有统一用户（包括已删除）
     *
     * @return
     */
    public List<User> findAllPlatform() {
        return findAll(null, UserType.Platform.getValue());
    }

    /**
     * 获得所有统一用户（正常）
     *
     * @return
     */
    public List<User> findAllNormalPlatform() {
        return findAll(DataEntity.STATUS_NORMAL, UserType.Platform.getValue());
    }

    /**
     * 获得所有用户（正常）
     *
     * @return
     */
    public List<User> findAll() {
        return findAll(DataEntity.STATUS_NORMAL, null);
    }

    /**
     * 获得所有用户（正常）
     *
     * @return
     */
    public List<User> findAllNormal() {
        return findAll(DataEntity.STATUS_NORMAL, null);
    }

    /**
     * 获得所有用户（包括已删除）
     *
     * @return
     */
    public List<User> findAllWidthDelete() {
        return findAll(null, null);
    }


    public List<User> findAll(String status, String userType) {
        User entity = new User();
        entity.setStatus(status);
        entity.setUserType(userType);
        return dao.findAllList(entity);
    }


    public List<String> findAllNormalUserIds() {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        return dao.findAllNormalUserIds(parameter);
    }


    public List<User> findAllNormalWithExclude(List<String> userIds) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("userIds", userIds);
        return dao.findAllNormalWithExclude(parameter);
    }

    /**
     * @param userIds 必须包含的用户
     * @param query   查询条件
     * @return
     */
    public List<User> findWithInclude(Collection<String> userIds, String query) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("userIds", userIds);
        parameter.put("query", query);
        return dao.findWithInclude(parameter);

    }

    /**
     * @param page
     * @param organId  机构Id
     * @param query    关键字
     * @param userType 用户类型
     * @return
     */
    public Page<User> findPage(Page<User> page, String organId, String query, String userType) {
        Parameter parameter = new Parameter();
//        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put("organId", organId);
        parameter.put("query", query);
        parameter.put("userType", userType);
        return page.setResult(dao.findQuery(parameter));
    }


    /**
     * 根据机构查询用户信息
     *
     * @param page           机构Id
     * @param organId        机构Id
     * @param query          关键字
     * @param excludeUserIds 排除的用户IDS
     * @return
     */
    public Page<User> findUserPageByOrgan(Page<User> page, String organId, String query, Collection<String> excludeUserIds) {
        if (StringUtils.isBlank(organId)) {
            return page;
        }
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put("organId", organId);
        parameter.put("query", query);
        parameter.put("excludeUserIds", excludeUserIds);
        return page.setResult(dao.findUsersByOrgan(parameter));
    }


    /**
     * 根据机构查询用户信息
     *
     * @param organId        机构Id
     * @param query          关键字
     * @param excludeUserIds 排除的用户IDS
     * @return
     */
    public List<User> findUserListByOrgan(String organId, String query, Collection<String> excludeUserIds) {
        if (StringUtils.isBlank(organId)) {
            return Collections.emptyList();
        }
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("organId", organId);
        parameter.put("query", query);
        parameter.put("excludeUserIds", excludeUserIds);
        return dao.findUsersByOrgan(parameter);
    }

    /**
     * 获取机构用户
     *
     * @param organId
     * @return
     */
    public List<User> findOrganUsers(String organId) {
        Assert.notNull(organId, "参数[organId]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("organId", organId);
        return dao.findOrganUsersByOrganId(parameter);
    }

    /**
     * 获取机构用户
     *
     * @param organCode
     * @return
     */
    public List<User> findOrganUsersByOrganCode(String organCode) {
        Assert.notNull(organCode, "参数[organCode]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("organCode", organCode);
        return dao.findOrganUsersByOrganCode(parameter);
    }

    /**
     * 获取机构用户数
     *
     * @param organId
     * @return
     */
    public Integer findOrganUserCount(String organId) {
        Assert.notNull(organId, "参数[organId]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("organId", organId);
        return dao.findOrganUserCount(parameter);
    }

    /**
     * 获取机构用户IDS
     *
     * @param organId
     * @return
     */
    public List<String> findOrganUserIds(String organId) {
        Assert.notNull(organId, "参数[organId]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("organId", organId);
        return dao.findOrganUserIds(parameter);
    }


    /**
     * 获取机构用户
     *
     * @param organId
     * @return
     */
    public List<User> findOrganDefaultUsers(String organId) {
        Assert.notNull(organId, "参数[organId]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("organId", organId);
        return dao.findOrganDefaultUsers(parameter);
    }


    /**
     * 获取机构用户IDS
     *
     * @param organId
     * @return
     */
    public List<String> findOrganDefaultUserIds(String organId) {
        Assert.notNull(organId, "参数[organId]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("organId", organId);
        return dao.findOrganDefaultUserIds(parameter);
    }

    /**
     * 获取单位下直属部门用户
     *
     * @param companyId 单位ID
     * @return
     */
    public List<User> findUsersByCompanyId(String companyId) {
        return findUsersByCompanyId(companyId, null);
    }


    /**
     * 获取单位下直属部门用户
     *
     * @param companyId      单位ID
     * @param excludeUserIds 排除的用户IDS
     * @return
     */
    public List<User> findUsersByCompanyId(String companyId, Collection<String> excludeUserIds) {
        Assert.notNull(companyId, "参数[companyId]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("companyId", companyId);
        parameter.put("excludeUserIds", excludeUserIds);
        return dao.findUsersByCompanyId(parameter);
    }

    /**
     * 获取单位下直属部门用户IDS
     *
     * @param companyId 单位ID
     * @return
     */
    public List<String> findUserIdsByCompanyId(String companyId) {
        return findUserIdsByCompanyId(companyId, null);
    }

    /**
     * 获取单位下直属部门用户IDS
     *
     * @param companyId      单位ID
     * @param excludeUserIds 排除的用户IDS
     * @return
     */
    public List<String> findUserIdsByCompanyId(String companyId, Collection<String> excludeUserIds) {
        Assert.notNull(companyId, "参数[companyId]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("companyId", companyId);
        parameter.put("excludeUserIds", excludeUserIds);
        return dao.findUserIdsByCompanyId(parameter);
    }

    /**
     * 获取单位下直属部门用户
     *
     * @param homeCompanyId      单位ID
     * @param excludeUserIds 排除的用户IDS
     * @return
     */
    public List<User> findUsersByHomeCompanyId(String homeCompanyId, Collection<String> excludeUserIds) {
        Assert.notNull(homeCompanyId, "参数[homeCompanyId]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("homeCompanyId", homeCompanyId);
        parameter.put("excludeUserIds", excludeUserIds);
        return dao.findUsersByHomeCompanyId(parameter);
    }

    /**
     * 获取单位下直属部门用户IDS
     *
     * @param homeCompanyId 单位ID
     * @return
     */
    public List<String> findUserIdsByHomeCompanyId(String homeCompanyId) {
        return findUserIdsByHomeCompanyId(homeCompanyId, null);
    }


    /**
     * 获取单位下直属部门用户IDS
     *
     * @param homeCompanyId      单位ID
     * @param excludeUserIds 排除的用户IDS
     * @return
     */
    public List<String> findUserIdsByHomeCompanyId(String homeCompanyId, Collection<String> excludeUserIds) {
        Assert.notNull(homeCompanyId, "参数[homeCompanyId]为空!");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("homeCompanyId", homeCompanyId);
        parameter.put("excludeUserIds", excludeUserIds);
        return dao.findUserIdsByHomeCompanyId(parameter);
    }

    /**
     * 得到排序字段的最大值.
     *
     * @return 返回排序字段的最大值
     */
    public Integer getMaxSort() {
        Integer max = dao.getMaxSort();
        return max == null ? 0 : max;
    }

    /**
     * 批量更新用户 机构信息
     *
     * @param userIds        用户Id集合
     * @param organIds       所所机构ID集合
     * @param defaultOrganId 默认机构
     */
    public void updateUserOrgan(Collection<String> userIds, Collection<String> organIds, String defaultOrganId) {
        if (Collections3.isNotEmpty(userIds)) {
            for (String userId : userIds) {
                User model = this.get(userId);
                if (model == null) {
                    throw new ServiceException("用户[" + userId + "]不存在.");
                }
                //保存用户机构信息
                saveUserOrgans(userId, organIds);
                //去除用户已删除机构下的岗位关联信息
                int d = deleteNotInUserOrgansPostsByUserId(userId, organIds);
                if(d != 0){
                    logger.warn("删除用户不在部门的岗位数据：{} {} {}",d,userId, defaultOrganId);
                }
                //设置默认部门
                model.setDefaultOrganId(defaultOrganId);
                this.save(model);
            }
        }
    }


    /**
     * 设置用户岗位 批量
     *
     * @param userIds 用户ID集合
     * @param roleIds 角色ID集合
     */
    public void updateUserRole(Collection<String> userIds, Collection<String> roleIds) {
        if (Collections3.isNotEmpty(userIds)) {
            for (String userId : userIds) {
                saveUserRoles(userId, roleIds);
            }
        } else {
            logger.warn("参数[userIds]为空.");
        }
    }

    /**
     * 设置用户岗位 批量
     *
     * @param userId 用户ID集合
     * @param organId 机构ID集合
     * @param postIds 岗位ID集合
     */
    public void updateUserPost(String userId,String organId, Collection<String> postIds) throws ServiceException {
        saveUserOrganPosts(userId, organId, postIds);
    }

    /**
     * 设置用户岗位 批量
     *
     * @param userIds     用户ID集合
     * @param resourceIds 资源ID集合
     */
    public void updateUserResource(Collection<String> userIds, Collection<String> resourceIds) throws ServiceException {
        if (Collections3.isNotEmpty(userIds)) {
            for (String userId : userIds) {
                saveUserResources(userId, resourceIds);
            }
        } else {
            logger.warn("参数[userIds]为空.");
        }
    }

    /**
     * 重置用户密码（AOP切面） 批量
     *
     * @param userIds  用户ID集合
     * @param password 密码(未加密)
     */
    public void updateUserPasswordReset(Collection<String> userIds, String password) throws ServiceException {
        if (Collections3.isNotEmpty(userIds)) {
            for (String userId : userIds) {
                User model = this.get(userId);
                if (model == null) {
                    throw new ServiceException("用户[" + userId + "]不存在或已被删除.");
                }
                try {
                    model.setOriginalPassword(Encryption.encrypt(password));
                } catch (Exception e) {
                    throw new ServiceException(e);
                }
                model.setPassword(Encrypt.e(password));
                this.updatePasswordByUserId(model.getId(),model.getPassword(),model.getOriginalPassword());
            }
        } else {
            logger.warn("参数[userIds]为空.");
        }
    }

    /**
     * 排序号交换
     *
     * @param upUserId
     * @param downUserId
     * @param moveUp     是否上移 是；true 否（下移）：false
     */
    public void changeOrderNo(String upUserId, String downUserId, boolean moveUp) {
        Validate.notNull(upUserId, "参数[upUserId]不能为null!");
        Validate.notNull(downUserId, "参数[downUserId]不能为null!");
        User upUser = this.get(upUserId);
        User downUser = this.get(downUserId);
        if (upUser == null) {
            throw new ServiceException("用户[" + upUserId + "]不存在.");
        }
        if (downUser == null) {
            throw new ServiceException("用户[" + downUserId + "]不存在.");
        }
        Integer upUserOrderNo = upUser.getSort();
        Integer downUserOrderNo = downUser.getSort();
        if (upUser.getSort() == null) {
            upUserOrderNo = 1;
        }

        if (downUser.getSort() == null) {
            downUserOrderNo = 1;
        }
        if (upUserOrderNo.equals(downUserOrderNo)) {
            if (moveUp) {
                upUser.setSort(upUserOrderNo - 1);
            } else {
                downUser.setSort(downUserOrderNo + 1);
            }
        } else {
            upUser.setSort(downUserOrderNo);
            downUser.setSort(upUserOrderNo);
        }

        this.save(upUser);
        this.save(downUser);
    }

    /**
     * 锁定用户 批量
     *
     * @param userIds 用户ID集合
     */
    public void lockUsers(Collection<String> userIds, String status) {
        if (Collections3.isNotEmpty(userIds)) {
            List<User> list = findUsersByIds(userIds);
            for (User user : list) {
                user.setStatus(status);
                this.save(user);
            }
        } else {
            logger.warn("参数[userIds]为空.");
        }
    }

    /**
     * 根据ID查找
     *
     * @param userIds 用户ID集合
     * @return
     */
    public List<User> findUsersByIds(Collection<String> userIds) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("ids", userIds);
        return dao.findUsersByIds(parameter);
    }

    /**
     * 查询指定结构用户
     *
     * @param organIds
     * @return
     */
    public List<User> findUsersByOrganIds(Collection<String> organIds) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("organIds", organIds);
        return dao.findUsersByOrganIds(parameter);
    }

    /**
     * 查询指定结构用户ID
     *
     * @param organId
     * @return
     */
    public List<String> findUserIdsByOrganId(String organId) {
        List<String> list = new ArrayList<>(1);
        list.add(organId);
        return findUserIdsByOrganIds(list);
    }

    /**
     * 查询指定结构用户ID
     *
     * @param organIds
     * @return
     */
    public List<String> findUserIdsByOrganIds(Collection<String> organIds) {
        if (Collections3.isEmpty(organIds)) {
            return Collections.emptyList();
        }
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("organIds", organIds);
        return dao.findUsersIdsByOrganIds(parameter);
    }

    /**
     * 查询指定结构用户ID
     *
     * @param organId 机构ID
     * @return
     */
    public List<String> findUsersLoginNamesByOrganId(String organId) {
        List<String> list = new ArrayList<>(1);
        list.add(organId);
        return findUsersLoginNamesByOrganIds(list);
    }


    /**
     * 查询指定结构用户账号
     *
     * @param organIds 机构IDS
     * @return
     */
    public List<String> findUsersLoginNamesByOrganIds(Collection<String> organIds) {
        if (Collections3.isEmpty(organIds)) {
            return Collections.emptyList();
        }
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("organIds", organIds);
        return dao.findUsersLoginNamesByOrganIds(parameter);
    }


    /**
     * 查询指定机构以及子机构
     *
     * @param organId 机构ID
     * @return
     */
    public List<User> findOwnerAndChildsUsers(String organId) {
        return findOwnerAndChildsUsers(organId, null,null);
    }


    /**
     * 查询指定机构以及子机构
     *
     * @param organId 机构ID
     * @param organTypes 机构类型 {@link com.eryansky.modules.sys._enum.OrganType}
     * @param excludeUserIds 排除的用户ID
     * @return
     */
    public List<User> findOwnerAndChildsUsers(String organId, Collection<String> organTypes, Collection<String> excludeUserIds) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("organId", organId);
        parameter.put("organTypes", organTypes);
        parameter.put("excludeUserIds", excludeUserIds);
        return dao.findOwnerAndChildsUsers(parameter);
    }


    /**
     * 查询指定机构以及子机构
     *
     * @param organId 机构ID
     * @return
     */
    public List<String> findOwnerAndChildsUserIds(String organId) {
        return findOwnerAndChildsUserIds(organId,null);
    }

    /**
     * 查询指定机构以及子机构
     *
     * @param organId 机构ID
     * @param organTypes 机构类型 {@link com.eryansky.modules.sys._enum.OrganType}
     * @return
     */
    public List<String> findOwnerAndChildsUserIds(String organId, Collection<String> organTypes) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("organId", organId);
        parameter.put("organTypes", organTypes);
        return dao.findOwnerAndChildsUsersIds(parameter);
    }


    /**
     * 角色用户（分页查询）
     *
     * @param page
     * @param roleId
     * @param parentOrganId 分级授权机构ID
     * @param query
     * @return
     */
    public Page<User> findPageRoleUsers(Page<User> page, String roleId,String parentOrganId, String query) {
        Parameter parameter = new Parameter();
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put("roleId", roleId);
        parameter.put("parentOrganId", parentOrganId);
        parameter.put("query", query);
        return page.setResult(dao.findUsersByRole(parameter));
    }

    /**
     * 根据角色查询
     *
     * @param roleId 角色ID
     * @return
     */
    public List<User> findUsersByRoleId(String roleId) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("roleId", roleId);
        return dao.findUsersByRoleId(parameter);
    }

    /**
     * 根据角色查询（分页）
     *
     * @param page
     * @param roleId 角色ID
     * @return
     */
    public Page<User> findUsersByRoleId(Page<User> page,String roleId) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("roleId", roleId);
        return page.setResult(dao.findUsersByRoleId(parameter));
    }

    /**
     * 根据角色查询
     *
     * @param roleId 角色ID
     * @return
     */
    public List<String> findUserIdsByRoleId(String roleId) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("roleId", roleId);
        return dao.findUserIdsByRoleId(parameter);
    }

    /**
     * 根据岗位查询
     *
     * @param postId 岗位ID
     * @return
     */
    public List<User> findUsersByPostId(String postId) {
        return findUsersByPost(postId,null);
    }

    /**
     * 根据岗位查询
     *
     * @param postCode 岗位编码
     * @return
     */
    public List<User> findUsersByPostCode(String postCode) {
        return findUsersByPost(null,postCode);
    }


    /**
     * 根据岗位查询
     *
     * @param postId 岗位ID
     * @param postCode 岗位编码
     * @return
     */
    public List<User> findUsersByPost(String postId,String postCode) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("postId", postId);
        parameter.put("postCode", postCode);
        return dao.findUsersByPost(parameter);
    }

    /**
     * 根据岗位查询（分页）
     *
     * @param page 分页对象
     * @param postId 岗位ID
     * @param postCode 岗位编码
     * @param parentOrganId 顶级机构id
     * @param query 关键字
     * @return
     */
    public Page<User> findPageByPost(Page<User> page,String postId,String postCode,String parentOrganId,String query) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put("postId", postId);
        parameter.put("postCode", postCode);
        parameter.put("parentOrganId", parentOrganId);
        parameter.put("query", query);
        return page.setResult(dao.findUsersByPost(parameter));
    }


    /**
     * 根据岗位查询
     *
     * @param postId 岗位ID
     * @return
     */
    public List<String> findUserIdsByPostId(String postId) {
        return findUserIdsByPost(postId,null);
    }

    /**
     * 根据岗位查询
     *
     * @param postCode 岗位编码
     * @return
     */
    public List<String> findUserIdsByPostCode(String postCode) {
        return findUserIdsByPost(null,postCode);
    }

    /**
     * 根据岗位查询
     *
     * @param postId 岗位ID
     * @param postCode 岗位编码
     * @return
     */
    public List<String> findUserIdsByPost(String postId,String postCode) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("postId", postId);
        parameter.put("postCode", postCode);
        return dao.findUserIdsByPost(parameter);
    }


    /**
     * 根据岗位查询
     *
     * @param postId  岗位ID
     * @param organId 机构ID
     * @return
     */
    public List<User> findListByPostIdAndOrganId(String postId, String organId) {
        return findListByPostAndOrgan(postId,null,organId,null);
    }

    /**
     * 根据岗位查询
     *
     * @param postId  岗位ID
     * @param companyId 单位ID
     * @return
     */
    public List<User> findListByPostIdAndCompanyId(String postId, String companyId) {
        return findListByPostAndOrgan(postId,null,null,null,companyId,null,null,null);
    }

    /**
     * 根据岗位查询
     *
     * @param postId  岗位ID
     * @param homeCompanyId 上级单位ID
     * @return
     */
    public List<User> findListByPostIdAndHomeCompanyId(String postId, String homeCompanyId) {
        return findListByPostAndOrgan(postId,null,null,null,null,null,homeCompanyId,null);
    }

    /**
     * 根据岗位查询
     *
     * @param postCode  岗位编码
     * @param organCode 机构编码
     * @return
     */
    public List<User> findListByPostCodeAndOrganCode(String postCode, String organCode) {
        return findListByPostAndOrgan(null,postCode,null,organCode);
    }

    /**
     * 根据岗位查询
     *
     * @param postCode  岗位编码
     * @param companyCode 单位编码
     * @return
     */
    public List<User> findListByPostCodeAndCompanyCode(String postCode, String companyCode) {
        return findListByPostAndOrgan(null,postCode,null,null,null,companyCode,null,null);
    }

    /**
     * 根据岗位查询
     *
     * @param postCode  岗位编码
     * @param homeCompanyCode 上级单位编码
     * @return
     */
    public List<User> findListByPostCodeAndHomeCompanyCode(String postCode, String homeCompanyCode) {
        return findListByPostAndOrgan(null,postCode,null,null,null,null,null,homeCompanyCode);
    }


    /**
     * 根据岗位查询
     *
     * @param postId  岗位ID
     * @param postCode  岗位编码
     * @param organId 机构ID
     * @param organCode 机构编码
     * @return
     */
    public List<User> findListByPostAndOrgan(String postId,String postCode, String organId, String organCode) {
        return findListByPostAndOrgan(postId, postCode, organId, organCode,null,null,null,null);
    }

    /**
     * 根据岗位查询
     *
     * @param postId  岗位ID
     * @param postCode  岗位编码
     * @param organId 机构ID
     * @param organCode 机构编码
     * @param companyId 单位ID
     * @param companyCode 单位编码
     * @param homeCompanyId 上级单位ID
     * @param homeCompanyCode 上级单位编码
     * @return
     */
    public List<User> findListByPostAndOrgan(String postId,String postCode, String organId, String organCode,String companyId,String companyCode,String homeCompanyId,String homeCompanyCode) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("postId", postId);
        parameter.put("postCode", postCode);
        parameter.put("organId", organId);
        parameter.put("organCode", organCode);
        parameter.put("companyId", companyId);
        parameter.put("companyCode", companyCode);
        parameter.put("homeCompanyId", homeCompanyId);
        parameter.put("homeCompanyCode", homeCompanyCode);
        return dao.findListByPostAndOrgan(parameter);
    }


    /**
     * 根据岗位查询（分页）
     *
     * @param page  分页对象
     * @param postId  岗位ID
     * @param postCode  岗位编码
     * @param organId 机构ID
     * @param organCode 机构编码
     * @return
     */
    public Page<User> findPageByPostAndOrgan(Page<User> page,String postId,String postCode, String organId, String organCode) {
        return findPageByPostAndOrgan(page, postId, postCode, organId, organCode,null,null,null,null);
    }

    /**
     * 根据岗位查询（分页）
     *
     * @param postId  岗位ID
     * @param postCode  岗位编码
     * @param organId 机构ID
     * @param organCode 机构编码
     * @param companyId 单位ID
     * @param companyCode 单位编码
     * @param homeCompanyId 上级单位ID
     * @param homeCompanyCode 上级单位编码
     * @return
     */
    public Page<User> findPageByPostAndOrgan(Page<User> page,String postId,String postCode, String organId, String organCode,String companyId,String companyCode,String homeCompanyId,String homeCompanyCode) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put("postId", postId);
        parameter.put("postCode", postCode);
        parameter.put("organId", organId);
        parameter.put("organCode", organCode);
        parameter.put("companyId", companyId);
        parameter.put("companyCode", companyCode);
        parameter.put("homeCompanyId", homeCompanyId);
        parameter.put("homeCompanyCode", homeCompanyCode);
        return page.setResult(dao.findListByPostAndOrgan(parameter));
    }


    /**
     * 根据岗位查询
     *
     * @param postId  岗位ID
     * @param organId 机构ID
     * @return
     */
    public List<String> findUserIdsByPostIdAndOrganId(String postId,String organId){
        return findUserIdsByPostAndOrgan(postId,null,organId,null);
    }


    /**
     * 根据岗位查询
     *
     * @param postId  岗位ID
     * @param companyId 单位ID
     * @return
     */
    public List<String> findUserIdsByPostIdAndCompanyId(String postId,String companyId){
        return findUserIdsByPostAndOrgan(postId,null,null,null,companyId,null,null,null);
    }

    /**
     * 根据岗位查询
     *
     * @param postId  岗位ID
     * @param homeCompanyId 单位ID
     * @return
     */
    public List<String> findUserIdsByPostIdAndHomeCompanyId(String postId,String homeCompanyId){
        return findUserIdsByPostAndOrgan(postId,null,null,null,null,null,homeCompanyId,null);
    }

    /**
     * 根据岗位查询
     *
     * @param postCode  岗位编码
     * @param organCode 机构编码
     * @return
     */
    public List<String> findUserIdsByPostCodeAndOrganCode(String postCode,String organCode){
        return findUserIdsByPostAndOrgan(null,postCode,null,organCode);
    }

    /**
     * 根据岗位查询
     *
     * @param postCode  岗位编码
     * @param companyCode 单位编码
     * @return
     */
    public List<String> findUserIdsByPostCodeAndCompanyCode(String postCode,String companyCode){
        return findUserIdsByPostAndOrgan(null,postCode,null,null,null,companyCode,null,null);
    }

    /**
     * 根据岗位查询
     *
     * @param postCode  岗位编码
     * @param homeCompanyCode 上级单位编码
     * @return
     */
    public List<String> findUserIdsByPostCodeAndHomeCompanyCode(String postCode,String homeCompanyCode){
        return findUserIdsByPostAndOrgan(null,postCode,null,null,null,null,null,homeCompanyCode);
    }

    /**
     * 根据岗位查询
     *
     * @param postId  岗位ID
     * @param postCode  岗位编码
     * @param organId 机构ID
     * @param organCode 机构编码
     * @return
     */
    public List<String> findUserIdsByPostAndOrgan(String postId,String postCode, String organId, String organCode) {
        return findUserIdsByPostAndOrgan(postId, postCode, organId, organCode,null,null,null,null);
    }


    /**
     * 根据岗位查询
     *
     * @param postId  岗位ID
     * @param postCode  岗位编码
     * @param organId 机构ID
     * @param organCode 机构编码
     * @param companyId 单位ID
     * @param companyCode 单位编码
     * @param homeCompanyId 上级单位ID
     * @param homeCompanyCode 上级单位编码
     * @return
     */
    public List<String> findUserIdsByPostAndOrgan(String postId,String postCode, String organId, String organCode,String companyId,String companyCode,String homeCompanyId,String homeCompanyCode) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("postId", postId);
        parameter.put("postCode", postCode);
        parameter.put("organId", organId);
        parameter.put("organCode", organCode);
        parameter.put("companyId", companyId);
        parameter.put("companyCode", companyCode);
        parameter.put("homeCompanyId", homeCompanyId);
        parameter.put("homeCompanyCode", homeCompanyCode);
        return dao.findUserIdsByPostAndOrgan(parameter);
    }


    /**
     * 根据岗位查询
     *
     * @param postId  岗位ID
     * @param organId 机构ID
     * @return
     */
    public List<User> findOwnerAndChildsByPostIdAndOrganId(String postId, String organId) {
        return findOwnerAndChildsByPostAndOrgan(postId,null,organId);
    }


    /**
     * 根据岗位查询
     *
     * @param postCode  岗位编码
     * @param organCode 机构编码
     * @return
     */
    public List<User> findOwnerAndChildsByPostCodeAndOrganCode(String postCode, String organCode) {
        Organ organ = OrganUtils.getByOrganCode(organCode);
        if(null == organ){
            throw new SystemException("机构编码["+organCode+"]对应机构不存在!");
        }
        return findOwnerAndChildsByPostAndOrgan(null,postCode,organ.getId());
    }

    /**
     * 根据岗位查询
     *
     * @param postId  岗位ID
     * @param postCode  岗位编码
     * @param organId 机构ID
     * @return
     */
    public List<User> findOwnerAndChildsByPostAndOrgan(String postId,String postCode, String organId) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("postId", postId);
        parameter.put("postCode", postCode);
        parameter.put("organId", organId);
        return dao.findOwnerAndChildsByPostAndOrgan(parameter);
    }

    /**
     * 快速查找方法
     *
     * @param unionUsers 用户信息
     * @param rootId 根ID
     * @return
     */
    public List<TreeNode> toTreeNodeByUsersAndRootOrganId(Collection<User> unionUsers,String rootId) {
        return toTreeNodeByUsersAndRootOrganId(unionUsers,false,rootId);
    }

    /**
     * 快速查找方法
     *
     * @param unionUsers 用户信息
     * @param shortOrganName 机构简称
     * @param rootId 根ID
     * @return
     */
    public List<TreeNode> toTreeNodeByUsersAndRootOrganId(Collection<User> unionUsers,Boolean shortOrganName,String rootId) {
        if(Collections3.isEmpty(unionUsers)){
            return Collections.emptyList();
        }
        final int[] minLevel = {Integer.MAX_VALUE};
        final int[] maxLevel = {Integer.MIN_VALUE};
        Map<Integer, List<OrganExtend>> organMap = Maps.newHashMap();// 树层级 机构
        Map<String, OrganExtend> organTempMap = Maps.newHashMap();// 机构 机构
        Map<String, List<User>> userMap = Maps.newHashMap();// 机构ID 用户
        unionUsers.forEach(user->{
            OrganExtend userOrganExtend = OrganUtils.getOrganExtendByUserId(user.getId());
            if (userOrganExtend == null) {
                throw new ServiceException(Result.ERROR, user.getName() + "未设置默认机构.", null);
            }

            OrganExtend _userOrganExtend = userOrganExtend;
            while (null != _userOrganExtend) {
                List<OrganExtend> organs = organMap.get(_userOrganExtend.getTreeLevel());
                if (Collections3.isEmpty(organs)) {
                    organs = Lists.newArrayList();
                }
                List<OrganExtend> pList = organMap.get(_userOrganExtend.getTreeLevel());
                if (Collections3.isEmpty(pList) || !pList.contains(_userOrganExtend)) {
                    organs.add(_userOrganExtend);
                }
                organMap.put(_userOrganExtend.getTreeLevel(), organs);
                organTempMap.put(_userOrganExtend.getId(), _userOrganExtend);

                if (maxLevel[0] < _userOrganExtend.getTreeLevel()) {
                    maxLevel[0] = _userOrganExtend.getTreeLevel();
                }
                if (minLevel[0] > _userOrganExtend.getTreeLevel()) {
                    minLevel[0] = _userOrganExtend.getTreeLevel();
                }

                //补全上级机构
                if (StringUtils.isNotBlank(rootId)) {
                    if (rootId.equals(_userOrganExtend.getId())) {
                        _userOrganExtend = null;
                    } else {
                        _userOrganExtend = OrganUtils.getOrganExtend(_userOrganExtend.getParentId());
                    }
                } else {
                    _userOrganExtend = null;
                }

            }

            List<User> users = userMap.get(userOrganExtend.getId());
            if (Collections3.isEmpty(users)) {
                users = Lists.newArrayList();
            }
            users.add(user);
            userMap.put(userOrganExtend.getId(), users);
        });

        int finalMinLevel = minLevel[0];
        unionUsers.forEach(u->{
            //补漏(中间漏了的机构)
            OrganExtend _userOrganExtend = OrganUtils.getOrganExtendByUserId(u.getId());
            while (null != _userOrganExtend) {
                if (_userOrganExtend.getTreeLevel() >= finalMinLevel && !organTempMap.containsKey(_userOrganExtend.getId())) {
                    List<OrganExtend> organs = organMap.get(_userOrganExtend.getTreeLevel());
                    if (Collections3.isEmpty(organs)) {
                        organs = Lists.newArrayList();
                    }
                    List<OrganExtend> pList = organMap.get(_userOrganExtend.getTreeLevel());
                    if (Collections3.isEmpty(pList) || !pList.contains(_userOrganExtend)) {
                        organs.add(_userOrganExtend);
                    }
                    organMap.put(_userOrganExtend.getTreeLevel(), organs);
                    organTempMap.put(_userOrganExtend.getId(), _userOrganExtend);

                }
                _userOrganExtend = OrganUtils.getOrganExtend(_userOrganExtend.getParentId());

            }
        });

        List<Integer> levelKeys = Lists.newArrayList(organMap.keySet());
        Collections.sort(levelKeys);

        List<TreeNode> treeNodes = Lists.newArrayList();
        levelKeys.forEach(level->{
            List<OrganExtend> organExtends = organMap.get(level);
            organExtends.sort(Comparator.comparing(OrganExtend::getSort, Comparator.nullsLast(Integer::compareTo)));
            organExtends.forEach(oe->{
                TreeNode organTreeNode = new TreeNode(oe.getId(), (null != shortOrganName && shortOrganName && StringUtils.isNotBlank(oe.getShortName())) ? oe.getShortName():oe.getName());
                organTreeNode.setpId(oe.getParentId());
                organTreeNode.addAttribute("nType", "o");
                organTreeNode.addAttribute("type", oe.getType());
                organTreeNode.addAttribute("code", oe.getCode());
//                organTreeNode.addAttribute("sysCode", oe.getSysCode());
                organTreeNode.setIconCls(ICON_GROUP);
//                organTreeNode.setNocheck(true);
                treeNodes.add(organTreeNode);
                List<User> _userList = userMap.get(oe.getId());
                if (Collections3.isEmpty(_userList)) {
                    return;
                }
                _userList.sort(Comparator.comparing(User::getSort, Comparator.nullsLast(Integer::compareTo)));
                _userList.forEach(v->{
                    TreeNode userNode = new TreeNode(v.getId(), v.getName());
                    userNode.setpId(oe.getId());
                    userNode.addAttribute("nType", "u");
                    if (SexType.girl.getValue().equals(v.getSex())) {
                        userNode.setIconCls(ICON_USER_RED);
                    } else {
                        userNode.setIconCls(ICON_USER);
                    }
                    organTreeNode.addChild(userNode);
                });
            });
        });
        return treeNodes;
    }

    /**
     * 查找父级节点
     * @param parentId
     * @param treeNodes
     * @return
     */
    private TreeNode getParentTreeNode(String parentId, List<TreeNode> treeNodes){
        TreeNode t = null;
        for(TreeNode treeNode:treeNodes){
            if(parentId.equals(treeNode.getId())){
                t = treeNode;
                break;
            }
        }
        return t;
    }
    /**
     * 登录
     */
    public void login(String userId) {
        logger.debug("login {}", new Object[]{userId});

    }


    /**
     * 注销 空操 可提供切面使用
     */
    public void logout(String userId, SecurityType securityType) {
        logger.debug("logout {}、{}", new Object[]{userId, securityType});

    }


    /**
     * 保存用户机构关联信息
     * 保存之前先删除原有
     *
     * @param id  用户ID
     * @param ids 机构IDS
     */
    public void saveUserOrgans(String id, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("ids", ids);
        dao.deleteUserOrgansByUserId(parameter);
        if (Collections3.isNotEmpty(ids)) {
            dao.insertUserOrgans(parameter);
        }
    }

    /**
     * 保存用户岗位关联信息
     * 保存之前先删除原有
     *
     * @param id  用户ID
     * @param organId  机构ID
     * @param ids 岗位IDS
     */
    public void saveUserOrganPosts(String id, String organId, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("organId", organId);
        parameter.put("ids", ids);
        dao.deleteUserPostsByUserIdAndOrganId(parameter);
        if (Collections3.isNotEmpty(ids)) {
            dao.insertUserPosts(parameter);
        }
    }

    /**
     * 删除用户不在这些部门下的用户岗位信息
     * 保存之前先删除原有
     *
     * @param id  用户ID
     * @param ids 机构IDS
     */
    public int deleteNotInUserOrgansPostsByUserId(String id, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("ids", ids);
        if (Collections3.isNotEmpty(ids)) {
            return dao.deleteNotInUserOrgansPostsByUserId(parameter);
        }
        return 0;
    }


    /**
     * 保存用户角色关联信息
     * 保存之前先删除原有
     *
     * @param id  用户ID
     * @param ids 角色IDS
     */
    public void saveUserRoles(String id, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("ids", ids);
        dao.deleteUserRolesByUserId(parameter);
        if (Collections3.isNotEmpty(ids)) {
            dao.insertUserRoles(parameter);
        }
    }

    /**
     * 查找资源关联用户
     *
     * @param resourceId 资源ID
     * @return
     */
    public List<User> findUsersByResourceId(String resourceId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("resourceId", resourceId);
        return dao.findUsersByResourceId(parameter);
    }

    /**
     * 查找资源关联用户
     *
     * @param resourceId 资源ID
     * @return
     */
    public Page<User> findUsersByResourceId(Page<User> page, String resourceId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("resourceId", resourceId);
        return page.setResult(dao.findUsersByResourceId(parameter));
    }


    /**
     * 保存用户资源关联信息
     * 保存之前先删除原有
     *
     * @param id  用户ID
     * @param ids 资源IDS
     */
    public void saveUserResources(String id, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("ids", ids);
        dao.deleteUserResourcesByUserId(parameter);
        if (Collections3.isNotEmpty(ids)) {
            dao.insertUserResources(parameter);
        }
    }

    /**
     * 删除用户资源关联关系
     *
     * @param userId  用户ID
     */
    public int deleteUserResourcesByUserId(String userId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", userId);
        return dao.deleteUserResourcesByUserId(parameter);
    }

    /**
     * 删除用户资源关联关系
     *
     * @param userId  用户ID
     * @param resourceId 资源ID
     */
    @CacheEvict(value = {CacheConstants.ROLE_ALL_CACHE,
            CacheConstants.RESOURCE_USER_AUTHORITY_URLS_CACHE,
            CacheConstants.RESOURCE_USER_MENU_TREE_CACHE,
            CacheConstants.RESOURCE_USER_RESOURCE_TREE_CACHE,
            CacheConstants.ORGAN_USER_TREE_1_CACHE,
            CacheConstants.ORGAN_USER_TREE_2_CACHE,
            CacheConstants.CACHE_OrganDao,
            CacheConstants.CACHE_UserDao,
            CacheConstants.CACHE_ResourceDao}, allEntries = true)
    public int deleteUserResourceByResourceIdAndUserId(String userId, String resourceId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("userId", userId);
        parameter.put("resourceId", resourceId);
        return dao.deleteUserResourceByResourceIdAndUserId(parameter);
    }


    /**
     * 自定义SQL查询
     *
     * @param whereSQL
     * @return
     */
    public List<User> findByWhereSQL(String whereSQL) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("whereSQL", whereSQL);
        return dao.findByWhereSQL(parameter);
    }

    /**
     * 自定义SQL查询
     *
     * @param sql
     * @return
     */
    public List<User> findBySql(String sql) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("sql", sql);
        return dao.findBySql(parameter);
    }

    /**
     * 根据IDS查询
     *
     * @param ids
     * @return
     */
    public List<User> findByIds(Collection<String> ids) {
        if(Collections3.isEmpty(ids)){
            return Collections.emptyList();
        }
        Parameter parameter = Parameter.newParameter();
        parameter.put("ids", ids);
        return dao.findByIds(parameter);
    }

    /**
     * 根据账号查询
     *
     * @param loginNames
     * @return
     */
    public List<User> findByLoginNames(Collection<String> loginNames) {
        if(Collections3.isEmpty(loginNames)){
            return Collections.emptyList();
        }
        Parameter parameter = Parameter.newParameter();
        parameter.put("loginNames", loginNames);
        return dao.findByLoginNames(parameter);
    }

    /**
     * 根据员工编号查询
     *
     * @param codes
     * @return
     */
    public List<User> findByCodes(List<String> codes) {
        if(Collections3.isEmpty(codes)){
            return Collections.emptyList();
        }
        Parameter parameter = Parameter.newParameter();
        parameter.put("codes", codes);
        return dao.findByCodes(parameter);
    }

}
