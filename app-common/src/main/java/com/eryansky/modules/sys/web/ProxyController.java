/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.model.Result;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.http.HttpPoolCompoents;
import com.eryansky.common.web.filter.CustomHttpServletRequestWrapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

/**
 * 代理访问服务 (已迁移至 HttpClient 5.x)
 *
 * @author Eryan
 * @date 2015-12-14
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/proxy")
public class ProxyController extends SimpleController {

    /**
     * 判断URL是否允许代理
     */
    private Boolean isAuthProxyUrl(String url) {
        if (!AppConstants.isProxyEnable()) {
            logger.warn("系统未启用Proxy功能。");
            return false;
        }

        // 白名单校验优化：使用 anyMatch 替代 filter().findAny()
        Collection<String> whiteList = AppConstants.getProxyWhiteList();
        if (whiteList != null) {
            return whiteList.stream().anyMatch(v -> "*".equals(v) || StringUtils.simpleWildcardMatch(v, url));
        }
        return false;
    }

    /**
     * 代理访问（指定contentUrl参数）
     */
    @GetMapping(value = {""})
    public void getHttpProxy(NativeWebRequest nativeWebRequest, String contentUrl) throws Exception {
        CustomHttpServletRequestWrapper request = nativeWebRequest.getNativeRequest(CustomHttpServletRequestWrapper.class);
        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);

        doForwardProxy(request, response, contentUrl);
    }

    /**
     * 代理访问（通过RESTful通配符路径提取URL）
     */
    @GetMapping(value = {"**"})
    public ModelAndView proxy(NativeWebRequest nativeWebRequest) throws Exception {
        CustomHttpServletRequestWrapper request = nativeWebRequest.getNativeRequest(CustomHttpServletRequestWrapper.class);
        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);

        String requestUrl = request.getRequestURI();
        String contentUrl = StringUtils.substringAfterLast(requestUrl, AppConstants.getAdminPath() + "/sys/proxy/");

        doForwardProxy(request, response, contentUrl);
        return null;
    }

    /**
     * 核心路由转发公共方法（适配 HttpClient 5.x）
     */
    private void doForwardProxy(CustomHttpServletRequestWrapper request, HttpServletResponse response, String contentUrl) throws Exception {
        // 1. 检查URL是否允许代理
        if (!isAuthProxyUrl(contentUrl)) {
            logger.warn("未授权proxy访问权限：{}", contentUrl);
            renderError(request, response, "未授权访问权限!");
            return;
        }

        // 2. 拼接 URL 与请求参数
        String param = AppUtils.joinParasWithEncodedValue(WebUtils.getParametersStartingWith(request, null));
        String url = StringUtils.isNotBlank(param) ? contentUrl + "?" + param : contentUrl;
        logger.debug("proxy url：{}", url);

        // 3. 复制请求头
        Map<String, String> headers = Maps.newHashMap();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String header = enumeration.nextElement();
            headers.put(header, request.getHeader(header));
        }

        HttpPoolCompoents httpPoolCompoents = HttpPoolCompoents.getInstance();
        Response remoteResponse = null;
        HttpEntity entity = null;

        try {
            remoteResponse = httpPoolCompoents.getResponse(url, headers);
            if (remoteResponse == null) {
                renderError(request, response, "代理访问异常：" + contentUrl);
                return;
            }

            HttpResponse httpResponse = remoteResponse.returnResponse();

            // 【HC5】获取 Entity：必须将 HttpResponse 强转为 ClassicHttpResponse
            if (httpResponse instanceof ClassicHttpResponse) {
                entity = ((ClassicHttpResponse) httpResponse).getEntity();
            }

            // 4. 判断响应状态码（【HC5】移除了 StatusLine，直接使用 getCode()）
            if (httpResponse.getCode() >= 400) {
                String errorMsg = "代理访问异常：" + contentUrl;
                logger.error("{}, 状态码: {}", errorMsg, httpResponse.getCode());
                if (entity != null) {
                    logger.error("错误响应内容: {}", EntityUtils.toString(entity, StandardCharsets.UTF_8));
                }
                renderError(request, response, errorMsg);
                return;
            }

            // 5. 设置返回头（【HC5】getContentType 直接返回 String）
            if (entity != null && StringUtils.isNotBlank(entity.getContentType())) {
                response.setContentType(entity.getContentType());
            }

            // 安全处理 Content-Length 强转，防止大文件溢出
            if (entity != null && entity.getContentLength() > 0) {
                long contentLength = entity.getContentLength();
                if (contentLength <= Integer.MAX_VALUE) {
                    response.setContentLength((int) contentLength);
                } else {
                    response.setHeader("Content-Length", String.valueOf(contentLength));
                }
            }

            // 传递 Content-Disposition 头以支持文件下载等
            if (httpResponse.getHeaders() != null) {
                for (Header h : httpResponse.getHeaders()) {
                    if ("Content-Disposition".equalsIgnoreCase(h.getName())) {
                        response.setHeader(h.getName(), h.getValue());
                    }
                }
            }

            // 6. 输出流拷贝（通过 Try-with-resources 自动关闭远程输入流）
            if (entity != null) {
                try (InputStream input = entity.getContent();
                     OutputStream output = response.getOutputStream()) {
                    IOUtils.copy(input, output);
                    output.flush();
                }
            }
        } catch (IOException e) {
            logger.error("代理转发流处理异常: " + e.getMessage(), e);
            throw e;
        } finally {
            // 7. 必须释放 Entity 归还 HttpClient 连接池，杜绝连接泄露
            if (entity != null) {
                try {
                    EntityUtils.consume(entity);
                } catch (IOException e) {
                    logger.error("释放HttpEntity资源失败", e);
                }
            }
        }
    }

    /**
     * 抽取统一的错误渲染逻辑
     */
    private void renderError(CustomHttpServletRequestWrapper request, HttpServletResponse response, String errorMsg) throws IOException {
        if (WebUtils.isAjaxRequest(request)) {
            WebUtils.renderJson(response, Result.errorResult().setObj(errorMsg));
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, errorMsg);
        }
    }
}