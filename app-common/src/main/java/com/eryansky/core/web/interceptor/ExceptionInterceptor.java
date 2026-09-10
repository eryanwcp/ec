/**
 *  Copyright (c) 2012-2026 https://www.eryansky.com
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
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.encrypt.util.RequestEncryptUtils;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Map;

/**
 * @author Eryan
 * @date : 2014-05-05 12:59
 */
public class ExceptionInterceptor implements HandlerExceptionResolver {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final JsonMapper jsonMapper = JsonMapper.getInstance();
    private static final String MSG_DETAIL = " 详细信息:";

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         @NonNull Object handler,
                                         Exception ex) {
        // 1. 客户端中断连接异常直接忽略（如用户在下载/加载时取消请求）
        if ("ClientAbortException".equals(ex.getClass().getSimpleName())) {
            return null;
        }

        String requestUrl = request.getRequestURI().replace("//", "/");
        String emsg = ex.getMessage();
        StringBuilder sb = new StringBuilder();
        boolean isWarn = false; // 是否是警告级别的异常
        Result result = null;

        // 拼接用户信息
        String loginName = SecurityUtils.getCurrentUserLoginName();
        if (StringUtils.isNotBlank(loginName)) {
            sb.append(loginName).append(",");
        }
        sb.append("访问出现异常:");

        // 2. 根据异常类型构建错误信息和 Result 对象
        if (Exceptions.isCausedBy(ex, IllegalArgumentException.class)) {
            // 参数类异常
            isWarn = true;
            sb.append(SysUtils.jsonStrConvert(emsg));
        } else if (Exceptions.isCausedBy(ex, NullPointerException.class)) {
            // 空指针异常
            sb.append("空指针异常，请刷新后重试或者联系管理员！");
            if (SysConstants.isdevMode()) {
                sb.append(MSG_DETAIL).append(SysUtils.jsonStrConvert(emsg));
            }
        } else if (Exceptions.isCausedBy(ex, ServiceException.class)) {
            // 业务异常
            ServiceException serviceException = (ServiceException) ex;
            int code = serviceException.getCode() != null ? serviceException.getCode() : Result.ERROR;
            result = new Result(code, serviceException.getMessage(), serviceException.getObj());
        } else if (Exceptions.isCausedBy(ex, SystemException.class) || Exceptions.isCausedBy(ex, ActionException.class)) {
            // 系统异常 / Action异常
            sb.append(SysUtils.jsonStrConvert(emsg));
        } else {
            // 其它未知异常
            if (SysConstants.isdevMode()) {
                sb.append(MSG_DETAIL).append(SysUtils.jsonStrConvert(emsg));
            } else {
                sb.append("未知异常，请刷新后重试或者联系管理员！");
            }
        }

        // 统一构建非 ServiceException 情况下的 Result 对象
        if (result == null) {
            result = new Result(isWarn ? Result.WARN : Result.ERROR, sb.toString(), null);
        }

        // 3. 打印日志
        String logMsg = String.format("%s %s %s:%s", IpUtils.getIpAddr0(request), requestUrl, loginName, result);
        if (isWarn) {
            logger.warn(logMsg, ex);
        } else {
            logger.error(logMsg, ex);
        }

        // 4. 处理 AJAX / JSON 异步请求拦截与加密响应
        if (WebUtils.JSON_TYPE.equals(request.getContentType()) || WebUtils.isAjaxRequest(request)) {
            result.setCode(Result.ERROR_API);

            // 数据加密判断与处理
            String encrypt = WebUtils.getHeaderIgnoreCase(request, RequestEncryptUtils.ENCRYPT);
            String encryptKey = WebUtils.getHeaderIgnoreCase(request, RequestEncryptUtils.ENCRYPT_KEY);

            if (StringUtils.isNotBlank(encrypt) && StringUtils.isNotBlank(encryptKey)) {
                try {
                    if (result.getObj() != null) {
                        byte[] objBytes = jsonMapper.writeValueAsBytes(result.getObj());
                        String encryptedObj = RequestEncryptUtils.encryptDataStringByRequest(encrypt, encryptKey, objBytes);
                        result.setObj(encryptedObj);
                    }
                } catch (Exception e) {
                    logger.error("响应数据加密异常, URI: {}, EncryptType: {}, Error: {}", requestUrl, encrypt, e.getMessage(), e);
                }
            }

            // 直接通过 Response 渲染 JSON 数据
            WebUtils.renderJson(response, result);

            // 重要修复：当已通过 HttpServletResponse 直接输出内容时，必须返回空的 ModelAndView，
            // 阻止 Spring MVC 继续渲染默认视图或重复写入 Response Buffer。
            return new ModelAndView();
        }

        // 5. 非 AJAX 请求处理：返回 JSON View
        ModelAndView modelAndView = new ModelAndView();
        Map<String, Object> maps = Maps.newHashMapWithExpectedSize(4);
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