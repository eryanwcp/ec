/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.web.listener;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.*;

/**
 * 系统初始化监听.
 *
 * @author Eryan
 * @date 2012-12-11 下午4:56:54
 */
public class DefaultSystemInitListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSystemInitListener.class);

    public DefaultSystemInitListener() {
        // TODO document why this constructor is empty
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Properties props = System.getProperties();
        props.stringPropertyNames().stream().sorted().forEach(v -> {
            logger.info("{}={}", v, props.getProperty(v));
        });
//        logger.info("{}",props);
        logger.info("系统服务启动.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("系统服务关闭.");
    }

    /**
     * session创建
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        logger.debug("sessionCreated");
    }

    /**
     * session销毁
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent evt) {
        logger.debug("sessionDestroyed");
    }

    @Override
    public void attributeAdded(HttpSessionBindingEvent sbe) {
        logger.debug("attributeAdded");
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent sbe) {
        logger.debug("attributeRemoved");
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent sbe) {
        logger.debug("attributeReplaced");
    }

}
