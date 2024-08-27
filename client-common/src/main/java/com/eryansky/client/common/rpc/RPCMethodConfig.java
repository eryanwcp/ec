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
     * 加密方式
     * @see CipherMode
     */
    String encrypt() default "";
    /**
     * 加密密钥
     */
    String encryptKey() default "";;

}
