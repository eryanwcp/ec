/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.encrypt.anotation.DecryptRequestBody;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import com.eryansky.encrypt.util.EncryptUtils;
import com.eryansky.modules.sys.mapper.Config;
import com.eryansky.modules.sys.mapper.Log;
import com.eryansky.modules.sys.service.ConfigService;
import com.eryansky.modules.sys.service.LogService;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 数据加密、解密传输
 *
 * @author Eryan
 * @date 2024-06-28
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/encrypt")
public class SystemEncryptController extends SimpleController {

    @Autowired
    private ConfigService configService;
    @Autowired
    private LogService logService;

    /**
     * 初始密钥
     *
     * @return
     */
    @GetMapping(value = "initKey")
    @ResponseBody
    public Result initKey(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String,Object> data = Maps.newHashMap();
        //数据传输加密方法 支持SM4、AES
        data.put("encrypt", "AES");
        //动态密钥
//        String key = Sm4Utils.generateHexKeyString();//hex编码
        String key = Cryptos.getBase64EncodeKey();//AES base64编码
        //RSA公钥
//        data.put("publicKey", EncryptProvider.publicKeyBase64());
        data.put("publicKey", RSAUtils.getDefaultBase64PublicKey());
        //RSA加密密钥 RSA对密钥加密
        data.put("requestKey", RSAUtils.encryptBase64String(key,RSAUtils.getDefaultBase64PublicKey()));

        //加密后的示例数据 模拟前端数据加密
        Map<String,Object> dataMap = Maps.newHashMap();
        dataMap.put("key0",0);
        dataMap.put("key1","123456");
        String requestData = JsonMapper.toJsonString(dataMap);
        String demoEncryptRequestData = Cryptos.aesECBEncryptBase64String(requestData, key);
        data.put("demoEncryptRequestData", demoEncryptRequestData);

        return Result.successResult().setData(data);
    }

    /**
     * 数据加密传输与解密
     *
     * @return
     */
    @DecryptRequestBody()
    @EncryptResponseBody()
    @PostMapping(value = "data")
    @ResponseBody
    public Result data(@RequestBody String data, HttpServletRequest request, HttpServletResponse response) {
        logger.info("data:{}",data);
        return Result.successResult().setData(data);
    }


}
