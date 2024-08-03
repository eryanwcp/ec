package com.eryansky.core.rpc;

import java.lang.annotation.*;

/**
 * RPC方法发布配置注解
 */
@Target({ElementType.METHOD}) // 作用于方法上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomRpcMethodConfig {

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

}
