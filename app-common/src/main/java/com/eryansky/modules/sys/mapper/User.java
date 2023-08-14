/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.mapper;


import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm.mybatis.sensitive.annotation.EncryptField;
import com.eryansky.common.orm.mybatis.sensitive.annotation.SensitiveEncryptEnabled;
import com.eryansky.common.orm.mybatis.sensitive.annotation.SensitiveField;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveType;
import com.eryansky.common.orm.mybatis.sensitive.utils.SensitiveUtils;
import com.eryansky.common.orm.persistence.IUser;
import com.eryansky.common.utils.Pinyin4js;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.sys._enum.DataScope;
import com.eryansky.modules.sys._enum.OrganType;
import com.eryansky.modules.sys._enum.SexType;
import com.eryansky.modules.sys._enum.UserType;
import com.eryansky.modules.sys.utils.DictionaryUtils;
import com.eryansky.modules.sys.utils.UserUtils;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 用户
 *
 * @author Eryan
 * @date 2018-05-08
 */
@JsonFilter(" ")
public class User extends DataEntity<User> implements IUser {

    public static final String SUPERUSER_ID = "1";//超级管理员ID
    public static final String FOLDER_USER_PHOTO = "userPhoto";//头像文件夹编码
    public static final String DIC_USER_TYPE = "SYS_USER_TYPE_EXTEND";//扩展自定义用户类型数据字典编码

    /**
     * 登录名
     */
    private String loginName;
    /**
     * 员工编号
     */
    private String code;
    /**
     * 原始密码
     */
    private String originalPassword;
    /**
     * 登录密码
     */
    private String password;
    /**
     * 姓名
     */
    private String name;
    /**
     * 性别 {@link SexType}
     */
    private String sex;
    /**
     * 出生日期 格式：yyyy-MM-dd
     */
    private Date birthday;
    /**
     * 头像
     */
    private String photo;
    /**
     * 邮箱地址
     */
    private String email;
    /**
     * 个人邮箱
     */
    private String personEmail;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 办公电话
     */
    private String tel;
    /**
     * QQ
     */
    private String qq;
    /**
     * 微信
     */
    private String weixin;
    /**
     * 住址
     */
    private String address;
    /**
     * 默认组织机构ID
     */
    private String defaultOrganId;
    /**
     * 排序号
     */
    private Integer sort;
    /**
     * 用户类型 ${@link UserType}以及 {@link Organ#DIC_ORGAN_TYPE}
     */
    private String userType;
    /**
     * 备注
     */
    private String remark;

    //扩展信息
    /**
     * 关键字
     */
    private String query;
    /**
     * 脱敏后的手机号码
     */
    private String mobileSensitive;
    /**
     * 默认部门名称
     */
    private String defaultOrganName;
    private String companyId;
    private String companyCode;
    private String companyName;
    private String homeCompanyId;
    private String homeCompanyCode;
    private String homeCompanyName;

    public User() {
    }

    @Override
    public void prePersist() {
        super.prePersist();
    }

    @Override
    public void preUpdate() {
        super.preUpdate();
    }

    public User(String id) {
        super(id);
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginName() {
        return this.loginName;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    public void setOriginalPassword(String originalPassword) {
        this.originalPassword = originalPassword;
    }

    public String getOriginalPassword() {
        return this.originalPassword;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSex() {
        return this.sex;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    @JsonFormat(pattern = DATE_FORMAT, timezone = TIMEZONE)
    public Date getBirthday() {
        return this.birthday;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhoto() {
        return this.photo;
    }

    public String getPhotoUrl() {
        return DiskUtils.getFileUrl(photo);
    }

    public String getPhotoSrc() {
        return DiskUtils.getFileSrc(photo);
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setPersonEmail(String personEmail) {
        this.personEmail = personEmail;
    }

    public String getPersonEmail() {
        return this.personEmail;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return this.mobile;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getTel() {
        return this.tel;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getQq() {
        return this.qq;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return this.address;
    }

    public void setDefaultOrganId(String defaultOrganId) {
        this.defaultOrganId = defaultOrganId;
    }

    public String getDefaultOrganId() {
        return this.defaultOrganId;
    }


    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserType() {
        return this.userType;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setWeixin(String weixin) {
        this.weixin = weixin;
    }

    public String getWeixin() {
        return this.weixin;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getCompanyId() {
        if(null != companyId){
            return companyId;
        }
        return UserUtils.getCompanyId(this.id);
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        if(null != companyName){
            return companyName;
        }
        return UserUtils.getCompanyName(this.id);
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDefaultOrganName() {
        if(null != defaultOrganName){
            return defaultOrganName;
        }
        return UserUtils.getDefaultOrganName(this.id);
    }

    public void setDefaultOrganName(String defaultOrganName) {
        this.defaultOrganName = defaultOrganName;
    }

    public boolean isAdmin() {
        return SecurityUtils.isUserAdmin(this.id);
    }

    /**
     * 拼音首字母
     *
     * @return
     */
    public String getNamePinyinHeadChar() {
        if (StringUtils.isNotBlank(name)) {
            String str = Pinyin4js.getPinYinHeadChar(Pinyin4js.filterSpecialCharacter(StringUtils.trim(name)));
            if (str != null) {
                return str.substring(0, 1).toUpperCase();
            }
        }
        return "#";
    }


    /**
     * 性别描述.
     */
    public String getSexView() {
        return GenericEnumUtils.getDescriptionByValue(SexType.class,sex,sex);
    }

    /**
     * 用户类型显示.
     */
    public String getTypeView() {
        String typeView = GenericEnumUtils.getDescriptionByValue(UserType.class,userType,null);
        if(null == typeView){
            typeView = DictionaryUtils.getDictionaryNameByDC(DIC_USER_TYPE,userType,userType);
        }
        return typeView;
    }

    /**
     * 手机号脱敏
     * @return
     */
    public String getMobileSensitive() {
        return SensitiveUtils.getSensitive(mobile,SensitiveType.MOBILE_PHONE);
    }
}
