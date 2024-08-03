/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.model.Result;
import com.eryansky.common.utils.encode.RSAUtil;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.encrypt.anotation.DecryptRequestBody;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import com.eryansky.encrypt.enums.CipherMode;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
        data.put("encrypt", CipherMode.SM4.name());
        //动态密钥
        String sm4Key = Sm4Utils.generateKeyString();
        //RSA公钥
        data.put("publicKey", RSAUtil.getDefaultBase64PublicKey());
        //RSA加密密钥 RSA对密钥加密
        data.put("requestKey", RSAUtil.encrypt(sm4Key));

        //加密后的示例数据 模拟前端数据加密
        String requestData = "{\"key\":123}";
        String demoEncryptRequestData = Sm4Utils.encrypt(sm4Key,requestData);
        data.put("demoEncryptRequestData", demoEncryptRequestData);

        return Result.successResult().setData(data);
    }

    /**
     * 数据加密传输与解密
     *
     * @return
     */
    @DecryptRequestBody
    @EncryptResponseBody
    @PostMapping(value = "data")
    @ResponseBody
    public Result data(@RequestBody Map<String,Object> data, HttpServletRequest request, HttpServletResponse response) {
        logger.info("data:{}",data);
        return Result.successResult().setData(data);
    }

}
