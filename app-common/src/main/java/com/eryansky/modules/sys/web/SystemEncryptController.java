/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;


import com.eryansky.common.model.*;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.encrypt.anotation.Decrypt;
import com.eryansky.encrypt.anotation.Encrypt;
import com.eryansky.encrypt.config.EncryptProvider;
import com.eryansky.encrypt.enums.CipherMode;
import com.eryansky.encrypt.enums.Scenario;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 用户登录/注销等前端交互入口
 *
 * @author Eryan
 * @date : 2014-05-02 19:50
 */
@Controller
@RequestMapping(value = {"${adminPath}/sys/encrypt"})
public class SystemEncryptController extends SimpleController {

    /**
     * 欢迎页面
     *
     * @return
     */
    @Encrypt(enableAop = false,fields = {"data"},cipher = CipherMode.AES,scenario = Scenario.transmit,dynamic = false)
    @GetMapping(value = {"initKey"})
    @ResponseBody
    public Result initKey(HttpServletRequest request, HttpServletResponse response) {
        Map<String,Object> data = Maps.newHashMap();
        data.put("publicKey", EncryptProvider.publicKeyBase64());
        return Result.successResult().setData(data).setObj(data);
    }


    /**
     * 异步方式返回session信息
     * @reload 刷新Session信息
     */
    @Decrypt(enableAop = false,fields = {"data"},cipher = CipherMode.AES,scenario = Scenario.storage,dynamic = false)
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"data"})
    @ResponseBody
    public Result data(@RequestBody String data) {
        return Result.successResult().setData(data);
    }


}
