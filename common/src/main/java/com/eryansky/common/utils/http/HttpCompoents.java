/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.http;

import com.eryansky.common.utils.StringUtils;
import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.entity.GzipDecompressingEntity;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.fluent.Executor;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 基于 HttpClient 5.x 的 HTTP 客户端组件
 * <br/>1、支持重试机制 (3次).
 * 2、同一实例自动维护 Cookie 信息.
 * 3、支持 gzip 压缩
 *
 * @author Eryan
 * @date 2015-12-14 (Migrated to HC5)
 */
public class HttpCompoents {
    private static final Logger logger = LoggerFactory.getLogger(HttpCompoents.class);

    private static final Timeout MAX_TIME_OUT = Timeout.ofMilliseconds(30 * 1000);
    private static final Timeout MAX_SOCKET_TIMEOUT = Timeout.ofMilliseconds(10 * 60 * 1000);

    private static final int POOL_MAX_CONN = 1024;
    private static final int POOL_MAX_PER_CONN = 256;
    private static final int MAX_EXECUTE_COUNT = 3;

    private final String _DEFLAUT_CHARSET = StandardCharsets.UTF_8.name();

    private RequestConfig requestConfig = null;
    private PoolingHttpClientConnectionManager connectionManager;
    private final BasicCookieStore cookieStore = new BasicCookieStore();

    private HttpCompoents() {
        this.requestConfig = RequestConfig.custom()
                .setConnectTimeout(MAX_TIME_OUT)
                .setConnectionRequestTimeout(MAX_TIME_OUT)
                .build();
    }

    private HttpCompoents(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }

    public static HttpCompoents newInstance() {
        return new HttpCompoents();
    }

    public static HttpCompoents newInstance(RequestConfig requestConfig) {
        return new HttpCompoents(requestConfig);
    }

    private static class HttpCompoentsHolder {
        private static final HttpCompoents httpCompoents = new HttpCompoents();
    }

    public static HttpCompoents getInstance() {
        return HttpCompoentsHolder.httpCompoents;
    }

    public CloseableHttpClient createHttpClient() throws Exception {
        return createHttpClient(null);
    }

    public CloseableHttpClient createHttpClient(String url) throws Exception {
        // 1. 保持连接时长策略
        ConnectionKeepAliveStrategy keepAliveStrat = new DefaultConnectionKeepAliveStrategy() {
            @Override
            public TimeValue getKeepAliveDuration(HttpResponse response, org.apache.hc.core5.http.protocol.HttpContext context) {
                TimeValue keepAlive = super.getKeepAliveDuration(response, context);
                if (keepAlive == null) {
                    return TimeValue.ofSeconds(5);
                }
                return keepAlive;
            }
        };

        // 2. HttpClient 5 重试策略
        HttpRequestRetryStrategy retryStrategy = new HttpRequestRetryStrategy() {
            @Override
            public boolean retryRequest(HttpRequest request, IOException exception, int executionCount, org.apache.hc.core5.http.protocol.HttpContext context) {
                if (executionCount >= MAX_EXECUTE_COUNT) return false;
                if (exception instanceof NoHttpResponseException) return true;
                if (exception instanceof InterruptedIOException || exception instanceof UnknownHostException || exception instanceof SSLException) {
                    return false;
                }
                return Method.GET.isSame(request.getMethod()) || Method.HEAD.isSame(request.getMethod());
            }

            @Override
            public boolean retryRequest(HttpResponse response, int executionCount, org.apache.hc.core5.http.protocol.HttpContext context) {
                return false;
            }

            @Override
            public TimeValue getRetryInterval(HttpResponse response, int executionCount, org.apache.hc.core5.http.protocol.HttpContext context) {
                return TimeValue.ofSeconds(1);
            }
        };

        // 3. SSL 信任所有
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                .build();
        SSLConnectionSocketFactory sslsf = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(sslContext)
                .build();

        // 4. 连接池配置
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(MAX_TIME_OUT)
                .setSocketTimeout(MAX_SOCKET_TIMEOUT)
                .build();

        connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslsf)
                .setMaxConnTotal(POOL_MAX_CONN)
                .setMaxConnPerRoute(POOL_MAX_PER_CONN)
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        // 5. 代理配置
        Properties props = System.getProperties();
        String httpProxyHost = props.getProperty("http.proxyHost");
        String httpProxyPort = props.getProperty("http.proxyPort");
        String httpNonProxyHosts = props.getProperty("http.nonProxyHosts");
        String httpProxyUser = props.getProperty("http.proxyUser");
        String httpProxyPassword = props.getProperty("http.proxyPassword");

