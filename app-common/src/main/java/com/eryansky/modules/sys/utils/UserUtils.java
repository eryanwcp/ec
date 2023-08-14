/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.utils;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.ConvertUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.utils.encode.Encryption;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.security.SecurityType;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.modules.sys.mapper.*;
import com.eryansky.modules.sys.service.*;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * @author Eryan
 * @date 2014-11-25
 */
public class UserUtils {

    private UserUtils(){}
    /**
     * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
     */
    public static final class Static {
        private static final UserService userService = SpringContextHolder.getBean(UserService.class);
        private static final OrganService organService = SpringContextHolder.getBean(OrganService.class);
        private static final RoleService roleService = SpringContextHolder.getBean(RoleService.class);
        private static final PostService postService = SpringContextHolder.getBean(PostService.class);
        private static final UserPasswordService userPasswordService = SpringContextHolder.getBean(UserPasswordService.class);

    }

    /**
     * 根据userId查找用户
     *
     * @param userId 用户ID
     * @return
     */
    public static User getUser(String userId) {
        if (StringUtils.isNotBlank(userId)) {
            return Static.userService.get(userId);
        }
        return null;
    }

    /**
     * 根据loginName查找用户
     *
     * @param loginName 用户账号
     * @return
     */
    public static User getUserByLoginName(String loginName) {
        if (StringUtils.isNotBlank(loginName)) {
            return Static.userService.getUserByLoginName(loginName);
        }
        return null;
    }

    /**
     * 根据ID或登录名查找用户
     *
     * @param idOrLoginName ID或登录名
     * @return
     */
    public static User getUserByIdOrLoginName(String idOrLoginName) {
        if (StringUtils.isNotBlank(idOrLoginName)) {
            return Static.userService.getUserByIdOrLoginName(idOrLoginName);
        }
        return null;
    }


    /**
     * 根据登录名或手机号查找用户
     *
     * @param loginNameOrMobile 登录名或手机号
     * @return
     */
    public static User getUserByLoginNameOrMobile(String loginNameOrMobile) {
        if (StringUtils.isNotBlank(loginNameOrMobile)) {
            return Static.userService.getUserByLoginNameOrMobile(loginNameOrMobile);
        }
        return null;
    }


    /**
     * 根据ID或手机号查找用户
     *
     * @param idOrMobile ID、账号或手机号
     * @return
     */
    public static User getUserByIdOrMobile(String idOrMobile) {
        if (StringUtils.isNotBlank(idOrMobile)) {
            return Static.userService.getUserByIdOrMobile(idOrMobile);
        }
        return null;
    }

    /**
     * 根据员工编号查找用户
     *
     * @param code 员工编号
     * @return
     */
    public static List<User> findByCode(String code) {
        if (StringUtils.isNotBlank(code)) {
            return Static.userService.findByCode(code);
        }
        return Collections.emptyList();
    }


    /**
     * 根据姓名查找用户
     *
     * @param name 姓名
     * @return
     */
    public static List<User> findByName(String name) {
        if (StringUtils.isNotBlank(name)) {
            return Static.userService.findByName(name);
        }
        return Collections.emptyList();
    }

    /**
     * 根据账号、员工编号查找用户
     *
     * @param loginName 账号
     * @param code 工编号
     * @return
     */
    public static List<User> findByLoginNameOrCode(String loginName,String code) {
        return Static.userService.findByLoginNameOrCode(loginName,code);
    }

    /**
     * 根据账号、员工编号查找用户
     *
     * @param loginName 账号
     * @param code 工编号
     * @param mobile 手机号
     * @return
     */
    public static List<User> findByLoginNameOrCodeOrMobile(String loginName,String code,String mobile) {
        return Static.userService.findByLoginNameOrCodeOrMobile(loginName,code,mobile);
    }

    /**
     * 根据loginName查找用户
     *
     * @param loginName 用户账号
     * @return
     */
    public static String getUserNameByLoginName(String loginName) {
        return getUserNameByLoginName(loginName,null);
    }

    /**
     * 根据loginName查找用户
     *
     * @param loginName 用户账号
     * @param defaultResult 为null时返回
     * @return
     */
    public static String getUserNameByLoginName(String loginName,String defaultResult) {
        User user = getUserByLoginName(loginName);
        if (null != user) {
            return user.getName();
        }
        return defaultResult;
    }

    /**
     * 根据用户头像
     *
     * @param userId 用户账号
     * @return
     */
    public static String getPhotoUrlByUserId(String userId) {
        User user = getUser(userId);
        if (null != user) {
            return user.getPhotoUrl();
        }
        String ctx = StringUtils.EMPTY;
        try {
            ctx = WebUtils.getAppURL(SpringMVCHolder.getRequest());
        } catch (Exception e) {
        }
        return  ctx + "/static/img/icon_boy.png";
    }

