/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.orm.mybatis.interceptor;

/**
 * 需要实现该接口用于获取租户
 * @author Eryan
 * @date 2016-06-29
 */
public interface TenantInfo {

    String getTenantId();

}
