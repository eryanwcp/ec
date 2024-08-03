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
}
