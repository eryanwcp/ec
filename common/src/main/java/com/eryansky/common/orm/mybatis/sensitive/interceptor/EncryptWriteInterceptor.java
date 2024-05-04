package com.eryansky.common.orm.mybatis.sensitive.interceptor;

import com.eryansky.common.orm.mybatis.sensitive.IEncrypt;
import com.eryansky.common.orm.mybatis.sensitive.annotation.*;
import com.eryansky.common.orm.mybatis.sensitive.encrypt.AesSupport;
import com.eryansky.common.orm.mybatis.sensitive.utils.JsonUtils;
import com.eryansky.common.orm.mybatis.sensitive.utils.SensitiveUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.*;

/**
 * 拦截写请求的插件，需要加密。插件生效仅支持预编译的sql
 *
 * @author Eryan
 * @version 2019-12-13
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
})
public class EncryptWriteInterceptor implements Interceptor {

    protected Log log = LogFactory.getLog(this.getClass());

    private static final String MAPPEDSTATEMENT = "delegate.mappedStatement";
    private static final String BOUND_SQL = "delegate.boundSql";

    private Properties properties = new Properties();
    private IEncrypt encrypt;

    public EncryptWriteInterceptor() throws NoSuchAlgorithmException {
        this.encrypt = new AesSupport();
    }

    public EncryptWriteInterceptor(IEncrypt encrypt) {
        Objects.requireNonNull(encrypt, "encrypt should not be null!");
        this.encrypt = encrypt;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        StatementHandler statementHandler = SensitiveUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue(MAPPEDSTATEMENT);
        SqlCommandType commandType = mappedStatement.getSqlCommandType();

        BoundSql boundSql = (BoundSql) metaObject.getValue(BOUND_SQL);
        Object params = boundSql.getParameterObject();
        if (params instanceof Map) {
            return invocation.proceed();
        }
        SensitiveEncryptEnabled sensitiveEncryptEnabled = params != null ? params.getClass().getAnnotation(SensitiveEncryptEnabled.class) : null;
        if (sensitiveEncryptEnabled != null && sensitiveEncryptEnabled.value()) {
            handleParameters(mappedStatement.getConfiguration(), boundSql, params, commandType);
        }
        return invocation.proceed();
    }

    private void handleParameters(Configuration configuration, BoundSql boundSql, Object param, SqlCommandType commandType) throws Exception {

        Map<String, Object> newValues = new HashMap<>(16);
        MetaObject metaObject = configuration.newMetaObject(param);

        Map<String,List<String>> mapList = Maps.newHashMap();
        for (Field field : param.getClass().getDeclaredFields()) {
            Object value = metaObject.getValue(field.getName());
            if (value instanceof CharSequence) {
                EncryptField encryptField = field.getAnnotation(EncryptField.class);
                if (encryptField != null && value != null && !"".equals(value.toString())) {
                    List<String> mapData = mapList.get(!"".equals(encryptField.type()) ? encryptField.type():encrypt.defaultType());
                    if(null == mapData){
                        mapData = Lists.newArrayList();
                    }
                    mapData.add(value.toString());
                    mapList.put(!"".equals(encryptField.type()) ? encryptField.type():encrypt.defaultType(),mapData);
                }
                EncryptJSONField encryptJSONField = field.getAnnotation(EncryptJSONField.class);
                if (encryptJSONField != null && value != null) {
                    value =  JsonUtils.parseToObjectMap(value.toString());
                    for(EncryptJSONFieldKey encryptJSONFieldKey: encryptJSONField.encryptList()){
                        String keyValue = (String) ((Map<String, Object>) value).get(encryptJSONFieldKey.key());
                        if(keyValue != null && !"".equals(keyValue)){
                            List<String> mapData = mapList.get(!"".equals(encryptJSONFieldKey.type()) ? encryptJSONFieldKey.type():encrypt.defaultType());
                            if(null == mapData){
                                mapData = Lists.newArrayList();
                            }
                            mapData.add(keyValue.toString());
                            mapList.put(!"".equals(encryptJSONFieldKey.type()) ? encryptJSONFieldKey.type():encrypt.defaultType(),mapData);
                        }

                    }

                }
            }else if (value instanceof Map) {
                EncryptJSONField encryptField = field.getAnnotation(EncryptJSONField.class);
                if (encryptField != null && value != null) {
                    for(EncryptJSONFieldKey encryptJSONFieldKey: encryptField.encryptList()){
                        String keyValue = (String) ((Map<?, ?>) value).get(encryptJSONFieldKey.key());
                        if(keyValue != null && !"".equals(keyValue)){
                            List<String> mapData = mapList.get(!"".equals(encryptJSONFieldKey.type()) ? encryptJSONFieldKey.type():encrypt.defaultType());
                            if(null == mapData){
                                mapData = Lists.newArrayList();
                            }
                            mapData.add(keyValue.toString());
                            mapList.put(!"".equals(encryptJSONFieldKey.type()) ? encryptJSONFieldKey.type():encrypt.defaultType(),mapData);
                        }

                    }

                }
            }
        }

        Map<String,List<String>> rDatas = encrypt.batchEncrypt(mapList);
        for (Field field : param.getClass().getDeclaredFields()) {

            Object value = metaObject.getValue(field.getName());
            Object newValue = value;
            if (value instanceof CharSequence) {
                EncryptField encryptField = field.getAnnotation(EncryptField.class);
                if (encryptField != null && value != null && !"".equals(value.toString())) {
                    newValue = rDatas.get(!"".equals(encryptField.type()) ? encryptField.type():encrypt.defaultType()).get(0);
                    rDatas.get(!"".equals(encryptField.type()) ? encryptField.type():encrypt.defaultType()).remove(0);
                }

                EncryptJSONField encryptJSONField = field.getAnnotation(EncryptJSONField.class);
                if (encryptJSONField != null && value != null) {
                    newValue = JsonUtils.parseToObjectMap(value.toString());
                    for(EncryptJSONFieldKey encryptJSONFieldKey: encryptJSONField.encryptList()){
                        String keyValue = (String) ((Map<String, Object>) newValue).get(encryptJSONFieldKey.key());
                        if(keyValue != null && !"".equals(keyValue)){
                            ((Map<String, Object>) newValue).put(encryptJSONFieldKey.key(),rDatas.get(!"".equals(encryptJSONFieldKey.type()) ? encryptJSONFieldKey.type():encrypt.defaultType()).get(0));
                            rDatas.get(!"".equals(encryptJSONFieldKey.type()) ? encryptJSONFieldKey.type():encrypt.defaultType()).remove(0);
                        }

                    }
                    newValue = JsonUtils.parseMaptoJSONString(((Map<String, Object>) newValue));
                }
            }else if (value instanceof Map) {
                EncryptJSONField encryptField = field.getAnnotation(EncryptJSONField.class);
                if (encryptField != null && value != null) {
                    for(EncryptJSONFieldKey encryptJSONFieldKey: encryptField.encryptList()){
                        String keyValue = (String) ((Map<String, Object>) newValue).get(encryptJSONFieldKey.key());
                        if(keyValue != null && !"".equals(keyValue)){
                            ((Map<String, Object>) newValue).put(encryptJSONFieldKey.key(),rDatas.get(!"".equals(encryptJSONFieldKey.type()) ? encryptJSONFieldKey.type():encrypt.defaultType()).get(0));
                            rDatas.get(!"".equals(encryptJSONFieldKey.type()) ? encryptJSONFieldKey.type():encrypt.defaultType()).remove(0);
                        }

                    }
                }
            }
            if (value != null && newValue != null && !value.equals(newValue)) {
                newValues.put(field.getName(), newValue);
            }
        }
        for (Map.Entry<String, Object> entry : newValues.entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }

    }

    private boolean isWriteCommand(SqlCommandType commandType) {
        return SqlCommandType.UPDATE.equals(commandType) || SqlCommandType.INSERT.equals(commandType);
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
        String encryptValue = (String) properties.get("encrypt");
        if (null != encryptValue) {
            log.debug("properties-encrypt:" + encryptValue);
            try {
                Class clazz = Class.forName(encryptValue);
                encrypt = (IEncrypt) clazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
