/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky;

import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.mybatis.sensitive.IEncrypt;
import com.eryansky.common.orm.mybatis.sensitive.encrypt.AesSupport;
import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.*;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.monitor.domain.Server;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eryan
 * @date 2020-06-17 
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Page<User> page = new Page();
        page.setPageNo(2);
        page.setPageSize(20);
        Map<String, Object> params = new HashMap<>();
        params.put("orderDate","2024-10-11");
        // params.put("manageUnitId",);
//        Page<TradeOrderWholeDto> pageOrderByDate = tradeOrderWholeSaleService.findPageOrderByDate(page, params);
        String json = JsonMapper.toJsonString(page);
        System.out.println(json);
        page = JsonMapper.getInstance().toJavaObject(json, new TypeReference<Page<User>>() {
        });
        System.out.println(JsonMapper.toJsonString(page));

    }
}
