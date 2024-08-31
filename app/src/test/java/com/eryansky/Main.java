/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky;

import com.eryansky.common.orm.mybatis.sensitive.IEncrypt;
import com.eryansky.common.orm.mybatis.sensitive.encrypt.AesSupport;
import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.*;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.google.common.collect.Maps;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.Map;

/**
 * @author Eryan
 * @date 2020-06-17 
 */
public class Main {
    public static void main(String[] args) throws Exception {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor cpu = hal.getProcessor();
        Map<String,Object> map = Maps.newHashMap();
        map.put("hal",hal);
        map.put("cpu",cpu);
        System.out.println(JsonMapper.toJsonString(map));

    }
}
