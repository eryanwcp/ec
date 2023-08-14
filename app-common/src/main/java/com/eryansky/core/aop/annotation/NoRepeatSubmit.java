package com.eryansky.core.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防止重复提交标记注解
 * 仅限作用在返回值为{@link com.eryansky.common.model.Result}的Controller方法上
 *
 * @author Eryan
 * @date : 2021-11-05
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoRepeatSubmit {

    /**
     * 缓存key
     *
     * @return
     */
    String region() default "lock_no_repeat_submit_default";

    /*
     * 设置请求锁定时间(无效配置，请在缓存中配置caffeine.properties) 单位：秒
     * @return
     */
    @Deprecated
    int lockTime() default 5;
}
