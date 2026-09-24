package com.eryansky.core.security.annotation;


import com.eryansky.core.security._enum.Logical;

import java.lang.annotation.*;

import static com.eryansky.core.security._enum.Logical.*;

/**
 * 需要的角色
 * @author Eryan
 * @date : 2014-06-11 20:06
 */
@Target({ElementType.TYPE, ElementType.METHOD,ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRoles {

    String[] value() default {};

    Logical logical() default AND;
}