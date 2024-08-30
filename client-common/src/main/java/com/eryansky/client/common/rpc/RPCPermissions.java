/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 */
package com.eryansky.client.common.rpc;

import com.eryansky.client.common._enum.Logical;

import java.lang.annotation.*;


/**
 * 需要的权限
 * @author Eryan
 * @date : 2014-06-11 20:06
 */
@Target({ElementType.TYPE, ElementType.METHOD,ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RPCPermissions {

    String[] value();

    Logical logical() default Logical.AND;

}