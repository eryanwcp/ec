/**
 * Copyright (c) 2012-2019 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.configure;

import com.eryansky.common.utils.ObjectUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.io.PropertiesLoader;
import com.eryansky.encrypt.advice.DecryptRequestBodyAdvice;
import com.eryansky.encrypt.advice.EncryptResultResponseBodyAdvice;
import com.eryansky.modules.sys.mapper.Config;
import com.eryansky.modules.sys.service.ConfigService;
import com.eryansky.utils.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Autowired
    private ConfigService configService;

    @Bean
    public String checkSysConfig() {
        List<Config> dbConfigs = configService.findList(new Config());
        PropertiesLoader propertiesLoader = AppConstants.getConfig();
        String[] resourcesPaths = propertiesLoader.getResourcesPaths();
        Properties properties = AppConstants.getConfig().getProperties();
        logger.info("系统参数读取模式：{}", AppConstants.isdevMode() ? "配置文件" + resourcesPaths[0] : "数据库t_sys_config");
        if (Collections3.isNotEmpty(dbConfigs) && null != properties) {
            dbConfigs.forEach(v -> {
                Map.Entry<Object, Object> r = properties.entrySet().stream().filter(p -> p.getKey().toString().equals(v.getCode())).findFirst().orElse(null);
                if (null != r && !ObjectUtils.isEquals(r.getValue(), v.getValue())) {
                    logger.warn("数据库与配置文件读取的系统参数配置不一致！{}：{} {}", v.getCode(), v.getValue(), r.getValue());
                }
            });
        }
        return null;
    }

    @Bean
    public EncryptResultResponseBodyAdvice encryptResultResponseBodyAdvice() {
        return new EncryptResultResponseBodyAdvice();
    }

    @Bean
    public DecryptRequestBodyAdvice decryptRequestBodyAdvice() {
        return new DecryptRequestBodyAdvice();
    }

}
