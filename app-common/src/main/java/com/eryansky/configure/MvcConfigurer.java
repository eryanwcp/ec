package com.eryansky.configure;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.dialect.dialect.ShiroDialect;
import com.eryansky.core.security.interceptor.*;
import com.eryansky.core.security.xss.XssJsonDeserializer;
import com.eryansky.core.security.xss.XssJsonSerializer;
import com.eryansky.core.web.interceptor.MobileInterceptor;
import com.eryansky.modules.disk.extend.DISKManager;
import com.eryansky.modules.disk.extend.IFileManager;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
//import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.ssl.*;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
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
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.net.ssl.SSLContext;
import java.net.ProxySelector;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;

import static com.fasterxml.jackson.core.JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION;

@Configuration
public class MvcConfigurer implements WebMvcConfigurer {

    public static final String MEDIA_TYPE_SECURE_MSGPACK = "application/x-secure-msgpack";
    public static final String MEDIA_TYPE_MSGPACK = "application/x-msgpack";

    @Lazy
    @Resource
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

        if (AppConstants.isLimitUrlEnable()) {
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


        if (AppConstants.isOauth2Enable()) {
            List<String> cList = AppConstants.getOauth2ExcludePathList();
            registry.addInterceptor(new SSOAuthorityOauth2Interceptor()).addPathPatterns("/**")
                    .excludePathPatterns(Collections3.aggregate(dList, cList))
                    .order(Ordered.HIGHEST_PRECEDENCE + 190);

            registry.addInterceptor(new AuthorityOauth2Interceptor()).addPathPatterns("/**")
                    .excludePathPatterns(Collections3.aggregate(dList, cList))
                    .order(Ordered.HIGHEST_PRECEDENCE + 195);


        }

        List<String> authExcludePathList = AppConstants.getAuthExcludePathList();
        AuthorityInterceptor authorityInterceptor = new AuthorityInterceptor();
        String redirectURL = "/jump.jsp";
        //开启SSO单点登录
        if (AppConstants.getIsSSOEnable()) {
            redirectURL = AppUtils.appendParaToUrlBuilder(AppConstants.getSSOIssuerUri(), "client_id", AppConstants.getSSOClientId())
                    .append("redirect_uri", AppConstants.getSSOCallbackUrl()).toString();
        }

        authorityInterceptor.setRedirectURL(redirectURL);
        registry.addInterceptor(authorityInterceptor).addPathPatterns("/**")
                .excludePathPatterns(Collections3.aggregate(dList, authExcludePathList))
                .order(Ordered.HIGHEST_PRECEDENCE + 200);


        registry.addInterceptor(new MobileInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(Lists.newArrayList("/static/**","/api/**","/rest/**"))
                .order(Ordered.HIGHEST_PRECEDENCE + 300);
    }

   @Override
   public void configurePathMatch(PathMatchConfigurer configurer) {
      configurer.setUseTrailingSlashMatch(true);
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
        module.addDeserializer(String.class, new XssJsonDeserializer());
        // XSS序列化
        module.addSerializer(String.class, new XssJsonSerializer());

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
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);


        RestTemplate restTemplate = builder.requestFactory(this::getRequestFactory).connectTimeout(Duration.ofSeconds(20)).build();
        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter);
        return restTemplate;
    }

    /**
     * 设置整个连接池最大连接数
     */
    private static final int POOL_MAX_CONN = 1024;
    /**
     * 设置单个路由默认连接数
     */
    private static final int POOL_MAX_PER_CONN = 256;

    //配置SSL, 使用RestTemplate访问https
    // 配置SSL, 使用RestTemplate访问https
    public HttpComponentsClientHttpRequestFactory getRequestFactory() {
        try {
            // 1. SSL 配置 (当前策略为信任所有证书)
            TrustStrategy trustStrategy = (x509Certificates, s) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, trustStrategy).build();

            TlsSocketStrategy tlsSocketStrategy = new DefaultClientTlsStrategy(
                    sslContext,
                    HttpsSupport.getSystemProtocols(),
                    HttpsSupport.getSystemCipherSuits(),
                    SSLBufferMode.STATIC,
                    HostnameVerificationPolicy.BOTH,
                    HttpsSupport.getDefaultHostnameVerifier());
//            PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
//            final RFC6265CookieSpecFactory cookieSpecFactory = new RFC6265CookieSpecFactory(publicSuffixMatcher);
//            final Lookup<CookieSpecFactory> cookieSpecRegistry = RegistryBuilder.<CookieSpecFactory>create()
//                    .register(StandardCookieSpec.RELAXED, cookieSpecFactory)
//                    .register(StandardCookieSpec.STRICT, cookieSpecFactory)
//                    .build();

            // 2. 配置连接池
            PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setTlsSocketStrategy(tlsSocketStrategy)
                    .setMaxConnTotal(POOL_MAX_CONN)
                    .setMaxConnPerRoute(POOL_MAX_PER_CONN)
                    .build();

            HttpClientBuilder clientBuilder = HttpClients.custom()
//                    .disableCookieManagement()
//                    .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                    .setConnectionManager(connectionManager);

            // 3. 处理代理与路由配置 (修复 setProxy 与 RoutePlanner 冲突的 Bug)
            String proxyHost = System.getProperty("http.proxyHost");
            String proxyPort = System.getProperty("http.proxyPort");

            if (StringUtils.isNotBlank(proxyHost) && StringUtils.isNotBlank(proxyPort)) {

                // 统一使用系统默认的路由规划器，它会自动读取系统属性中的 http(s).proxyHost 和 http.nonProxyHosts
                clientBuilder.setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault()));

                // 处理自定义的代理身份认证
                String proxyUser = System.getProperty("http.proxyUser");
                String proxyPassword = System.getProperty("http.proxyPassword");

                if (StringUtils.isNotBlank(proxyUser) && StringUtils.isNotBlank(proxyPassword)) {
                    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    org.apache.hc.core5.http.HttpHost proxy = new org.apache.hc.core5.http.HttpHost(proxyHost, Integer.parseInt(proxyPort));
                    credentialsProvider.setCredentials(
                            new AuthScope(proxy),
                            new UsernamePasswordCredentials(proxyUser, proxyPassword.toCharArray())
                    );
                    clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            }

            // 4. 构建 HttpClient 及 Factory
            CloseableHttpClient httpClient = clientBuilder.build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);

            // 设置从连接池获取连接的超时时间 (注: Spring 6/Boot 3 中推荐使用 Duration，这里保留原有的毫秒单位)
            requestFactory.setConnectionRequestTimeout(10 * 1000);

            return requestFactory;

        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            // 优化：抛出带上下文信息的异常
            throw new RuntimeException("初始化 HttpComponentsClientHttpRequestFactory 失败, SSL配置错误", e);
        }
    }



}