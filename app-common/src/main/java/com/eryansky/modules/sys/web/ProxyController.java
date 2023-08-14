/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
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
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

/**
 * 代理访问服务
 *
 * @author Eryan
 * @date 2015-12-14
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/proxy")
public class ProxyController extends SimpleController {


    /**
     * 判断URL是否允许代理
     *
     * @param url
     */
    private Boolean isAuthProxyUrl(String url) {
        boolean proxyEnable = AppConstants.isProxyEnable();
        if(!proxyEnable){
            logger.warn("系统未启用Proxy功能。");
            return false;
        }
        //白名单
        Collection<String> whiteList = AppConstants.getProxyWhiteList();
        if (null != whiteList && null != whiteList.stream().filter(v->"*".equals(v) || StringUtils.simpleWildcardMatch(v,url)).findAny().orElse(null)) {
            return true;
        }
        return false;
    }

    /**
     * 代理访问
     *
     * @param nativeWebRequest
     * @param contentUrl       远程URL
     * @throws IOException
     */
    @GetMapping(value = {""})
    public void getHttpProxy(NativeWebRequest nativeWebRequest, String contentUrl) throws Exception {
        CustomHttpServletRequestWrapper request = nativeWebRequest.getNativeRequest(CustomHttpServletRequestWrapper.class);
        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);
        //检查URL是否允许代理
        if(!isAuthProxyUrl(contentUrl)){
            logger.warn("未授权proxy访问权限：{}",contentUrl);
            String errorMsg = "未授权访问权限!";
            if (WebUtils.isAjaxRequest(request)) {
                WebUtils.renderJson(response, Result.errorResult().setObj(errorMsg));
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, errorMsg);
            }
            return;
        }

        HttpPoolCompoents httpPoolCompoents = HttpPoolCompoents.getInstance();//获取当前实例 可自动维护Cookie信息
        String param = AppUtils.joinParasWithEncodedValue(WebUtils.getParametersStartingWith(request, null));//请求参数
        String url = contentUrl + "?" + param;
        logger.debug("proxy url：{}", url);
        Map<String,String> headers = Maps.newHashMap();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()){
            String header = enumeration.nextElement();
            headers.put(header,request.getHeader(header));
        }
        Response remoteResponse= null;
        HttpEntity entity = null;
        try{
            remoteResponse = httpPoolCompoents.getResponse(url,headers);
            // 判断返回值
            if (remoteResponse == null) {
                String errorMsg = "代理访问异常：" + contentUrl;
                logger.error(errorMsg);
                if (WebUtils.isAjaxRequest(request)) {
                    WebUtils.renderJson(response, Result.errorResult().setObj(errorMsg));
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, errorMsg);
                }
                return;
            }
            HttpResponse httpResponse = remoteResponse.returnResponse();
            entity = httpResponse.getEntity();
            // 判断返回值
            if (httpResponse.getStatusLine().getStatusCode() >= 400) {
                String errorMsg = "代理访问异常：" + contentUrl;
                logger.error(errorMsg);
                logger.error(httpResponse.getStatusLine().getStatusCode() + "");
                logger.error(EntityUtils.toString(entity, "utf-8"));
                if (WebUtils.isAjaxRequest(request)) {
                    WebUtils.renderJson(response, Result.errorResult().setObj(errorMsg));
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, errorMsg);
                }
                return;
            }


            // 设置Header
            if(null != entity.getContentType()){
                response.setContentType(entity.getContentType().getValue());
            }
            if (entity.getContentLength() > 0) {
                response.setContentLength((int) entity.getContentLength());
            }
            Header[] allHeaders = httpResponse.getAllHeaders();
            if(null != allHeaders){
                for(Header h:allHeaders){
                    if("Content-Disposition".equalsIgnoreCase(h.getName())){
                        response.setHeader(h.getName(),h.getValue());
                    }
                }
            }
            // 输出内容
            InputStream input = entity.getContent();
            OutputStream output = response.getOutputStream();
            // 基于byte数组读取InputStream并直接写入OutputStream, 数组默认大小为4k.
            IOUtils.copy(input, output);
            output.flush();
        } finally {
            //回收链接到连接池
//            try {
//                EntityUtils.consume(entity);
//                if (null != remoteResponse) {
//                    remoteResponse.discardContent();
//                }
//            } catch (IOException e) {
//                logger.error(e.getMessage(), e);
//            }
        }
    }


    /**
     * 代理访问
     *
     * @param nativeWebRequest
     * @throws IOException
     */
    @GetMapping(value = {"**"})
    public ModelAndView proxy(NativeWebRequest nativeWebRequest) throws Exception {
        CustomHttpServletRequestWrapper request = nativeWebRequest.getNativeRequest(CustomHttpServletRequestWrapper.class);
        String requestUrl = request.getRequestURI();

        String contentUrl = StringUtils.substringAfterLast(requestUrl, AppConstants.getAdminPath() + "/sys/proxy/");
        String param = AppUtils.joinParasWithEncodedValue(WebUtils.getParametersStartingWith(request, null));//请求参数
        String url = contentUrl + "?" + param;
        logger.debug("proxy url：{}", url);
        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);
        //检查URL是否允许代理
        if(!isAuthProxyUrl(contentUrl)){
            logger.warn("未授权proxy访问权限：{}",contentUrl);
            String errorMsg = "未授权访问权限!";
            if (WebUtils.isAjaxRequest(request)) {
                WebUtils.renderJson(response, Result.errorResult().setObj(errorMsg));
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, errorMsg);
            }
            return null;
        }

        HttpPoolCompoents httpCompoents = HttpPoolCompoents.getInstance();//获取当前实例 可自动维护Cookie信息
        Map<String,String> headers = Maps.newHashMap();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()){
            String header = enumeration.nextElement();
            headers.put(header,request.getHeader(header));
        }
        Response remoteResponse= null;
        HttpEntity entity = null;
        try {
            remoteResponse = httpCompoents.getResponse(url,headers);
            // 判断返回值
            if (remoteResponse == null) {
                String errorMsg = "代理访问异常：" + contentUrl;
                logger.error(errorMsg);
                if (WebUtils.isAjaxRequest(request)) {
                    WebUtils.renderJson(response, Result.errorResult().setObj(errorMsg));
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, errorMsg);
                }
                return null;
            }
            HttpResponse httpResponse = remoteResponse.returnResponse();
            entity = httpResponse.getEntity();
            // 判断返回值
            if (httpResponse.getStatusLine().getStatusCode() >= 400) {
                String errorMsg = "代理访问异常：" + contentUrl;
                logger.error(errorMsg);
                logger.error(httpResponse.getStatusLine().getStatusCode() + "");
                logger.error(EntityUtils.toString(entity, "utf-8"));
                if (WebUtils.isAjaxRequest(request)) {
                    WebUtils.renderJson(response, Result.errorResult().setObj(errorMsg));
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, errorMsg);
                }
                return null;
            }


            // 设置Header
            if(null != entity.getContentType()){
                response.setContentType(entity.getContentType().getValue());
            }
            if (entity.getContentLength() > 0) {
                response.setContentLength((int) entity.getContentLength());
            }
            Header[] allHeaders = httpResponse.getAllHeaders();
            if(null != allHeaders){
                for(Header h:allHeaders){
                    if("Content-Disposition".equalsIgnoreCase(h.getName())){
                        response.setHeader(h.getName(),h.getValue());
                    }
                }
            }
            // 输出内容
            InputStream input = entity.getContent();
            OutputStream output = response.getOutputStream();
            // 基于byte数组读取InputStream并直接写入OutputStream, 数组默认大小为4k.
            IOUtils.copy(input, output);
            output.flush();
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        } finally {
            //回收链接到连接池
//            try {
//                EntityUtils.consume(entity);
//                if (null != remoteResponse) {
//                    remoteResponse.discardContent();
//                }
//            } catch (IOException e) {
//                logger.error(e.getMessage(), e);
//            }
        }
        return null;
    }

}