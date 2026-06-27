/**
 * Copyright (c) 2012-2026 http://www.eryansky.com
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
import jakarta.annotation.Resource;
import org.apache.fory.resolver.AllowListChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Properties;

/**
 * 应用启动默认配置与校验器
 *
 * @author Eryan
 * @date 2024-08-01
 */
@Configuration
public class DefaultConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConfigurer.class);

    @Resource
    private ConfigService configService;

    /**
     * 校验数据库配置与本地文件配置的一致性
     */
    private void checkSysConfig() {
        List<Config> dbConfigs = configService.findList(new Config());
        PropertiesLoader propertiesLoader = AppConstants.getConfig();

        if (propertiesLoader == null) {
            return;
        }

        String[] resourcesPaths = propertiesLoader.getResourcesPaths();
        Properties properties = propertiesLoader.getProperties();

        if (logger.isInfoEnabled()) {
            String mode = AppConstants.isdevMode() ? "配置文件: " + (resourcesPaths != null && resourcesPaths.length > 0 ? resourcesPaths[0] : "未知") : "数据库";
            logger.info("系统参数读取模式：{}", mode);
        }

        // 优化点：消除原先 forEach 内部的 Stream 嵌套循环，改用 O(1) 的 Map.getProperty() 查询
        if (Collections3.isNotEmpty(dbConfigs) && properties != null) {
            for (Config config : dbConfigs) {
                if (config == null || config.getCode() == null) {
                    continue;
                }

                // Properties 本身就是 Map 结构，直接 getProperty 效率最高
                String fileValue = properties.getProperty(config.getCode());

                // 如果配置文件中存在该配置，且与数据库不一致，则发出警告
                if (fileValue != null && !ObjectUtils.isEquals(fileValue, config.getValue())) {
                    logger.warn("数据库与配置文件读取的系统参数配置不一致！Key: {} -> 数据库: {} | 配置文件: {}",
                            config.getCode(), config.getValue(), fileValue);
                }
            }
        }
    }

    /**
     * 动态配置序列化安全策略（白名单/黑名单校验）
     */
    private void checkSerializerTypeCheck() {
        AllowListChecker allowListChecker = ForySerializer.getTypeChecker();

        // 处理黑名单
        List<String> disallowClasses = AppConstants.getSerializerTypeCheckDisallowClassList();
        if (Collections3.isNotEmpty(disallowClasses)) {
            logger.info("SerializerTypeCheck disallowClasses : {}", JsonMapper.toJsonString(disallowClasses));
            allowListChecker.disallowClasses(disallowClasses);
        }

        // 处理白名单
        List<String> allowClassList = AppConstants.getSerializerTypeCheckAllowClassList();
        if (Collections3.isNotEmpty(allowClassList)) {
            logger.info("SerializerTypeCheck allowClasses : {}", JsonMapper.toJsonString(allowClassList));
            allowListChecker.allowClasses(allowClassList);
        }

        // 设置严格模式
        allowListChecker.setCheckLevel(AllowListChecker.CheckLevel.STRICT);
        logger.info("SerializerTypeCheck: {} checkLevel: {}", allowListChecker.getClass().getName(), allowListChecker.getCheckLevel().name());
    }

    /**
     * 清空本地临时缓存目录
     */
    private void clearTempDir() {
        logger.info("清空本地缓存目录：{}", AppConstants.getDiskTempDir());
        DiskUtils.clearTempDir();
        logger.info("清空本地缓存目录结束。");
    }

    /**
     * 监听 Spring Boot 启动完成事件
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        // 动态配置序列化安全策略
        checkSerializerTypeCheck();

        // 打印系统基础信息
        logger.info("当前启动系统：{}-V{}", AppConstants.getAppFullName(), AppConstants.getAppVersion());

        // 校验系统配置一致性
        checkSysConfig();

        // 打印存储状态并清理临时目录
        logger.info("文件存储方式：{}", AppConstants.getSystemDiskType());
        clearTempDir();

        // 打印访问入口
        logger.info("默认访问地址：{}", AppConstants.getAppURL());
    }
}