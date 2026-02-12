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
import com.eryansky.core.rpc.advice.EncryptRPCResponseBodyAdvice;
import com.eryansky.core.rpc.consumer.EcHttpContext;
import com.eryansky.core.rpc.consumer.EcServiceClient;
import com.eryansky.encrypt.anotation.DecryptRequestBody;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import com.eryansky.modules.sys.service.SystemService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.annotation.Resource;
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

    @Resource
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

    @DecryptRequestBody()
    @EncryptResponseBody(defaultHandle = false,handle = EncryptRPCResponseBodyAdvice.HANDLE)
    @ResponseBody
    @PostMapping(value = {"service"})
    public R service(HttpServletRequest request, HttpServletResponse response, @RequestBody JsonNode requestData) {
        EcHttpContext ecpHttpContext = EcHttpContext.getInstance();
        R r = null;
        try {
            if (null == requestData) {
                return R.rest(false).setMsg("参数错误：requestData");
            }
            String serviceName = requestData.get("serviceName").asText();
            String serviceMethod = requestData.get("serviceMethod").asText();
            JsonNode data = requestData.get("data");
            ecpHttpContext.setHttp(request, response);
            ecpHttpContext.setService(serviceName, serviceMethod);
            EcServiceClient ecpServiceClient = SpringContextHolder.getBean(EcServiceClient.class);
            ecpServiceClient.init(serviceName, serviceMethod);

            ArrayNode arrayNode= null;
            if (null != data) {
                StringBuilder builder = new StringBuilder();
                builder.append("[");
                Iterator<Map.Entry<String, JsonNode>> fields = data.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    builder.append(JsonMapper.toJsonString(entry.getValue()));
                    if (fields.hasNext()) {
                        builder.append(",");
                    }
                }
                builder.append("]");
                arrayNode = JsonMapper.getInstance().toArrayNode(builder.toString());
            }
            r = ecpServiceClient.callService(ecpHttpContext.getServiceContext(), arrayNode);
        } finally {
            EcHttpContext.removeInstance();
        }
        return r;
    }

}
