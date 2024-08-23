package com.eryansky.client.common.rpc;

import java.lang.annotation.*;

/**
 * 通过该注解标识接口发布的应用信息
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCExchange {
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

    /**
     * 服务地址
     * @return
     */
    String serverUrl() default "";

    /**
     * 密钥
     * @return
     */
    String apiKey() default "";
    /**
     *
     * @return
     */

    String[] qualifiers() default {};

}
