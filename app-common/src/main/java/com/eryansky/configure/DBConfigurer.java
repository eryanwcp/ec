/**
 * Copyright (c) 2012-2026 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.configure;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.eryansky.common.orm.mybatis.MyBatisDao;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.ArrayUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.utils.AppUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.*;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * DB configuration for data source, MyBatis and transaction management.
 *
 * Author: Eryan
 */
@Configuration(proxyBeanMethods = false)
public class DBConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(DBConfigurer.class);

    private static volatile Map<String, Object> mybatisMap;
    public static final String TX_MANAGER_NAME = "transactionManager";

    /**
     * 获取 MyBatis 配置映射表（只读视图）
     */
    public static Map<String, Object> getMybatisMap() {
        return mybatisMap == null ? Collections.emptyMap() : mybatisMap;
    }

    /**
     * 获取 MyBatis 配置项的值
     * @param key 配置项键名
     * @return 配置项的值，不存在则返回空串
     */
    public static String getMybatisProperty(String key) {
        return getProperty(key, StringUtils.EMPTY);
    }

    /**
     * 获取 MyBatis 配置项的值，支持自定义默认值
     * @param key 配置项键名
     * @param defaultValue 默认值
     * @return 配置项的值
     */
    public static String getProperty(String key, String defaultValue) {
        Object value = getMybatisMap().get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    /**
     * 获取指定类型的 MyBatis 配置项值
     * @param key 配置项键名
     * @param type 目标类型
     * @return 转换后的值，不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProperty(String key, Class<T> type) {
        Object value = getMybatisMap().get(key);
        if (value == null) {
            return null;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            logger.warn("MyBatis property {} is not of type {}. Actual type: {}",
                key, type.getName(), value.getClass().getName());
            return null;
        }
    }

    // 默认包配置
    private static final String DEFAULT_TYPE_ALIASES = "com.eryansky.modules.sys.mapper,com.eryansky.modules.disk.mapper,com.eryansky.modules.notice.mapper";
    private static final String DEFAULT_BASE_PACKAGE = "com.eryansky.modules.sys.dao,com.eryansky.modules.disk.dao,com.eryansky.modules.notice.dao";

    // 数据源
    @Bean(name = "dataSource")
    @ConfigurationProperties("spring.datasource.druid")
    @Primary
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE) // 标记为基础设施 Bean
    public DataSource dataSource(){
        return DruidDataSourceBuilder.create().build();
    }

    // mybatis properties helper removed (unused) - using AppUtils.mapToProperties directly

    /**
     * Create and configure MyBatis SqlSessionFactory.
     * @param dataSource the configured DataSource
     * @param environment spring Environment to read properties
     * @return configured SqlSessionFactory
     * @throws Exception if factory creation fails
     */
    @Bean(name = "sqlSessionFactory")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public SqlSessionFactory sqlSessionFactoryBean(@Qualifier("dataSource") DataSource dataSource,
                                                   Environment environment) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
        sqlSessionFactoryBean.setVfs(SpringBootVFS.class);

        String typeAliasesPackage = environment.getProperty("spring.dataSource.mybatis.typeAliasesPackage");
        sqlSessionFactoryBean.setTypeAliasesPackage(mergePackages(DEFAULT_TYPE_ALIASES, typeAliasesPackage));

        Resource[] defaultResource = resolver.getResources("classpath*:mappings/modules/**/*Dao.xml");
        String mapperLocations = environment.getProperty("spring.dataSource.mybatis.mapperLocations");
        if (StringUtils.isBlank(mapperLocations)) {
            sqlSessionFactoryBean.setMapperLocations(defaultResource);
        } else {
            List<Resource> allResources = new ArrayList<>(Arrays.asList(defaultResource));
            for (String path : StringUtils.split(mapperLocations, ",")) {
                allResources.addAll(Arrays.asList(resolver.getResources(path)));
            }
            sqlSessionFactoryBean.setMapperLocations(allResources.toArray(new Resource[0]));
        }

        mybatisMap = mybatisProperties(environment);
        sqlSessionFactoryBean.setConfigurationProperties(AppUtils.mapToProperties(mybatisMap));
        return sqlSessionFactoryBean.getObject();
    }


    private Map<String, Object> mybatisProperties(Environment environment) {
        String mybatisProperties = environment.getProperty("spring.dataSource.mybatis.properties");
        Map<String, Object> map;
        if (StringUtils.isNotBlank(mybatisProperties)) {
            Map<String, Object> parsed = JsonMapper.getInstance().toMap(mybatisProperties);
            map = (parsed != null) ? new HashMap<>(parsed) : new HashMap<>();
        } else {
            map = new HashMap<>();
        }
        // Ensure expected keys exist without overwriting provided values
        map.putIfAbsent("sysPrefix", "");
        map.putIfAbsent("diskPrefix", "");
        map.putIfAbsent("noticePrefix", "");
        return Collections.unmodifiableMap(map);
    }

    private String mergePackages(String defaultPkg, String customPkg) {
        if (StringUtils.isBlank(customPkg)) {
            return defaultPkg;
        }
        return defaultPkg + (customPkg.startsWith(",") ? "" : ",") + customPkg;
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer(Environment environment) {
        MapperScannerConfigurer cfg = new MapperScannerConfigurer();
        String basePackage = environment.getProperty("spring.dataSource.mybatis.basePackage");
        cfg.setBasePackage(mergePackages(DEFAULT_BASE_PACKAGE, basePackage));
        cfg.setSqlSessionFactoryBeanName("sqlSessionFactory");
        cfg.setAnnotationClass(MyBatisDao.class);
        return cfg;
    }

    @Order(2)
    @Bean(TX_MANAGER_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionManager annotationDrivenTransactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    private static final int TX_METHOD_TIMEOUT = 60000;
    private static final String AOP_POINTCUT_EXPRESSION = "execution(* com.eryansky.modules..*.service..*Service.*(..))";
//    private static final String AOP_POINTCUT_EXPRESSION = "execution(* com.eryansky.modules..*.service..*Service.*(..))";

    // 事务的实现Advice
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public TransactionInterceptor txAdvice(@Qualifier(TX_MANAGER_NAME) TransactionManager m) {
        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        RuleBasedTransactionAttribute readOnlyTx = new RuleBasedTransactionAttribute();
        readOnlyTx.setReadOnly(true);
        readOnlyTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);

        RuleBasedTransactionAttribute requiredTx = new RuleBasedTransactionAttribute();
        requiredTx.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
        requiredTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        requiredTx.setTimeout(TX_METHOD_TIMEOUT);
        Map<String, TransactionAttribute> txMap = new HashMap<>(16);
        txMap.put("get*", readOnlyTx);
        txMap.put("find*", readOnlyTx);
        txMap.put("query*", readOnlyTx);
        txMap.put("search*", readOnlyTx);
        txMap.put("load*", readOnlyTx);
        txMap.put("is*", readOnlyTx);
        txMap.put("count*", readOnlyTx);
        txMap.put("*", requiredTx);
        source.setNameMap(txMap);
        return new TransactionInterceptor(m, source);
    }

    // 切面的定义,pointcut及advice
    @Bean
    @Order(1)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor txAdviceAdvisor(@Qualifier("txAdvice") TransactionInterceptor txAdvice,
                                   @Value("${spring.dataSource.aopPointcutExpression}") String aopPointcutExpression) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        String expression = "(" + AOP_POINTCUT_EXPRESSION +
            (StringUtils.isNotBlank(aopPointcutExpression) ?
                (aopPointcutExpression.startsWith("||") ? aopPointcutExpression : " || " + aopPointcutExpression) : "") +
            " && !@annotation(org.springframework.transaction.annotation.Transactional))";
        pointcut.setExpression(expression);
        logger.debug("aop expression:{}", expression);
        return new DefaultPointcutAdvisor(pointcut, txAdvice);
    }
}
