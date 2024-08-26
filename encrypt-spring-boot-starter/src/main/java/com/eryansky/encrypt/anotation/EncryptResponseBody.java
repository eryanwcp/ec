package com.eryansky.encrypt.anotation;

import java.lang.annotation.*;

/**
 * 加密注解
 *
 * @author : 尔演@Eryan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EncryptResponseBody {
    /**
     * 是否启用
     * @return
     */
    String enable() default "true";
    /**
     * 是否使用默认处理策略
     * @return
     */
    boolean defaultHandle() default true;
    /**
     * 处理策略
     * @return
     */
    String handle() default "";
}
