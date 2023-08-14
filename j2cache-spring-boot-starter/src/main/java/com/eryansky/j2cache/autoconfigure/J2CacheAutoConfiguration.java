package com.eryansky.j2cache.autoconfigure;

import com.eryansky.j2cache.properties.J2CacheSessionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.env.StandardEnvironment;

import com.eryansky.j2cache.CacheChannel;
import com.eryansky.j2cache.J2Cache;
import com.eryansky.j2cache.J2CacheBuilder;
import com.eryansky.j2cache.cache.support.util.SpringJ2CacheConfigUtil;
import com.eryansky.j2cache.cache.support.util.SpringUtil;
/**
 * 启动入口
 * @author zhangsaizz
 *
 */
@ConditionalOnClass(J2Cache.class)
@EnableConfigurationProperties({J2CacheConfig.class, J2CacheSessionProperties.class})
@Import(J2CacheSessionFilterAutoConfiguration.class)
@Configuration
@PropertySource(value = "${j2cache.config-location}", encoding = "UTF-8", ignoreResourceNotFound = true)
public class J2CacheAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(J2CacheAutoConfiguration.class);

    @Autowired
    private StandardEnvironment standardEnvironment;

    @Bean
    public com.eryansky.j2cache.J2CacheConfig j2CacheConfig(){
        com.eryansky.j2cache.J2CacheConfig j2CacheConfig = J2Cache.getConfig();
        if(j2CacheConfig != null){
    	    return j2CacheConfig;
        }
        return SpringJ2CacheConfigUtil.initFromConfig(standardEnvironment);
    }

    @Bean
    @DependsOn({"springUtil","j2CacheConfig"})
    public CacheChannel cacheChannel(com.eryansky.j2cache.J2CacheConfig j2CacheConfig) {
        CacheChannel cacheChannel = J2Cache.getChannel();
        if(cacheChannel != null){
            return cacheChannel;
        }
    	J2CacheBuilder builder = J2CacheBuilder.init(j2CacheConfig);
        J2Cache.init(j2CacheConfig,builder);
        return builder.getChannel();
    }

    @Bean
    public SpringUtil springUtil() {
    	return new SpringUtil();
    }



}
