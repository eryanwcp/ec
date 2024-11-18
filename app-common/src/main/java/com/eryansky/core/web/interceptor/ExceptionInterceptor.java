/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 */
package com.eryansky.core.web.interceptor;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.exception.SystemException;
import com.eryansky.common.model.Result;
import com.eryansky.common.utils.Exceptions;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.SysConstants;
import com.eryansky.common.utils.SysUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.EncodeUtils;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.encrypt.advice.EncryptResultResponseBodyAdvice;
import com.eryansky.encrypt.config.EncryptProvider;
import com.eryansky.encrypt.enums.CipherMode;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.eryansky.common.web.utils.WebUtils.JSON_TYPE;

/**
 * @author Eryan
 * @date : 2014-05-05 12:59
 */
public class ExceptionInterceptor implements HandlerExceptionResolver {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    private final static String MSG_DETAIL = " 详细信息:";
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String requestUrl = request.getRequestURI();
        requestUrl = requestUrl.replaceAll("//","/");

        Result result = null;
        //非Ajax请求 将跳转到500错误页面
//        if(!WebUtils.isAjaxRequest(request)){
//            throw ex;
//        }
        //Ajax方式返回错误信息
        String emsg = ex.getMessage();
        StringBuilder sb = new StringBuilder();

        boolean isWarn = false;//是否是警告级别的异常
        Object obj = null;//其它信息
        String loginName = SecurityUtils.getCurrentUserLoginName();
        if(null != loginName){
            sb.append(loginName).append(",");
        }
        sb.append("访问出现异常:");
        if("ClientAbortException".equals(ex.getClass().getSimpleName())){
            return null;
        }
        //参数类异常 Spring Assert、Apache Common Validate抛出该异常
        else if(Exceptions.isCausedBy(ex, IllegalArgumentException.class)){
            isWarn = true;
            sb.append(SysUtils.jsonStrConvert(emsg));//将":"替换为","
        }
        //空指针异常
        else if(Exceptions.isCausedBy(ex, NullPointerException.class)){
            sb.append("空指针异常，请刷新后重试或者联系管理员！");
//            sb.append("空指针异常！");
            if(SysConstants.isdevMode()){
                sb.append(MSG_DETAIL).append(SysUtils.jsonStrConvert(emsg));//将":"替换为","
            }
        }

        //业务异常
        else if(Exceptions.isCausedBy(ex, ServiceException.class)){
            ServiceException serviceException = (ServiceException) ex;
            result = new Result(serviceException.getCode() == null ? Result.ERROR:serviceException.getCode(), serviceException.getMessage(), serviceException.getObj());
        }

        //系统异常
        else if(Exceptions.isCausedBy(ex, SystemException.class)){
            sb.append(SysUtils.jsonStrConvert(emsg));//将":"替换为","
        }

        //Action异常
        else if(Exceptions.isCausedBy(ex, ActionException.class)){
            sb.append(SysUtils.jsonStrConvert(emsg));//将":"替换为","
        }

        //其它异常
        else{
            if(SysConstants.isdevMode()){
                sb.append(MSG_DETAIL).append(SysUtils.jsonStrConvert(emsg));//将":"替换为","
            }else{
                sb.append("未知异常，请刷新后重试或者联系管理员！");
            }
        }
        if(isWarn){
            result = new Result(Result.WARN,sb.toString(),obj);
            logger.warn(IpUtils.getIpAddr0(request) +" " + request.getRequestURI()+ " " +loginName + ":" + result.toString(), ex);
        }else{
            if(result == null){
                result = new Result(Result.ERROR,sb.toString(),obj);
            }
            logger.error(IpUtils.getIpAddr0(request) +" " + request.getRequestURI()+ " " +loginName + ":" + result.toString(), ex);
        }
//        Map<String, Object> model = Maps.newHashMap();
//        model.put("ex", ex);
//        return  new ModelAndView("error-business", model);

        //异步方式返回异常信息
        if(StringUtils.startsWith(requestUrl, request.getContextPath()+"/rest") || WebUtils.isAjaxRequest(request)){
            result.setCode(Result.ERROR_API);

            //数据加密
            String requestEncrypt = request.getHeader(EncryptResultResponseBodyAdvice.ENCRYPT);
            String requestEncryptKey = request.getHeader(EncryptResultResponseBodyAdvice.ENCRYPT_KEY);
            String data = JsonMapper.toJsonString(result);
            if (StringUtils.isNotBlank(requestEncrypt)) {
                if (CipherMode.SM4.name().equals(requestEncrypt) && StringUtils.isNotBlank(requestEncryptKey)) {
                    if (StringUtils.isNotBlank(data) && !StringUtils.equals(data, "null")) {
                        try {
                            String key = null;
                            try {
                                key = RSAUtils.decryptHexString(requestEncryptKey, EncryptProvider.privateKeyBase64());
                            } catch (Exception e) {
                                key = requestEncryptKey;
                            }
                            data = Sm4Utils.encrypt(key, data);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    }
                } else if (CipherMode.AES.name().equals(requestEncrypt) && StringUtils.isNotBlank(requestEncryptKey)) {
                    if (StringUtils.isNotBlank(data) && !StringUtils.equals(data, "null")) {
                        try {
                            String key = null;
                            try {
                                key = RSAUtils.decryptBase64String(requestEncryptKey, EncryptProvider.privateKeyBase64());
                            } catch (Exception e) {
                                key = requestEncryptKey;
                            }
                            data = Cryptos.aesECBEncryptBase64String(data, key);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    }

                } else if (CipherMode.BASE64.name().equals(requestEncrypt)) {
                    if (StringUtils.isNotBlank(data) && !StringUtils.equals(data, "null")) {
                        try {
                            data = EncodeUtils.base64Encode(data.getBytes(StandardCharsets.UTF_8));
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            throw new RuntimeException(e);
                        }
                    }

                }
            }

            WebUtils.renderJson(response, JSON_TYPE,data);
        }
//        WebUtils.renderText(response, result);

        ModelAndView modelAndView = new ModelAndView();
        Map<String, Object> maps = Maps.newHashMap();
        maps.put("code", result.getCode());
        maps.put("msg", result.getMsg());
        maps.put("obj", result.getObj());
        maps.put("data", result.getData());
        MappingJackson2JsonView mappingJackson2JsonView = new MappingJackson2JsonView();
        mappingJackson2JsonView.setAttributesMap(maps);
        modelAndView.setView(mappingJackson2JsonView);
        return modelAndView;
    }
}