    /**
     * 根据loginName查找用户
     *
     * @param loginName 用户账号
     * @return
     */
    public static String getUserIdByLoginName(String loginName) {
        User user = getUserByLoginName(loginName);
        if (null != user) {
            return user.getId();
        }
        return null;
    }

    /**
     * 根据userId查找用户loginName
     *
     * @param userId 用户ID
     * @return
     */
    public static String getLoginNameByUserId(String userId) {
        User user = getUser(userId);
        if (null != user) {
            return user.getLoginName();
        }
        return null;
    }

    /**
     * 根据手机号码查找用户
     *
     * @param mobile 手机号码
     * @return
     */
    public static User getUserByMobile(String mobile) {
        if (StringUtils.isNotBlank(mobile)) {
            return Static.userService.getUserByMobile(mobile);
        }
        return null;
    }

    /**
     * 根据userId查找用户手机号
     *
     * @param userId 用户ID
     * @return
     */
    public static String getUserMobile(String userId) {
        User user = getUser(userId);
        if (user != null) {
            return user.getMobile();
        }
        return null;
    }


    /**
     * 根据userId查找用户姓名
     *
     * @param userId 用户ID
     * @return
     */
    public static String getUserName(String userId) {
        return getUserName(userId,null);
    }

    /**
     * 根据userId查找用户姓名
     *
     * @param userId 用户ID
     * @param defaultResult 为null时返回
     * @return
     */
    public static String getUserName(String userId,String defaultResult) {
        User user = getUser(userId);
        if (user != null) {
            return user.getName();
        }
        return defaultResult;
    }

    /**
     * 根据userId查找用户登录名
     *
     * @param userId 用户ID
     * @return
     */
    public static String getLoginName(String userId) {
        User user = getUser(userId);
        if (user != null) {
            return user.getLoginName();
        }
        return null;
    }