        HttpHost proxy = null;
        HttpRoutePlanner httpRoutePlanner = null;
        BasicCredentialsProvider credentialsProvider = null;

        if (StringUtils.isNotBlank(httpProxyHost)) {
            proxy = new HttpHost(httpProxyHost, Integer.parseInt(httpProxyPort));
            httpRoutePlanner = new DefaultProxyRoutePlanner(proxy);
            if (StringUtils.isNotBlank(httpProxyUser)) {
                credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(proxy),
                        new UsernamePasswordCredentials(httpProxyUser, httpProxyPassword.toCharArray()));
            }
        }

        // 6. 构建 Client
        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setKeepAliveStrategy(keepAliveStrat)
                .setRetryStrategy(retryStrategy)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .setConnectionManager(connectionManager)
                // 拦截器：添加 Gzip 接收标识
                .addRequestInterceptorFirst((request, entity, context) -> {
                    if (!request.containsHeader(HttpHeaders.ACCEPT_ENCODING)) {
                        request.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");
                    }
                })
                // 拦截器：自动解压 Gzip
                .addResponseInterceptorFirst((response, entityDetails, context) -> {
                    // HttpClient 5 中需要强转为 ClassicHttpResponse 才能操作 Entity
                    if (response instanceof org.apache.hc.core5.http.ClassicHttpResponse) {
                        org.apache.hc.core5.http.ClassicHttpResponse classicResponse =
                                (org.apache.hc.core5.http.ClassicHttpResponse) response;

                        HttpEntity responseEntity = classicResponse.getEntity();
                        if (responseEntity != null && responseEntity.getContentEncoding() != null) {
                            if ("gzip".equalsIgnoreCase(responseEntity.getContentEncoding())) {
                                classicResponse.setEntity(new GzipDecompressingEntity(responseEntity));
                            }
                        }
                    }
                });

        if (httpRoutePlanner != null && StringUtils.isNotBlank(url)) {
            boolean isNonProxy = httpNonProxyHosts != null &&
                    Arrays.stream(httpNonProxyHosts.split(",")).anyMatch(url::contains);
            if (!isNonProxy) {
                httpClientBuilder.setRoutePlanner(httpRoutePlanner);
                if (credentialsProvider != null) {
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            }
        }

        return httpClientBuilder.build();
    }

    public void setTimeOut(int socketTimeOutMs, int connectTimeOutMs) {
        requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeOutMs))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeOutMs))
                .build();
    }

    public String get(String url) {
        return get(url, _DEFLAUT_CHARSET);
    }

    public String get(String url, String charset) {
        return get(url, null, charset);
    }

    public String get(String url, Map<String, String> headers, String charset) {
        String useCharset = (charset == null) ? _DEFLAUT_CHARSET : charset;
        HttpGet httpGet = new HttpGet(url);
        if (headers != null) {
            headers.forEach(httpGet::setHeader);
        }
        httpGet.setConfig(requestConfig);

        try (CloseableHttpClient httpClient = createHttpClient(url);
             CloseableHttpResponse response = httpClient.execute(httpGet)) {
            return EntityUtils.toString(response.getEntity(), Charset.forName(useCharset));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * GET请求
     *
     * @param url     请求地址
     * @param headers 自定义Header
     * @param callback 自定义结果回调函数
     * @return
     */
    public <T> T get(String url, Map<String, String> headers, HttpResponseCallback<T> callback) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }
        }
        httpGet.setConfig(requestConfig);
        try (CloseableHttpClient httpClient = createHttpClient(url);
             CloseableHttpResponse httpResponse =  httpClient.execute(httpGet)){
            return callback.handle(httpResponse);
        }
    }

    public String post(String url, Map<String, String> params) {
        return post(url, params, null, null);
    }

    public String post(String url, Map<String, String> params, Map<String, String> headers) {
        return post(url, params, headers, null);
    }

    public String post(String url, Map<String, String> params, String charset) {
        return post(url, params, null, charset);
    }

    public String post(String url, Map<String, String> params, Map<String, String> headers, String charset) {
        String useCharset = (charset == null) ? _DEFLAUT_CHARSET : charset;
        HttpPost httpPost = new HttpPost(url);
        if (headers != null) {
            headers.forEach(httpPost::setHeader);
        }

        if (params != null) {
            List<NameValuePair> nvps = new ArrayList<>();
            params.forEach((k, v) -> nvps.add(new BasicNameValuePair(k, v)));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName(useCharset)));
        }
        httpPost.setConfig(requestConfig);

        try (CloseableHttpClient httpClient = createHttpClient(url);
             CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity(), Charset.forName(useCharset));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * POST请求
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 自定义Header
     * @param charset 编码
     * @param callback 自定义结果回调函数
     * @return
     */
    public <T> T post(String url, Map<String, String> params,
                      Map<String, String> headers, String charset,HttpResponseCallback<T> callback) throws Exception {
        String useCharset = charset;
        if (charset == null) {
            useCharset = _DEFLAUT_CHARSET;
        }

        HttpPost httpPost = new HttpPost(url);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        List<NameValuePair> nvps = new ArrayList<>();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName(useCharset)));
        }
        httpPost.setConfig(requestConfig);
        try (CloseableHttpClient httpClient = createHttpClient(url);
             CloseableHttpResponse httpResponse =  httpClient.execute(httpPost)){
            return callback.handle(httpResponse);
        }
    }

    public String post(String url, String data) {
        return post(url, data, null, null);
    }

    public String post(String url, String data, Map<String, String> headers, String charset) {
        String useCharset = (charset == null) ? _DEFLAUT_CHARSET : charset;
        HttpPost httpPost = new HttpPost(url);
        if (headers != null) {
            headers.forEach(httpPost::setHeader);
        }

        if (data != null) {
            StringEntity requestEntity = new StringEntity(data, org.apache.hc.core5.http.ContentType.APPLICATION_JSON, useCharset, false);
            httpPost.setEntity(requestEntity);
        }
        httpPost.setConfig(requestConfig);

        try (CloseableHttpClient httpClient = createHttpClient(url);
             CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity(), Charset.forName(useCharset));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * POST请求
     *
     * @param url     请求地址
     * @param data  请求json数据
     * @param headers 自定义Header
     * @param charset
     * @param callback 自定义结果回调函数
     * @return
     */
    public <T> T  post(String url, String data,
                       Map<String, String> headers, String charset,HttpResponseCallback<T> callback) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        // Create request data
        StringEntity requestEntity = new StringEntity(data, ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);
        httpPost.setConfig(requestConfig);
        try (CloseableHttpClient httpClient = createHttpClient(url);
             CloseableHttpResponse httpResponse =  httpClient.execute(httpPost)){
            return callback.handle(httpResponse);
        }
    }

    public void addCookie(Cookie cookie) {
        cookieStore.addCookie(cookie);
    }

    public String getCookie(String key) {
        List<Cookie> cookies = getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals(key)) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    public List<Cookie> getCookies() {
        return cookieStore.getCookies();
    }

    /* ====================================================================
     * Fluent API (HttpClient 5)
     * ==================================================================== */

    public Response getResponse(CloseableHttpClient httpClient, String url) throws Exception {
        return getResponse(httpClient, url, null);
    }

    public Response getResponse(CloseableHttpClient httpClient, String url, Map<String, String> headers) throws Exception {
        try {
            Executor executor = Executor.newInstance(httpClient);
            Request request = Request.get(url);
            if (headers != null) {
                headers.forEach(request::addHeader);
            }
            return executor.execute(request);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public RequestConfig getRequestConfig() {
        return requestConfig;
    }

    public PoolingHttpClientConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }
}