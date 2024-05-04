package com.eryansky.common.orm.mybatis.sensitive.annotation;

import java.lang.annotation.*;

/**
 * json字段中需要脱敏的key字段以及key加密类型
 *
 * @author Eryan
 * @version 2019-12-13
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EncryptJSONFieldKey {
    /**
     * json中的key的类型
     *
     * @return key
     */
    String key();

    /**
     * 加密方法
     * @return
     */
    String type() default "";
}
