/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.orm.mybatis.interceptor;

import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.mybatis.dialect.Dialect;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.reflection.ReflectionUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Map;
import java.util.Properties;

/**
 * 数据库分页插件，只拦截查询语句.
 * @author Eryan
 * @version 2014-7-16
 */
@Intercepts({@Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class PaginationInterceptor extends BaseInterceptor {

    private static final long serialVersionUID = 1L;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        final MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        
//        //拦截需要分页的SQL
////        if (mappedStatement.getId().matches(_SQL_PATTERN)) {
//        if (StringUtils.indexOfIgnoreCase(mappedStatement.getId(), _SQL_PATTERN) != -1) {
            Object parameter = invocation.getArgs()[1];
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            Object parameterObject = boundSql.getParameterObject();

            //获取分页参数对象
            Page<Object> page = null;
            if (parameterObject != null) {
                page = convertParameter(parameterObject, page);
            }

            //如果设置了分页对象，则进行分页
            if (page != null && page.getPageSize() != -1) {

            	if (StringUtils.isBlank(boundSql.getSql())){
                    return null;
                }
                String originalSql = boundSql.getSql().trim();
                if (log.isDebugEnabled()) {
                    log.debug("SQL:"+originalSql);
                }

                //得到总记录数
                page.autoTotalCount(SQLHelper.getCount(originalSql, null, mappedStatement, parameterObject, boundSql, log));

                Dialect dialect = DIALECT;
                //数据库类型
                String dbName = convertDbNameParameter(parameterObject);
                if(StringUtils.isNotBlank(dbName)){
                    dialect = getDialect(dbName);
                }

                //分页查询 本地化对象 修改数据库注意修改实现
                String pageSql = SQLHelper.generatePageSql(originalSql, page, dialect);
                if (log.isDebugEnabled()) {
                    log.debug("PAGE SQL:" + StringUtils.replace(pageSql, "\n", ""));
                }
                invocation.getArgs()[2] = new RowBounds(RowBounds.NO_ROW_OFFSET, RowBounds.NO_ROW_LIMIT);
                BoundSql newBoundSql = new BoundSql(mappedStatement.getConfiguration(), pageSql, boundSql.getParameterMappings(), boundSql.getParameterObject());

                //解决MyBatis 分页foreach list item问题！ 升级3.4.0后的问题 此部分为新增内容
                if (ReflectionUtils.getFieldValue(boundSql, "additionalParameters") != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mo = ReflectionUtils.getFieldValue(boundSql, "additionalParameters");
                    ReflectionUtils.setFieldValue(newBoundSql, "additionalParameters", mo);
                }
                //解决MyBatis 分页foreach 参数失效
                if (ReflectionUtils.getFieldValue(boundSql, "metaParameters") != null) {
                    MetaObject mo = ReflectionUtils.getFieldValue(boundSql, "metaParameters");
                    ReflectionUtils.setFieldValue(newBoundSql, "metaParameters", mo);
                }

                MappedStatement newMs = copyFromMappedStatement(mappedStatement, new BoundSqlSqlSource(newBoundSql));

                invocation.getArgs()[0] = newMs;
            }
//        }
        return invocation.proceed();
    }


    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        super.initProperties(properties);
    }

    private MappedStatement copyFromMappedStatement(MappedStatement ms,
                                                    SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(),
                ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null) {
            for (String keyProperty : ms.getKeyProperties()) {
                builder.keyProperty(keyProperty);
            }
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.cache(ms.getCache());
        return builder.build();
    }

    public static class BoundSqlSqlSource implements SqlSource {
        BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}
