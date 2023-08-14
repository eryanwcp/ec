package com.eryansky.core.aop.annotation;

import java.lang.annotation.*;

/**
 * 排队(串行执行)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueuePoll {
    /**
     * 缓存key
     *
     * @return
     */
    String region() default "queue_poll";

    /**
     * 队列名称
     * @return
     */
    String value() default "";
}
