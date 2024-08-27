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
     * 不加密
     */
    String ENCRYPT_NONE = "none";
    String ENCRYPT_AES = "AES";
    String ENCRYPT_SM4 = "SM4";

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
     * 支持表达式 ${ec.api.serverUrl:}
     * @return
     */
    String serverUrl() default "";

    /**
     * 密钥
     *  支持表达式 ${ec.api.apiKey:}
     * @return
     */
    String apiKey() default "";
    /**
     * 别名
     * @return
     */
    String[] qualifiers() default {};

    /**
     * 加密方式 默认:不加密 支持的加密方式AES、SM4
     * 支持表达式 ${ec.api.encrypt:AES}
     *
     * @see CipherMode
     */
    String encrypt() default "";

}
