package com.eryansky.core.rpc;

import java.lang.annotation.*;

/**
 * 声明服务提供方注解
 */
@Target({ElementType.TYPE}) // 作用于类上， 发布整个类的接口
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomRpcProvider {
}
