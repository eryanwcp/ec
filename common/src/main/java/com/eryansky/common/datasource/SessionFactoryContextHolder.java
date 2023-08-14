/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.datasource;

/**
 * @author Eryan
 * @date 2014-08-13
 */
public abstract class SessionFactoryContextHolder {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

    public static void setSessionFactoryType(String sessionFactory) {
        contextHolder.set(sessionFactory);
    }

    public static String getSessionFactory() {
        return contextHolder.get();
    }

    public static void clearSessionFactory() {
        contextHolder.remove();
    }
}