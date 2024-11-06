package com.eryansky.configure;

import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.jackson.XssDefaultJsonDeserializer;
import com.eryansky.common.utils.jackson.XssDefaultJsonSerializer;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.dialect.dialect.ShiroDialect;
import com.eryansky.core.security.interceptor.*;
import com.eryansky.core.web.interceptor.LogInterceptor;
import com.eryansky.core.web.interceptor.MobileInterceptor;
import com.eryansky.modules.disk.extend.DISKManager;
import com.eryansky.modules.disk.extend.IFileManager;
import com.eryansky.utils.AppConstants;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.*;

import static com.fasterxml.jackson.core.JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION;

@Configuration
public class MvcConfigurer implements WebMvcConfigurer {

    @Lazy
    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                        "/webjars/**",
                        "/img/**",
                        "/css/**",
                        "/js/**")
                .addResourceLocations(
                        "classpath:/webjars/",
                        "classpath:/META-INF/resources/webjars/",
                        "classpath:/static/img/",
                        "classpath:/static/css/",
                        "classpath:/static/js/")
                .resourceChain(false);
    }

    /**
     * 配置拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new IpLimitInterceptor())
                .addPathPatterns("/**")
                .order(Ordered.HIGHEST_PRECEDENCE + 90);

        if (Boolean.TRUE.equals(AppConstants.isLimitUrlEnable())) {
            registry.addInterceptor(new UrlLimitInterceptor())
                    .addPathPatterns("/**")
                    .order(Ordered.HIGHEST_PRECEDENCE + 90);
        }

//        registry.addInterceptor(new LogInterceptor(requestMappingHandlerAdapter))
//                .addPathPatterns("/**")
//                .excludePathPatterns("/static/**")
//                .order(Ordered.HIGHEST_PRECEDENCE + 100);

        if (AppConstants.getIsSystemRestEnable() && AppConstants.isRestDefaultInterceptorEnable()) {
            registry.addInterceptor(new RestDefaultAuthorityInterceptor())
                    .addPathPatterns("/rest/**")
                    .order(Ordered.HIGHEST_PRECEDENCE + 145);
        }

        List<String> dList = Lists.newArrayList("/jump.jsp", "/index.html", "/web/**", "/mweb/**", "/assets/**", "/icons/**", "/static/**", "/**/*.css", "/**/*.js", "/**/*.png", "/**/*.ico", "/**/*.json", "favicon**", "/userfiles/**", "/servlet/**", "/error/**", "/api/**", "/rest/**");

        if (Boolean.TRUE.equals(AppConstants.isOauth2Enable())) {
            List<String> cList = AppConstants.getOauth2ExcludePathList();
            registry.addInterceptor(new AuthorityOauth2Interceptor()).addPathPatterns("/**")
                    .excludePathPatterns(Collections3.aggregate(dList, cList))
                    .order(Ordered.HIGHEST_PRECEDENCE + 195);
        }

        List<String> authExcludePathList = AppConstants.getAuthExcludePathList();
        AuthorityInterceptor authorityInterceptor = new AuthorityInterceptor();
        authorityInterceptor.setRedirectURL("/jump.jsp");
        registry.addInterceptor(authorityInterceptor).addPathPatterns("/**")
                .excludePathPatterns(Collections3.aggregate(dList, authExcludePathList))
                .order(Ordered.HIGHEST_PRECEDENCE + 200);


        registry.addInterceptor(new MobileInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(Lists.newArrayList("/static/**","/api/**","/rest/**"))
                .order(Ordered.HIGHEST_PRECEDENCE + 300);
    }

    /**
     * 跨域配置
     * @param registry
     */
