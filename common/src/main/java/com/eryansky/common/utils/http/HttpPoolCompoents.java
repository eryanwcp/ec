package com.eryansky.common.utils.http;

import com.eryansky.common.utils.StringUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.apache.http.HttpHost.DEFAULT_SCHEME_NAME;

/**
 * 基于HttpClient HTTP客户端组件
 * <br/>1、支持重试机制(3次).
 * 2、同一实例自动维护Cookie信息.
 * 3、支持gzip压缩
 *
 * @author Eryan
 * @date 2015-12-14
 */
public class HttpPoolCompoents {

    private static final Logger logger = LoggerFactory.getLogger(HttpPoolCompoents.class);

    /**
     * 连接超时时间 可以配到配置文件 （单位毫秒）
     */
    private static final int MAX_TIME_OUT = 30*1000;
    /**
     * 设置整个连接池最大连接数
     */
    private static final int POOL_MAX_CONN = 1024;
    /**
     * 设置单个路由默认连接数
     */
    private static final int POOL_MAX_PER_CONN = 256;

    /**
     * 连接丢失后,重试次数
     */
    private static final int MAX_EXECUT_COUNT = 3;

    private final String _DEFLAUT_CHARSET = "utf-8";
    /**
     * 设置超时，毫秒级别
     */
    private RequestConfig requestConfig = null;
    /**
     * 连接池
     */
    private PoolingHttpClientConnectionManager connectionManager;
    /**
     * Cookie存储
     */
    private final BasicCookieStore cookieStore = new BasicCookieStore();
    /**
     * 默认连接
     */
    private CloseableHttpClient httpClient;
    {
        try {
            httpClient = createHttpClient();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

    /**
     * @see #getInstance()
     */
    private HttpPoolCompoents() {
    }

    /**
     * @param requestConfig
     */
    private HttpPoolCompoents(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }

    /**
     * 创建新实例
     *
     * @return
     */
    public static HttpPoolCompoents newInstance() {
        return new HttpPoolCompoents();
    }

    /**
     * 创建新实例
     * @param requestConfig
     * @return
     */
    public static HttpPoolCompoents newInstance(RequestConfig requestConfig) {
        return new HttpPoolCompoents(requestConfig);
    }


    private static class HttpCompoentsHolder {
        private static final HttpPoolCompoents HTTP_POOL_COMPOENTS = new HttpPoolCompoents();
    }

    /**
     * 单例
     *
     * @return
     */
    public static HttpPoolCompoents getInstance() {
        return HttpCompoentsHolder.HTTP_POOL_COMPOENTS;
    }


    public CloseableHttpClient createHttpClient() throws Exception {
        // 保持连接时长
        ConnectionKeepAliveStrategy keepAliveStrat = new DefaultConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response,
                                             HttpContext context) {
                long keepAlive = super.getKeepAliveDuration(response,
                        context);
                if (keepAlive == -1) {// 如果服务器没有设置keep-alive这个参数，我们就把它设置成5秒
                    keepAlive = 5*1000;
                }
                return keepAlive;
            }
        };
        // 重试机制
        HttpRequestRetryHandler retryHandler = (exception, executionCount, context) -> {
            if (executionCount >= MAX_EXECUT_COUNT) {// 如果已经重试了3次，就放弃
                return false;
            }
            if (exception instanceof NoHttpResponseException) {
                // 如果服务器丢掉了连接，那么就重试
                return true;
            }
            if (exception instanceof InterruptedIOException) {// 超时
                return false;
            }
            if (exception instanceof UnknownHostException) {// 目标服务器不可达
                return false;
            }
            if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                return false;
            }
            if (exception instanceof SSLException) {// ssl握手异常
                return false;
            }
            HttpClientContext clientContext = HttpClientContext
                    .adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的，就再次尝试
            return !(request instanceof HttpEntityEnclosingRequest);
        };

        CookieSpecProvider easySpecProvider = context -> new DefaultCookieSpec() {
            @Override
            public void validate(Cookie cookie, CookieOrigin origin)
                    throws MalformedCookieException {
                // Oh, I am easy
            }
        };

        PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
        RFC6265CookieSpecProvider cookieSpecProvider = new RFC6265CookieSpecProvider(publicSuffixMatcher);
        Registry<CookieSpecProvider> r = RegistryBuilder
                .<CookieSpecProvider> create()
                .register(CookieSpecs.DEFAULT,
                        new DefaultCookieSpecProvider(publicSuffixMatcher))
                .register(CookieSpecs.STANDARD,cookieSpecProvider)
                .register(CookieSpecs.STANDARD_STRICT, cookieSpecProvider)
                .register("easy", easySpecProvider).build();

        requestConfig = RequestConfig.custom()
                .setCookieSpec("easy")
                .setSocketTimeout(MAX_TIME_OUT)
                .setConnectTimeout(MAX_TIME_OUT)
                .setConnectionRequestTimeout(MAX_TIME_OUT)
                .setRedirectsEnabled(true).build();

        SSLContext sslContext;
        // 信任所有
        sslContext = new SSLContextBuilder().loadTrustMaterial(null,
                (chain, authType) -> true).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
        // 创建SSLSocketFactory
        // 定义socket工厂类 指定协议（Http、Https）
        Registry registry = RegistryBuilder.create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslsf)//SSLConnectionSocketFactory.getSocketFactory()
                .build();

        connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(POOL_MAX_CONN);// 连接池最大并发连接数
        connectionManager.setDefaultMaxPerRoute(POOL_MAX_PER_CONN);// 单路由最大并发数
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(3000).build();
        //拦截器：返回增加gzip解压
        //增加gzip压缩请求
        Properties props = System.getProperties();
        String httpProxyHost = props.getProperty("http.proxyHost");
        String httpProxyPort = props.getProperty("http.proxyPort");
        String httpNonProxyHosts = props.getProperty("http.nonProxyHosts");
        String httpProxyUser = props.getProperty("http.proxyUser");
        String httpProxyPassword = props.getProperty("http.proxyPassword");
        HttpHost proxy = null;
        HttpRoutePlanner httpRoutePlanner = null;
        CredentialsProvider credentialsProvider = null;
        if(StringUtils.isNotBlank(httpProxyHost)){
            proxy = new HttpHost(httpProxyHost, Integer.valueOf(httpProxyPort),DEFAULT_SCHEME_NAME);
            httpRoutePlanner = new DefaultProxyRoutePlanner(proxy);
            if(StringUtils.isNotBlank(httpProxyUser)){
                credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        new AuthScope(proxy),
                        new UsernamePasswordCredentials(httpProxyUser, httpProxyPassword));

            }
        }
        return HttpClients.custom().setKeepAliveStrategy(keepAliveStrat)
                .setRetryHandler(retryHandler)
                .setDefaultCookieSpecRegistry(r)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCookieStore(cookieStore)
                .setDefaultSocketConfig(socketConfig)
                .setConnectionManager(connectionManager)
                .setRoutePlanner(httpRoutePlanner)
                .setDefaultCredentialsProvider(credentialsProvider)
                .addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                    if (!request.containsHeader("Accept-Encoding")) {
                        request.addHeader("Accept-Encoding", "gzip");
                    }
                })
                .addInterceptorFirst((HttpResponseInterceptor) (response, context) -> {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        Header ceheader = entity.getContentEncoding();
                        if (ceheader != null) {
                            HeaderElement[] codecs = ceheader.getElements();
                            for (HeaderElement codec : codecs) {
                                if (codec.getName().equalsIgnoreCase("gzip")) {
                                    response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                                    return;
                                }
                            }
                        }
                    }
                })
                //                    .setSslcontext(sslContext)
                .build();
    }
    /**
     * 设置超时
     *
     * @param socketTimeOut （单位：毫秒）
     * @param connectTimeOut （单位：毫秒）
     */
    public void setTimeOut(int socketTimeOut, int connectTimeOut) {
        requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeOut)
                .setConnectTimeout(connectTimeOut).build();
    }

    /**
     * GET请求
     *
     * @param url 请求地址
     * @return
     */
    public String get(String url) {
        return get(url, _DEFLAUT_CHARSET);
    }

    /**
     * GET请求
     *
     * @param url     请求地址
     * @param charset 编码
     * @return
     */
    public String get(String url, String charset) {
        return get(url, null, charset);
    }

    /**
     * GET请求
     *
     * @param url     请求地址
     * @param headers 自定义Header
     * @param charset 编码
     * @return
     */
    public String get(String url, Map<String, String> headers, String charset) {
        String useCharset = charset;
        if (charset == null) {
            useCharset = _DEFLAUT_CHARSET;
        }
        HttpGet httpGet = new HttpGet(url);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpGet.setHeader(entry.getKey(), entry.getValue());
            }
        }
        httpGet.setConfig(requestConfig);
        HttpEntity entity= null;
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            entity = response.getEntity();
            return EntityUtils.toString(entity, useCharset);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }


    /**
     * POST请求
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return
     */
    public String post(String url, Map<String, String> params) {
        return post(url, params, null, null);
    }

    /**
     * POST请求
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 自定义Header
     * @return
     */
    public String post(String url, Map<String, String> params,
                       Map<String, String> headers) {
        return post(url, params, headers, null);
    }

    /**
     * POST请求
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param charset 编码
     * @return
     */
    public String post(String url, Map<String, String> params, String charset) {
        return post(url, params, null, charset);
    }

    /**
     * POST请求
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 自定义Header
     * @param charset 编码
     * @return
     */
    public String post(String url, Map<String, String> params,
                       Map<String, String> headers, String charset) {
        String useCharset = charset;
        if (charset == null) {
            useCharset = _DEFLAUT_CHARSET;
        }
        try{
            HttpPost httpPost = new HttpPost(url);
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, Charset.forName(useCharset)));
            }
            httpPost.setConfig(requestConfig);
            HttpEntity entity = null;
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                entity = response.getEntity();
                return EntityUtils.toString(entity, useCharset);
            }
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
     * @return
     */
    public String post(String url, String data) {
        return  post(url, data,null,null);
    }
    /**
     * POST请求
     *
     * @param url     请求地址
     * @param data  请求json数据
     * @param headers 自定义Header
     * @param charset 编码
     * @return
     */
    public String post(String url, String data,
                       Map<String, String> headers, String charset) {
        String useCharset = charset;
        if (charset == null) {
            useCharset = _DEFLAUT_CHARSET;
        }
        try{
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
            HttpEntity entity= null;
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                 entity = response.getEntity();
                return EntityUtils.toString(entity, useCharset);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 新增Cooke信息
     * @see BasicClientCookie
     *
     * @return
     */
    public void addCookie(Cookie cookie) {
        cookieStore.addCookie(cookie);
    }

    /**
     * 获取Cooke信息
     *
     * @param key
     * @return
     */
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

    /**
     * 获取所有Cookie信息
     *
     * @return
     */
    public List<Cookie> getCookies() {
        return cookieStore.getCookies();
    }


    /**
     * 打印Cookie信息
     */
    public void printCookies() {
        List<Cookie> cookies = getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                logger.info(c.getName() + " :" + c.getValue());
                logger.info("	domain:" + c.getDomain());
                logger.info("	expires:" + c.getExpiryDate());
                logger.info("	path:" + c.getPath());
                logger.info("	version:" + c.getVersion());
            }
        }
    }

    /**
     * GET请求 FluentAPI
     *
     * @param url     请求地址
     * @return
     */
    public Response getResponse(String url) throws Exception {
        return getResponse(url,null);
    }

    /**
     * GET请求 FluentAPI
     *
     * @param url     请求地址
     * @param headers 自定义Header
     * @return
     */
    public Response getResponse(String url, Map<String, String> headers) throws Exception {
        try {
            Executor executor = getExecutor();
            Request request = Request.Get(url);
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    request.setHeader(entry.getKey(),entry.getValue());
                }
            }
            return executor.execute(request);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取执行器 FluentAPI
     *
     * @return
     */
    public Executor getExecutor() {
        return Executor.newInstance(httpClient);
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public RequestConfig getRequestConfig() {
        return requestConfig;
    }

    public PoolingHttpClientConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }
}