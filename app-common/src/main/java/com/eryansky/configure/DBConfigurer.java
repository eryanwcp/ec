/**
 * Copyright (c) 2012-2019 http://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.configure;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.eryansky.common.orm.mybatis.MyBatisDao;
import com.eryansky.common.utils.StringUtils;
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

    private static Logger logger = LoggerFactory.getLogger(DBConfigurer.class);

    public static final String TX_MANAGER_NAME = "transactionManager";

    // 数据源
    @Bean(name = "dataSource")
    @ConfigurationProperties("spring.datasource.druid")
    @Primary
    public DataSource dataSource(){
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * 自定义mybatis参数转换
     * @param mybatisProperties
     * @return
     */
    private Properties mybatisProperties(Map<String, String> mybatisProperties) {
        if(null == mybatisProperties){
            return new Properties();
        }
        Properties properties = new Properties();
        properties.putAll(mybatisProperties);
        return properties;
    }

    /**
     * mybatis sqlSessionFactory
     * @param dataSource 数据源
     * @param environment spring上下文参数
     * @return
     * @throws Exception
     */
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean(@Qualifier("dataSource") DataSource dataSource,
                                                   Environment environment) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("mybatis-config.xml"));
        sqlSessionFactoryBean.setVfs(SpringBootVFS.class);
        StringBuilder sb = new StringBuilder();
        String typeAliasesPackage = environment.getProperty("spring.dataSource.mybatis.typeAliasesPackage");
        sb.append("com.eryansky.modules.sys.mapper,com.eryansky.modules.disk.mapper,com.eryansky.modules.notice.mapper");
        if(StringUtils.isNotBlank(typeAliasesPackage)){
            sb.append(StringUtils.startsWith(typeAliasesPackage,",") ? typeAliasesPackage :","+ typeAliasesPackage);
        }
        sqlSessionFactoryBean.setTypeAliasesPackage(sb.toString());
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath*:mappings/modules/**/*Dao.xml"));

        String mybatisProperties = environment.getProperty("spring.dataSource.mybatis.properties");
        Map<String,Object> map = (Map<String, Object>) JsonMapper.fromJsonString(mybatisProperties,HashMap.class);
        sqlSessionFactoryBean.setConfigurationProperties(AppUtils.mapToProperties(map));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer(Environment environment) {
        MapperScannerConfigurer cfg = new MapperScannerConfigurer();
        String basePackage = environment.getProperty("spring.dataSource.mybatis.basePackage");
        StringBuilder sb = new StringBuilder();
        sb.append("com.eryansky.modules.sys.dao,com.eryansky.modules.disk.dao,com.eryansky.modules.notice.dao");
        if(StringUtils.isNotBlank(basePackage)){
            sb.append(StringUtils.startsWith(basePackage,",") ? basePackage :","+ basePackage);
        }
        cfg.setBasePackage(sb.toString());
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
        Map<String, TransactionAttribute> txMap = new HashMap<>();
        txMap.put("get*", readOnlyTx);
        txMap.put("find*", readOnlyTx);
        txMap.put("query*", readOnlyTx);
        txMap.put("search*", readOnlyTx);
        txMap.put("load*", readOnlyTx);
        txMap.put("is*", readOnlyTx);
        txMap.put("count*", readOnlyTx);
        txMap.put("*", requiredTx);
        source.setNameMap(txMap);
        TransactionInterceptor txAdvice = new TransactionInterceptor(m, source);
        return txAdvice;
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
            sb.append(StringUtils.startsWith(aopPointcutExpression,"||") ? aopPointcutExpression :" || "+ aopPointcutExpression);
        }
        sb.append(" && !@annotation(org.springframework.transaction.annotation.Transactional)");
        sb.append(")");
        pointcut.setExpression(sb.toString());
        logger.debug("aop expression:{}",sb);
        return new DefaultPointcutAdvisor(pointcut, txAdvice);
    }
}
