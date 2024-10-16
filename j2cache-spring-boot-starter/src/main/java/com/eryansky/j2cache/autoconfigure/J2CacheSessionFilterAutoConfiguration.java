package com.eryansky.j2cache.autoconfigure;

import com.eryansky.j2cache.properties.J2CacheSessionProperties;
import com.eryansky.j2cache.session.J2CacheSessionFilter;
import com.eryansky.j2cache.util.AesSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * J2Cache Session 自动注入配置
 * @author Eryan
 * @date 2019-02-11
 */
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "j2cache.session.filter.enabled", havingValue = "true")
public class J2CacheSessionFilterAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(J2CacheSessionFilterAutoConfiguration.class);

    @Bean
    public FilterRegistrationBean<J2CacheSessionFilter> j2CacheSessionFilter(J2CacheSessionProperties sessionProperties) {
        J2CacheSessionFilter filter = new J2CacheSessionFilter();
        FilterRegistrationBean<J2CacheSessionFilter> bean = new FilterRegistrationBean<>(filter);
        J2CacheSessionProperties.Filter filterConfig = sessionProperties.getFilter();
        J2CacheSessionProperties.Redis redisConfig = sessionProperties.getRedis();
        Map<String,String> map  = new HashMap<>();
        map.put("whiteListURL",filterConfig.getWhiteListURL());
        map.put("blackListURL",filterConfig.getBlackListURL());

        map.put("cookie.name",filterConfig.getCookieName());
        map.put("cookie.domain",filterConfig.getCookieDomain());
        map.put("cookie.path",filterConfig.getCookiePath());

        map.put("session.maxAge",sessionProperties.getMaxAge());
        map.put("session.maxSizeInMemory",sessionProperties.getMaxSizeInMemory());

        map.put("redis.enabled",redisConfig.isEnabled());
        map.put("redis.scheme",redisConfig.getScheme());
        map.put("redis.hosts",redisConfig.getHosts());
        map.put("redis.channel",redisConfig.getChannel());
        map.put("redis.cluster_name",redisConfig.getClusterName());
        map.put("redis.database",redisConfig.getDatabase());
        map.put("redis.timeout",redisConfig.getTimeout());
        map.put("redis.maxTotal",redisConfig.getMaxTotal());
        map.put("redis.maxIdle",redisConfig.getMaxIdle());
        map.put("redis.minIdle",redisConfig.getMinIdle());
        map.put("redis.password",redisConfig.getPassword());
        map.put("redis.passwordEncrypt",redisConfig.getPasswordEncrypt());
        map.put("redis.passwordEncryptKey",redisConfig.getPasswordEncryptKey());
        map.put("redis.sentinelMasterId",redisConfig.getSentinelMasterId());
        map.put("redis.sentinelPassword",redisConfig.getSentinelPassword());
        map.put("redis.clusterTopologyRefresh",redisConfig.getClusterTopologyRefresh());

        String password_encrypt = redisConfig.getPasswordEncrypt();
        String passwordEncryptKey = redisConfig.getPasswordEncryptKey();//长度16位
        boolean passwordEncrypt = Boolean.parseBoolean(password_encrypt);
        if(passwordEncrypt && !ObjectUtils.isEmpty(redisConfig.getPassword())){
            try {
                AesSupport aesSupport = null;
                if(StringUtils.hasText(passwordEncryptKey)){
                    aesSupport = new AesSupport(StringUtils.trimAllWhitespace(passwordEncryptKey));
                }else{
                    aesSupport = new AesSupport();
                }
                map.put("redis.password",aesSupport.decrypt(redisConfig.getPassword()));
            } catch (NoSuchAlgorithmException e) {
                logger.error(e.getMessage(),e);
            }
        }

        map.put("redis.maxTotal",redisConfig.getMaxTotal());
        map.put("redis.maxIdle",redisConfig.getMaxIdle());
        map.put("redis.minIdle",redisConfig.getMinIdle());
        map.put("redis.clusterTopologyRefresh",redisConfig.getClusterTopologyRefresh());
        Map<String,String> param = map.entrySet().stream().filter(m->m.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        bean.setInitParameters(param);
        Integer order = filterConfig.getOrder();
        bean.setOrder(order != null ? order:Ordered.HIGHEST_PRECEDENCE+30);
        return bean;
    }
}