//   @Override
//   public void addCorsMappings(CorsRegistry registry) {
//      registry.addMapping("/**")
//              .allowedOriginPatterns(CorsConfiguration.ALL)
//              .allowCredentials(true)
//              .allowedHeaders(CorsConfiguration.ALL)
//              .allowedMethods(CorsConfiguration.ALL)
//              .maxAge(3600);
//   }


    /**
     * Json解析
     *
     * @return
     */
    @Bean
    public MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
        final MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        //设置日期格式
        JsonMapper objectMapper = new JsonMapper();
        objectMapper.enable(INCLUDE_SOURCE_IN_LOCATION);

        SimpleModule module = new SimpleModule();
        // XSS反序列化
        module.addDeserializer(String.class, new XssDefaultJsonDeserializer());
        // XSS序列化
        module.addSerializer(String.class, new XssDefaultJsonSerializer());

        //序列换成json时,将所有的long变成string 因为js中得数字类型不能包含所有的java long值
//      module.addSerializer(Long.class, ToStringSerializer.instance);
//      module.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 注册自定义的序列化和反序列化器
        objectMapper.registerModule(module);

        mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);

        //设置中文编码格式
        List<MediaType> mediaTypes = Arrays.asList(
                MediaType.APPLICATION_JSON,
                MediaType.TEXT_PLAIN,
                MediaType.TEXT_HTML,
                MediaType.TEXT_XML,
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.valueOf("application/vnd.spring-boot.actuator.v3+json"));

        mappingJackson2HttpMessageConverter.setSupportedMediaTypes(mediaTypes);
        return mappingJackson2HttpMessageConverter;
    }


    @Bean("fileManager")
    @ConditionalOnProperty(name = "system.disk.type", havingValue = "disk", matchIfMissing = true)
    public IFileManager fileManager() {
        return new DISKManager();
    }

//
//   @Bean
//   public LayoutDialect layoutDialect() {
//      return new LayoutDialect();
//   }

    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }


    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // 创建一个 ObjectMapper 实例
        JsonMapper objectMapper = new JsonMapper();
        // 设置 INCLUDE_SOURCE_IN_LOCATION 特性
        objectMapper.enable(INCLUDE_SOURCE_IN_LOCATION);

        // 创建一个 MappingJackson2HttpMessageConverter 实例，并使用自定义的 ObjectMapper
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        RestTemplate restTemplate = builder.requestFactory(this::getRequestFactory).setConnectTimeout(Duration.ofSeconds(10)).build();
        restTemplate.getMessageConverters().add(converter);
        return restTemplate;
    }

    //配置SSL, 使用RestTemplate访问https
    public HttpComponentsClientHttpRequestFactory getRequestFactory(){
        try {
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

            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null,
                    (chain, authType) -> true).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            // 创建SSLSocketFactory
            // 定义socket工厂类 指定协议（Http、Https）
            Registry registry = RegistryBuilder.create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslsf)//SSLConnectionSocketFactory.getSocketFactory()
                    .build();

            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
            connectionManager.setMaxTotal(1024);// 连接池最大并发连接数
            connectionManager.setDefaultMaxPerRoute(256);// 单路由最大并发数

            // 重试机制
            HttpRequestRetryHandler retryHandler = (exception, executionCount, context) -> {
                if (executionCount >= 3) {// 如果已经重试了3次，就放弃
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
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                // 如果请求是幂等的，就再次尝试
                return idempotent;
            };

            CookieSpecProvider easySpecProvider = context -> new BrowserCompatSpec() {
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

            RequestConfig requestConfig = RequestConfig.custom()
                    .setCookieSpec("easy")
                    .setSocketTimeout(10*60*1000)
                    .setConnectTimeout(10000)
                    .setConnectionRequestTimeout(10000)
                    .setRedirectsEnabled(true).build();

            HttpClient httpClient = HttpClients.custom()
                    .setRetryHandler(retryHandler)
                    .setDefaultCookieSpecRegistry(r)
                    .setDefaultRequestConfig(requestConfig)
                    .setDefaultCookieStore(new BasicCookieStore())
                    .setConnectionManager(connectionManager)
                    .setKeepAliveStrategy(keepAliveStrat).build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            requestFactory.setConnectionRequestTimeout(5000);
            requestFactory.setConnectTimeout(10000);
            return requestFactory;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}