package com.eryansky.core.rpc;

import java.lang.annotation.*;

/**
 * 通过该注解标识接口发布的应用信息
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomRpcApp {
    /**
     * 发布接口的应用名称 
     *
     * @return
     */
    String name() default "";

    String url() default "";

    /**
     * 应用的contentPath，通过server.servlet.context-path 属性配置
     *
     * @return
     */
    String contentPath() default "";
}
