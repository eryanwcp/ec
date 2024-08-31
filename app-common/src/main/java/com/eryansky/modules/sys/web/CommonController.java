/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.model.R;
import com.eryansky.common.model.Result;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.utils.MediaTypes;
import com.eryansky.core.rpc.consumer.EcHttpContext;
import com.eryansky.core.rpc.consumer.EcServiceClient;
import com.eryansky.modules.sys.service.SystemService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.*;

/**
 * 提供公共方法的Controller.
 *
 * @author Eryan
 * @date 2013-2-25 下午1:59:38
 */
@Controller
@RequestMapping(value = "${adminPath}/common")
public class CommonController extends SimpleController {

    @Autowired
    private SystemService systemService;

    /**
     * JsonP跨域输出示例
     *
     * @param callbackName 回调方法
     * @return
     */
    @PostMapping(value = "mashup", produces = MediaTypes.JAVASCRIPT_UTF_8)
    @ResponseBody
    public String mashup(@RequestParam("callback") String callbackName) {

        // 设置需要被格式化为JSON字符串的内容.
        Map<String, String> map = Collections.singletonMap("content", "<p>你好，世界！</p>");

        // 渲染返回结果.
        return JsonMapper.getInstance().toJsonP(callbackName, map);
    }


    @PostMapping(value = {"service"})
    public R service(HttpServletRequest request, HttpServletResponse response, @RequestBody JsonNode requestData) {
        EcHttpContext ecpHttpContext = EcHttpContext.getInstance();
        R ecpResultBean = null;
        try {
            if (null == requestData) {
                return R.rest(false).setMsg("参数错误：requestData");
            }
            String serviceName = requestData.get("serviceName").asText();
            String method = requestData.get("serviceMethod").asText();
            JsonNode data = requestData.get("data");
            ecpHttpContext.setHttp(request, response);
            ecpHttpContext.setService(serviceName, method);
            EcServiceClient ecpServiceClient = SpringContextHolder.getBean(EcServiceClient.class);
            ecpServiceClient.init(serviceName, method);
            List<Object> params = new ArrayList<>();
            // 遍历 JsonNode 并将其添加到 LinkedHashMap
            Iterator<Map.Entry<String, JsonNode>> fields = data.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                params.add(entry.getValue());
            }
            ecpResultBean = ecpServiceClient.callService(ecpHttpContext.getServiceContext(), params.toArray());
        } finally {
            EcHttpContext.removeInstance();
        }
        return ecpResultBean;
    }

}
