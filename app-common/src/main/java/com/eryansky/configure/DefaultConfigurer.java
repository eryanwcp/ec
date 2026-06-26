/**
 * Copyright (c) 2012-2019 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.configure;

import com.eryansky.common.utils.ObjectUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.io.PropertiesLoader;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.j2cache.util.ForySerializer;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.sys.mapper.Config;
import com.eryansky.modules.sys.service.ConfigService;
import com.eryansky.utils.AppConstants;
import javax.annotation.Resource;
import org.apache.fory.resolver.AllowListChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * @author Eryan
 * @date 2024-08-01
 */
@Configuration
public class DefaultConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConfigurer.class);

    @Resource
    private ConfigService configService;

    private void checkSysConfig() {
        List<Config> dbConfigs = configService.findList(new Config());
        PropertiesLoader propertiesLoader = AppConstants.getConfig();
        String[] resourcesPaths = propertiesLoader.getResourcesPaths();
        Properties properties = AppConstants.getConfig().getProperties();
        logger.info("系统参数读取模式：{}", AppConstants.isdevMode() ? "配置文件" + resourcesPaths[0] : "数据库");
        if (Collections3.isNotEmpty(dbConfigs) && null != properties) {
            dbConfigs.forEach(v -> {
                Map.Entry<Object, Object> r = properties.entrySet().stream().filter(p -> p.getKey().toString().equals(v.getCode())).findFirst().orElse(null);
                if (null != r && !ObjectUtils.isEquals(r.getValue(), v.getValue())) {
                    logger.warn("数据库与配置文件读取的系统参数配置不一致！{}：{} {}", v.getCode(), v.getValue(), r.getValue());
                }
            });
        }
    }



    private void checkSerializerTypeCheck() {
        List<String> disallowClasses = AppConstants.getSerializerTypeCheckDisallowClassList();
        AllowListChecker allowListChecker = ForySerializer.getTypeChecker();
        if (Collections3.isNotEmpty(disallowClasses)) {
            logger.info("SerializerTypeCheck disallowClasses : {}", JsonMapper.toJsonString(disallowClasses));
            allowListChecker.disallowClasses(disallowClasses);
        }

        List<String> allowClassList = AppConstants.getSerializerTypeCheckAllowClassList();
        if (Collections3.isNotEmpty(allowClassList)) {
            logger.info("SerializerTypeCheck allowClasses : {}", JsonMapper.toJsonString(allowClassList));
            allowListChecker.allowClasses(allowClassList);
        }
        allowListChecker.setCheckLevel(AllowListChecker.CheckLevel.STRICT);
        logger.info("SerializerTypeCheck: {} checkLevel: {}",allowListChecker.getClass().getName(),allowListChecker.getCheckLevel().name());
    }

    private void clearTempDir(){
        logger.info("清空本地缓存目录：{}", AppConstants.getDiskTempDir());
        DiskUtils.clearTempDir();
        logger.info("清空本地缓存目录结束。");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        logger.info("当前启动系统：{}-V{}",AppConstants.getAppFullName(),AppConstants.getAppVersion());
        logger.info("文件存储方式：{}",AppConstants.getSystemDiskType());

        clearTempDir();

        checkSysConfig();

        logger.info("默认访问地址：{}",AppConstants.getAppURL());
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefresh(ContextRefreshedEvent event) {
        checkSerializerTypeCheck();
    }

}
