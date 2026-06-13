/**
 * Copyright (c) 2012-2019 http://www.eryansky.com
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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
import java.util.Properties;

/**
 * @author Eryan
 * @date 2019-01-23
 */
@Configuration
public class DBConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(DBConfigurer.class);

    public static final String TX_MANAGER_NAME = "transactionManager";

    // 默认包配置
    private static final String DEFAULT_TYPE_ALIASES = "com.eryansky.modules.sys.mapper,com.eryansky.modules.disk.mapper,com.eryansky.modules.notice.mapper";
    private static final String DEFAULT_BASE_PACKAGE = "com.eryansky.modules.sys.dao,com.eryansky.modules.disk.dao,com.eryansky.modules.notice.dao";

    // 数据源
    @Bean(name = "dataSource")
    @ConfigurationProperties("spring.datasource.druid")
    @Primary
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
    public SqlSessionFactory sqlSessionFactoryBean(@Qualifier("dataSource") DataSource dataSource,
                                                   Environment environment) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
        sqlSessionFactoryBean.setVfs(SpringBootVFS.class);

        String typeAliasesPackage = environment.getProperty("spring.dataSource.mybatis.typeAliasesPackage");
        StringBuilder typeAliases = new StringBuilder(DEFAULT_TYPE_ALIASES);
        if (StringUtils.isNotBlank(typeAliasesPackage)) {
            typeAliases.append(typeAliasesPackage.startsWith(",") ? typeAliasesPackage : "," + typeAliasesPackage);
        }
        sqlSessionFactoryBean.setTypeAliasesPackage(typeAliases.toString());

        Resource[] defaultResource = resolver.getResources("classpath*:mappings/modules/**/*Dao.xml");
        String mapperLocations = environment.getProperty("spring.dataSource.mybatis.mapperLocations");
        if (StringUtils.isBlank(mapperLocations)) {
            sqlSessionFactoryBean.setMapperLocations(defaultResource);
        } else {
            // collect resources from configured locations and merge with defaults
            Resource[] merged = defaultResource;
            String[] paths = StringUtils.split(mapperLocations, ",");
            if (paths != null) {
                for (String path : paths) {
                    Resource[] resources = resolver.getResources(path);
                    if (resources.length > 0) {
                        merged = ArrayUtils.concatAll(merged, resources);
                    }
                }
            }
            sqlSessionFactoryBean.setMapperLocations(merged);
        }

        String mybatisProperties = environment.getProperty("spring.dataSource.mybatis.properties");
        Map<String, Object> map = JsonMapper.getInstance().toMap(mybatisProperties);
        sqlSessionFactoryBean.setConfigurationProperties(AppUtils.mapToProperties(map));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer(Environment environment) {
        MapperScannerConfigurer cfg = new MapperScannerConfigurer();
        String basePackage = environment.getProperty("spring.dataSource.mybatis.basePackage");
        StringBuilder base = new StringBuilder(DEFAULT_BASE_PACKAGE);
        if (StringUtils.isNotBlank(basePackage)) {
            base.append(basePackage.startsWith(",") ? basePackage : "," + basePackage);
        }
        cfg.setBasePackage(base.toString());
        cfg.setSqlSessionFactoryBeanName("sqlSessionFactory");
        cfg.setAnnotationClass(MyBatisDao.class);
        return cfg;
    }

    @Order(2)
    @Bean(TX_MANAGER_NAME)
    public TransactionManager annotationDrivenTransactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    private static final int TX_METHOD_TIMEOUT = 60000;
    private static final String AOP_POINTCUT_EXPRESSION = "execution(* com.eryansky.modules..*.service..*Service.*(..))";
//    private static final String AOP_POINTCUT_EXPRESSION = "execution(* com.eryansky.modules..*.service..*Service.*(..))";

    // 事务的实现Advice
    @Bean
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
    public Advisor txAdviceAdvisor(@Qualifier("txAdvice") TransactionInterceptor txAdvice,
                                   @Value("${spring.dataSource.aopPointcutExpression}")String aopPointcutExpression) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(AOP_POINTCUT_EXPRESSION);
        if(StringUtils.isNotBlank(aopPointcutExpression)){
            sb.append(aopPointcutExpression.startsWith("||") ? aopPointcutExpression : " || " + aopPointcutExpression);
        }
        sb.append(" && !@annotation(org.springframework.transaction.annotation.Transactional)");
        sb.append(")");
        pointcut.setExpression(sb.toString());
        logger.debug("aop expression:{}", sb);
        return new DefaultPointcutAdvisor(pointcut, txAdvice);
    }
}
