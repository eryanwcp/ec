package com.eryansky.common.orm.mybatis.interceptor;

/**
 * 默认实现租户接口
 * @author Eryan
 * @date 2016-06-29 
 */
public class DefaultTenantInfoImpl implements TenantInfo {

    @Override
    public String getTenantId() {
        return "0";
    }
}
