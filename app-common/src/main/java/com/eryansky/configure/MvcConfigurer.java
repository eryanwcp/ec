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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.*;

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
    * @param registry
    */
   @Override
   public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(new IpLimitInterceptor())
              .addPathPatterns("/**")
              .order(Ordered.HIGHEST_PRECEDENCE + 90);

      if(Boolean.TRUE.equals(AppConstants.isLimitUrlEnable())){
         registry.addInterceptor(new UrlLimitInterceptor())
                 .addPathPatterns("/**")
                 .order(Ordered.HIGHEST_PRECEDENCE + 90);
      }

      registry.addInterceptor(new LogInterceptor(requestMappingHandlerAdapter))
              .addPathPatterns("/**")
              .excludePathPatterns("/static/**")
              .order(Ordered.HIGHEST_PRECEDENCE + 100);

      if(Boolean.TRUE.equals(AppConstants.getIsSystemRestEnable())){
         registry.addInterceptor(new RestDefaultAuthorityInterceptor())
                 .addPathPatterns("/rest/**")
                 .order(Ordered.HIGHEST_PRECEDENCE + 145);
      }

      if(Boolean.TRUE.equals(AppConstants.isOauth2Enable())){
         List<String> dList = Lists.newArrayList("/jump.jsp","/index.html","/web/**","/mweb/**","/assets/**","/icons/**","/static/**","/**/*.css","/**/*.js","/**/*.png","/**/*.ico","/**/*.json","favicon**","/userfiles/**","/servlet/**","/error/**","/api/**","/rest/**");
         List<String> cList = AppConstants.getOauth2ExcludePathList();
         registry.addInterceptor(new AuthorityOauth2Interceptor()).addPathPatterns("/**")
                 .excludePathPatterns(Collections3.aggregate(dList,cList))
                 .order(Ordered.HIGHEST_PRECEDENCE + 195);
      }


      AuthorityInterceptor authorityInterceptor = new AuthorityInterceptor();
      authorityInterceptor.setRedirectURL("/jump.jsp");
      registry.addInterceptor(authorityInterceptor).addPathPatterns("/**")
              .excludePathPatterns(Lists.newArrayList("/jump.jsp","/index.html","/web/**","/mweb/**","/assets/**","/icons/**","/static/**","/**/*.css","/**/*.js","/**/*.png","/**/*.ico","/**/*.json","favicon**","/userfiles/**","/servlet/**","/error/**","/api/**","/rest/**"))
              .order(Ordered.HIGHEST_PRECEDENCE + 200);



      registry.addInterceptor(new MobileInterceptor())
              .addPathPatterns("/**")
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
    * @return
    */
   @Bean
   public MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
      final MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
      //设置日期格式
      JsonMapper objectMapper = new JsonMapper();

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
   @ConditionalOnMissingBean(name = "fileManager")
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

}