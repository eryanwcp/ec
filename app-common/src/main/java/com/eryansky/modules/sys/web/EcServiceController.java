/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.model.R;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.core.rpc.advice.EncryptRPCResponseBodyAdvice;
import com.eryansky.core.rpc.consumer.EcHttpContext;
import com.eryansky.core.rpc.consumer.EcServiceClient;
import com.eryansky.core.security.annotation.RestApi;
import com.eryansky.encrypt.anotation.DecryptRequestBody;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 提供公共方法的Controller.
 *
 * @author Eryan
 * @date 2013-2-25 下午1:59:38
 */
@RestApi
@Controller
@RequestMapping(value = "/rest")
public class EcServiceController extends SimpleController {

    @DecryptRequestBody()
    @EncryptResponseBody(defaultHandle = false,handle = EncryptRPCResponseBodyAdvice.HANDLE)
    @ResponseBody
    @PostMapping(value = {"service"})
    public R service(HttpServletRequest request, HttpServletResponse response, @RequestBody JsonNode requestData) {
        EcHttpContext ecpHttpContext = EcHttpContext.getInstance();
        R ecpResultBean = null;
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

            List<Object> params = new ArrayList<>();
            // 遍历 JsonNode 并将其添加到 LinkedHashMap
            Iterator<Map.Entry<String, JsonNode>> fields = data.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                params.add(JsonMapper.getInstance().toJavaObject(entry.getValue(),Object.class));
            }
            ecpResultBean = ecpServiceClient.callService(ecpHttpContext.getServiceContext(), params.toArray());
        } finally {
            EcHttpContext.removeInstance();
        }
        return ecpResultBean;
    }

}
