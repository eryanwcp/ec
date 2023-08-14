/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.encode.Encryption;
import com.eryansky.common.utils.ftp.FtpFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.StandardEnvironment;


/**
 * 项目中用到的静态变量.
 * 
 * @author Eryan
 * @date 2012-8-20 上午11:40:56
 */
public class SysConstants {

    private static final Logger logger = LoggerFactory.getLogger(SysConstants.class);
    /**
     * session 验证码key
     */
    public static final String SESSION_VALIDATE_CODE = "validateCode";

    private static class SysConstantsHolder {
        private static final StandardEnvironment standardEnvironment = SpringContextHolder.getBean(StandardEnvironment.class);
    }

    /**
     * 配置文件(appconfig.properties)
     */
    public static StandardEnvironment getAppConfig() {
        return SysConstantsHolder.standardEnvironment;
    }

    /**
     * 获取配置
     */
    public static String getAppConfig(String key) {
        return SysConstants.getAppConfig().getProperty(key);
    }

    /**
     * 获取配置
     */
    public static String getAppConfig(String key,String defaultValue) {
        return SysConstants.getAppConfig().getProperty(key,defaultValue);
    }

    /**
     * jdbc type连接参数(默认:"").
     */
    public static String getJdbcType(){
        return SysConstants.getAppConfig().getProperty("jdbc.type","");
    }
    
    /**
     * jdbc url连接参数(默认:"").
     */
    public static String getJdbcUrl(){
    	return SysConstants.getAppConfig().getProperty("spring.datasource.url","");
    }

    /**
     * jdbc 驱动类
     * @return
     */
    public static String getJdbcDriverClassName(){
        return SysConstants.getAppConfig().getProperty("spring.datasource.driver-class-name","");
    }

    /**
     * jdbc 用户名
     * @return
     */
    public static String getJdbcUserName(){
        return SysConstants.getAppConfig().getProperty("spring.datasource.username","");
    }

    /**
     * jdbc 密码
     * @return
     */
    public static String getJdbcPassword(){
        return SysConstants.getAppConfig().getProperty("spring.datasource.password","");
    }


    /**
     * jdbc 密码
     * @return
     */
    public static String getDruidJdbcPassword(){
        //获取配置文件中的已经加密的密码
        String ePassword = getAppConfig().getProperty("spring.datasource.druid.connect-properties.password","");
        String cKey = getAppConfig().getProperty("spring.datasource.druid.connect-properties.key","");
        String cdecrypt = getAppConfig().getProperty("spring.datasource.druid.connect-properties.config.decrypt","false");
        boolean decrypt = Boolean.parseBoolean(cdecrypt);
        if (decrypt && StringUtils.isNotEmpty(ePassword)) {
            try {
                //这里的代码是将密码进行解密，并设置
                String password = StringUtils.isNotBlank(cKey) ?  Encryption.decrypt(ePassword,cKey ): Encryption.decrypt(ePassword);
                return password;
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
        }
        return getJdbcPassword();
    }


    /**
     * 获取是否是开发模式(默认:false).
     */
    public static Boolean isdevMode(){
    	return Boolean.valueOf(getAppConfig().getProperty("devMode","false"));
    }

}
