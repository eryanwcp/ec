package com.eryansky.common.orm.mybatis.interceptor;

/**
 * 需要实现该接口用于获取租户
 * @author Eryan
 * @date 2016-06-29
 */
public interface TenantInfo {

    String getTenantId();

}
