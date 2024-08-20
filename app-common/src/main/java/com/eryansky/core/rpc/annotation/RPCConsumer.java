package com.eryansky.core.rpc.annotation;

import java.lang.annotation.*;

/**
 * 声明一个消费者注解
 */
@Target(ElementType.FIELD) // 作用于字段上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RPCConsumer {
}