    /**
     * 根据userId查找用户所属单位ID
     *
     * @param userId 用户ID
     * @return
     */
    public static String getCompanyId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        OrganExtend organExtend = OrganUtils.getOrganExtendByUserId(userId);
        if (null != organExtend) {
            return organExtend.getCompanyId();
        }
        return null;
    }

    /**
     * 根据userId查找用户所属单位ID
     *
     * @param userId 用户ID
     * @return
     */
    public static String getHomeCompanyId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        OrganExtend organExtend = OrganUtils.getOrganExtendByUserId(userId);
        if (null != organExtend) {
            return organExtend.getHomeCompanyId();
        }
        return null;
    }

    /**
     * 根据userId查找用户所属单位编码
     *
     * @param userId 用户ID
     * @return
     */
    public static String getCompanyCode(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        OrganExtend organExtend = OrganUtils.getOrganExtendByUserId(userId);
        if (null != organExtend) {
            return organExtend.getCompanyCode();
        }
        return null;
    }

    /**
     * 根据userId查找用户所属单位编码
     *
     * @param userId 用户ID
     * @return
     */
    public static String getHomeCompanyCode(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        OrganExtend organExtend = OrganUtils.getOrganExtendByUserId(userId);
        if (null != organExtend) {
            return organExtend.getHomeCompanyCode();
        }
        return null;
    }

    /**
     * 根据userId查找用户所属单位编码
     *
     * @param userId 用户ID
     * @return
     */
    public static String getOrganSysCode(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        OrganExtend organExtend = OrganUtils.getOrganExtendByUserId(userId);
        if (null != organExtend) {
            return organExtend.getSysCode();
        }
        return null;
    }

    /**
     * 根据userId查找用户所属单位名称
     *
     * @param userId 用户ID
     * @return
     */
    public static String getCompanyName(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        OrganExtend company = OrganUtils.getCompanyByUserId(userId);
        if (company != null) {
            return company.getName();
        }
        return null;
    }

    /**
     * 根据userId查找用户所属单位名称(简称)
     *
     * @param userId 用户ID
     * @return
     */
    public static String getCompanyShortName(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        OrganExtend company = OrganUtils.getCompanyByUserId(userId);
        if (company != null) {
            return company.getShortName();
        }
        return null;
    }

    /**
     * 根据userId查找用户所属机构ID
     *
     * @param userId 用户ID
     * @return
     */
    public static String getDefaultOrganId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        User user = getUser(userId);
        if (user != null) {
            return user.getDefaultOrganId();
        }
        return null;
    }

    /**
     * 根据userId查找用户所属机构ID
     * @param userId 用户ID
     * @return
     */
    public static String getDefaultOrganCode(String userId){
        if(StringUtils.isBlank(userId)){
            return null;
        }
        OrganExtend organExtend = OrganUtils.getOrganExtendByUserId(userId);
        if(null != organExtend){
            return organExtend.getCode();
        }
        return null;
    }


    /**
     * 查找用户所属机构IDS
     * @param userId 用户ID
     * @return
     */
    public static List<String> findOrganIdsByUserId(String userId){
        if(StringUtils.isBlank(userId)){
            return Collections.emptyList();
        }
        return Static.organService.findOrganIdsByUserId(userId);
    }

    /**
     * 查找部门用户IDS
     * @param organId 机构ID
     * @return
     */
    public static List<String> findUserIdsByOrganId(String organId){
        if(StringUtils.isBlank(organId)){
            return Collections.emptyList();
        }
        return Static.organService.findOrganUserIds(organId);
    }

    /**
     * 查找用户所属机构
     * @param userId 用户ID
     * @return
     */
    public static List<Organ> findOrgansByUserId(String userId){
        if(StringUtils.isBlank(userId)){
            return Collections.emptyList();
        }
        return Static.organService.findOrgansByUserId(userId);
    }


    /**
     * 根据userId查找用户所属机构名称
     *
     * @param userId 用户ID
     * @return
     */
    public static String getDefaultOrganName(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        OrganExtend organExtend = OrganUtils.getOrganExtendByUserId(userId);
        if (null != organExtend) {
            return organExtend.getName();
        }
        return null;
    }

    /**
     * 根据userId查找用户所属机构名称（简称）
     *
     * @param userId 用户ID
     * @return
     */
    public static String getDefaultOrganShortName(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        OrganExtend organExtend = OrganUtils.getOrganExtendByUserId(userId);
        if (null != organExtend) {
            return organExtend.getShortName();
        }
        return null;
    }




    /**
     * 根据userId查找用户姓名
     *
     * @param userIds 用户ID集合
     * @return
     */
    public static String getUserNames(List<String> userIds) {
        if (Collections3.isNotEmpty(userIds)) {
            List<User> list = Static.userService.findUsersByIds(userIds);
            return ConvertUtils.convertElementPropertyToString(list, "name", ",");
        }
        return null;
    }

    /**
     * 得到超级用户
     *
     * @return
     */
    public static User getSuperUser() {
        return Static.userService.getSuperUser();
    }

    /**
     * 更新用户密码
     *
     * @param user 用户
     * @param type {@link  com.eryansky.modules.sys._enum.UserPasswordUpdateType}
     * @return
     */
    public static UserPassword addUserPasswordUpdate(User user,String type) {
        UserPassword userPassword = new UserPassword(user.getId(), user.getPassword());
        userPassword.setOriginalPassword(user.getOriginalPassword());
        userPassword.setType(type);
        Static.userPasswordService.save(userPassword);
        return userPassword;
    }

    /**
     * 更新用户密码
     *
     * @param userId           用户
     * @param password         密码
     * @param originalPassword 原始密码
     * @return
     */
    public static UserPassword addUserPasswordUpdate(String userId, String password, String originalPassword) {
        UserPassword userPassword = new UserPassword(userId, password);
        userPassword.setOriginalPassword(originalPassword);
        Static.userPasswordService.save(userPassword);
        return userPassword;
    }


    /**
     * 修改用户密码 批量
     *
     * @param userIds  用户ID集合
     * @param password 密码(未加密)
     */
    public static void updateUserPasswordReset(List<String> userIds, String password) {
        Static.userService.updateUserPasswordReset(userIds, password);
    }

    /**
     * 修改用户密码
     *
     * @param userId  用户ID
     * @param password 密码(未加密)
     */
    public static void updateUserPassword(String userId, String password) {
        try {
            Static.userService.updatePasswordByUserId(userId,Encrypt.e(password), Encryption.encrypt(password));
        } catch (Exception e) {
            throw new ActionException(e);
        }
    }


    /**
     * 初始化用户密码
     *
     * @param userId  用户ID
     * @param password 密码(未加密)
     */
    public static void updateUserPasswordFirst(String userId, String password) {
        try {
            Static.userService.updatePasswordFirstByUserId(userId,Encrypt.e(password), Encryption.encrypt(password));
        } catch (Exception e) {
            throw new ActionException(e);
        }
    }


    /**
     * 校验密码最近几次是否使用过
     * @param userId 用户ID
     * @param pagePassword 未加密的密码
     */
    public static void checkSecurity(String userId,String pagePassword){
        if(User.SUPERUSER_ID.equals(userId)){
            return;
        }
        if(!AppConstants.getIsSecurityOn()){
            return;
        }
        int max = AppConstants.getUserPasswordRepeatCount();
        if(max <=0){
            return;
        }
        List<UserPassword> userPasswords = Static.userPasswordService.findUserPasswordsByUserIdExcludeReset(userId,max);
        for(UserPassword userPassword:userPasswords){
            if (userPassword.getPassword().equals(Encrypt.e(pagePassword))) {
                throw new ServiceException("你输入的密码在最近"+max+"次以内已使用过，请更换！");
            }
        }
    }


    /**
     * 用户添加角色
     * @param userId 用户ID
     * @param roleCode 角色编码
     */
    public static void addUserRole(String userId, String roleCode) {
        if(null == roleCode){
            throw new ServiceException("角色编码不能为空！");
        }
        Role role = Static.roleService.getByCode(roleCode);
        if(null == role){
            throw new ServiceException("角色【"+roleCode+"】不存在！");
        }
        Static.roleService.addRoleUsers(role.getId(), Lists.newArrayList(userId));
        SecurityUtils.reloadSessionPermission(userId);
    }

    /**
     * 删除用户角色
     * @param userId 用户ID
     * @param roleCode 角色编码
     */
    public static void deleteUserRole(String userId,String roleCode) {
        if(null == roleCode){
            throw new ServiceException("角色编码不能为空！");
        }
        Role role = Static.roleService.getByCode(roleCode);
        if(null == role){
            throw new ServiceException("角色【"+roleCode+"】不存在！");
        }
        Static.roleService.deleteRoleUsersByRoleIdANDUserIds(role.getId(), Lists.newArrayList(userId));
        SecurityUtils.reloadSessionPermission(userId);
    }

    /**
     * 用户添加岗位
     * @param userId 用户ID
     * @param postCode 岗位编码
     */
    public static void addUserOrganPost(String userId, String postCode) {
        User user = UserUtils.getUser(userId);
        if(null == user){
            throw new ServiceException("用户【"+userId+"】不存在！");
        }
        addUserOrganPost(userId,user.getDefaultOrganId(),postCode);
    }


    /**
     * 用户添加岗位
     * @param userId 用户ID
     * @param organId 机构ID
     * @param postCode 岗位编码
     */
    public static void addUserOrganPost(String userId, String organId,String postCode) {
        if(null == postCode){
            throw new ServiceException("岗位编码不能为空！");
        }
        Post post = Static.postService.getByCode(postCode);
        if(null == post){
            throw new ServiceException("岗位【"+postCode+"】不存在！");
        }
        List<String> organIds = Static.organService.findAssociationOrganIdsByPostId(post.getId());
        if(!organIds.contains(organId) && !organId.equals(post.getOrganId())){
            throw new ServiceException("岗位【"+postCode+"】未关联机构【"+organId+"】！");
        }
        Static.postService.addPostOrganUsers(post.getId(),organId,Lists.newArrayList(userId));
        SecurityUtils.reloadSessionPermission(userId);
    }

    /**
     * 删除用户岗位
     * @param userId 用户ID
     * @param postCode 岗位编码
     */
    public static void deleteUserOrganPost(String userId,String postCode) {
        User user = UserUtils.getUser(userId);
        if(null == user){
            throw new ServiceException("用户【"+userId+"】不存在！");
        }
        deleteUserOrganPost(userId,user.getDefaultOrganId(),postCode);
    }

    /**
     * 删除用户岗位（指定机构）
     * @param userId 用户ID
     * @param organId 机构ID
     * @param postCode 岗位编码
     */
    public static void deleteUserOrganPost(String userId,String organId,String postCode) {
        if(null == postCode){
            throw new ServiceException("岗位编码不能为空！");
        }
        if(null == organId){
            throw new ServiceException("机构ID不能为空！");
        }
        Post post = Static.postService.getByCode(postCode);
        if(null == post){
            throw new ServiceException("岗位【"+postCode+"】不存在！");
        }

        Static.postService.deletePostUsersByPostIdAndOrganIdAndUserIds(post.getId(),organId,Lists.newArrayList(userId));
        SecurityUtils.reloadSessionPermission(userId);
    }


    /**
     * 删除用户岗位（不指定机构，全部删除）
     * @param userId 用户ID
     * @param postCode 岗位编码
     */
    public static void deleteUserPost(String userId,String postCode) {
        if(null == userId){
            throw new ServiceException("用户ID不能为空！");
        }
        if(null == postCode){
            throw new ServiceException("岗位编码不能为空！");
        }
        Post post = Static.postService.getByCode(postCode);
        if(null == post){
            throw new ServiceException("岗位【"+postCode+"】不存在！");
        }

        Static.postService.deletePostUsersByPostIdAndUserId(post.getId(),userId);
        SecurityUtils.reloadSessionPermission(userId);
    }



    /**
     * 记录登录日志
     * @param userId 用户ID
     */
    public static void recordLogin(String userId) {
        Static.userService.login(userId);
    }


    /**
     * 记录注销日志
     * @param userId 用户ID
     * @param securityType 操作类型 {@link  SecurityType}
     */
    public static void recordLogout(String userId, SecurityType securityType) {
        Static.userService.logout(userId,securityType);
    }

}
