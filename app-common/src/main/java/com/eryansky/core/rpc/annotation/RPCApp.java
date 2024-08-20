package com.eryansky.core.rpc.annotation;

import java.lang.annotation.*;

/**
 * 通过该注解标识接口发布的应用信息
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCApp {
    /**
     * 发布接口的应用名称 
     *
     * @return
     */
    String name();

    /**
     * 前缀
     * @return
     */

    String urlPrefix() default "/rest";

}
