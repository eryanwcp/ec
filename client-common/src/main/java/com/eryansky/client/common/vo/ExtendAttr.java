/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.client.common.vo;

import com.eryansky.common.utils.mapper.JsonMapper;

import java.util.HashMap;

/**
 * @author Eryan
 * @date 2023-12-15
 */
public class ExtendAttr extends HashMap<String,Object> {

    @Override
    public String toString() {
        return JsonMapper.toJsonString(this);
    }
}
