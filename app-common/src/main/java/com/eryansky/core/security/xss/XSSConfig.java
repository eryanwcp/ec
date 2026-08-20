package com.eryansky.core.security.xss;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * XSS序列化配置
 * 支持标注在：
 * 1. FIELD (DTO 字段)
 * 2. METHOD (Controller 接口方法)
 * 3. TYPE (Controller 类)
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface XSSConfig {

    /**
     * 是否启用xss序列化
     * @return
     */
    boolean serializer() default false;
    /**
     * 是否启用xss反序列化
     * @return
     */
    boolean deserializer() default false;
}