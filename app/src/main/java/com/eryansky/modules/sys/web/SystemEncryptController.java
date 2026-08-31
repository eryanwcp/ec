/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.model.Result;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.encrypt.anotation.DecryptRequestBody;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import com.eryansky.encrypt.config.EncryptProvider;
import com.eryansky.encrypt.enums.CipherMode;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.ConfigService;
import com.eryansky.modules.sys.service.LogService;
import com.eryansky.modules.sys.utils.UserUtils;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @Resource
    private ConfigService configService;
    @Resource
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
        data.put("requestEncrypt", CipherMode.AES.name());
        //动态密钥
        String requestEncryptSm4Key = Sm4Utils.generateHexKeyString();//hex编码
        String requestEncryptAesKey = Cryptos.getBase64EncodeKey();//AES base64编码
        //RSA公钥
        data.put("publicKey", EncryptProvider.publicKeyBase64());
        //RSA加密密钥 RSA对密钥加密
        data.put("requestEncryptKey", requestEncryptAesKey);
        data.put("requestEncryptAesKey", requestEncryptAesKey);
        data.put("requestEncryptSm4Key", requestEncryptSm4Key);

        //加密后的示例数据 模拟前端数据加密
        Map<String,Object> dataMap = Maps.newHashMap();
        dataMap.put("key0",0);
        dataMap.put("key1","123456");
        data.put("requestData", dataMap);
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
        return Result.successResult().setData(UserUtils.getUser(User.SUPERUSER_ID));
    }


}
