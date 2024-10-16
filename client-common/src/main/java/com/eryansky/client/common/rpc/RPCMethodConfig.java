package com.eryansky.client.common.rpc;

import java.lang.annotation.*;

/**
 * RPC方法发布配置注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCMethodConfig {

    /**
     * 不加密
     */
    String ENCRYPT_NONE = "none";
    String ENCRYPT_AES = "AES";
    String ENCRYPT_SM4 = "SM4";

    /**
     * 发布方法取别名
     *
     * @return
     */
    String alias() default "";

    /**
     * 标识该方法是否禁止发布
     *
     * @return
     */
    boolean isForbidden() default false;

    /**
     * 加密方式 默认: 支持的加密方式AES、SM4、NONE
     * 支持表达式 ${ec.api.encrypt:AES}
     *
     * @see CipherMode
     */
    String encrypt() default "";

}
