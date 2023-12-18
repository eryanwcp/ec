/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.vo;

import com.eryansky.common.utils.mapper.JsonMapper;

import java.util.HashMap;

/**
 * @author Eryan
 * @date 2023-12-15
 */
public class ExtendAttr extends HashMap<String,Object> {


    public ExtendAttr() {
    }

    @Override
    public String toString() {
        return JsonMapper.toJsonString(this);
    }

}
