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
import org.apache.hc.client5.http.cookie.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.cookie.RFC6265CookieSpecFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.psl.PublicSuffixMatcher;
import org.apache.hc.client5.http.psl.PublicSuffixMatcherLoader;
import org.apache.hc.client5.http.ssl.*;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.reactor.ssl.SSLBufferMode;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
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
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

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

        RestTemplate restTemplate = builder.requestFactory(this::getRequestFactory).setConnectTimeout(Duration.ofSeconds(20)).build();
        restTemplate.getMessageConverters().add(converter);
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
    public HttpComponentsClientHttpRequestFactory getRequestFactory(){
        try {
            TrustStrategy trustStrategy = (x509Certificates, s) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null,trustStrategy).build();
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


            CloseableHttpClient httpClient = HttpClients.custom()
//                    .disableCookieManagement()
//                    .setDefaultCookieSpecRegistry(cookieSpecRegistry)
                    .setConnectionManager(PoolingHttpClientConnectionManagerBuilder
                    .create().setTlsSocketStrategy(tlsSocketStrategy).setMaxConnTotal(POOL_MAX_CONN).setMaxConnPerRoute(POOL_MAX_PER_CONN).build()).build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
            requestFactory.setConnectionRequestTimeout(10*1000);
            requestFactory.setConnectTimeout(20*1000);
            return requestFactory;
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }



}