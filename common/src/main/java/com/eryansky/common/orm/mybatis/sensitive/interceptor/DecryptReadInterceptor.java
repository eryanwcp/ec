package com.eryansky.common.orm.mybatis.sensitive.interceptor;

import com.eryansky.common.orm.mybatis.sensitive.IEncrypt;
import com.eryansky.common.orm.mybatis.sensitive.annotation.*;
import com.eryansky.common.orm.mybatis.sensitive.encrypt.AesSupport;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveType;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveTypeRegisty;
import com.eryansky.common.orm.mybatis.sensitive.utils.JsonUtils;
import com.eryansky.common.orm.mybatis.sensitive.utils.SensitiveUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * 对响应结果进行拦截处理,对需要解密的字段进行解密
 * SQL样例：
 * 1. UPDATE tbl SET x=?, y =
 *
 * @author Eryan
 * @version 2019-12-13
 */
@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {java.sql.Statement.class})
})
public class DecryptReadInterceptor implements Interceptor {

    protected Log log = LogFactory.getLog(this.getClass());

    private static final String MAPPED_STATEMENT = "mappedStatement";

    private Properties properties = new Properties();
    private IEncrypt encrypt;

    public DecryptReadInterceptor() throws NoSuchAlgorithmException {
        this.encrypt = new AesSupport();
    }

    public DecryptReadInterceptor(IEncrypt encrypt) {
        Objects.requireNonNull(encrypt, "encrypt should not be null!");
        this.encrypt = encrypt;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final List<Object> results = (List<Object>) invocation.proceed();

        if (results.isEmpty()) {
            return results;
        }

        final ResultSetHandler statementHandler = SensitiveUtils.realTarget(invocation.getTarget());
        final MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        final MappedStatement mappedStatement = (MappedStatement) metaObject.getValue(MAPPED_STATEMENT);
        final ResultMap resultMap = mappedStatement.getResultMaps().isEmpty() ? null : mappedStatement.getResultMaps().get(0);

        Object result0 = results.get(0);
        SensitiveEncryptEnabled sensitiveEncryptEnabled = result0.getClass().getAnnotation(SensitiveEncryptEnabled.class);
        if (sensitiveEncryptEnabled == null || !sensitiveEncryptEnabled.value()) {
            return results;
        }

        final Map<String, EncryptField> encryptFieldMap = getEncryptFieldByResultMap(resultMap);
        final Map<String, EncryptJSONField> encryptJSONFieldMap = getEncryptJSONFieldByResultMap(resultMap);
        final Map<String, SensitiveBinded> sensitiveBindedMap = getSensitiveBindedByResultMap(resultMap);

        if (sensitiveBindedMap.isEmpty() && encryptFieldMap.isEmpty()) {
            return results;
        }

        Map<String,List<String>> mapList = Maps.newHashMap();
        for (Object obj : results) {
            final MetaObject objMetaObject = mappedStatement.getConfiguration().newMetaObject(obj);
            for (Map.Entry<String, EncryptField> entry : encryptFieldMap.entrySet()) {
                String property = entry.getKey();
                String value = (String) objMetaObject.getValue(property);
                if (null != value && !"".equals(value)) {
                    List<String> mapData = mapList.get(!"".equals(entry.getValue().type()) ? entry.getValue().type():encrypt.defaultType());
                    if(null == mapData){
                        mapData = Lists.newArrayList();
                    }
                    mapData.add(value);
                    mapList.put(!"".equals(entry.getValue().type()) ? entry.getValue().type():encrypt.defaultType(),mapData);
                }
            }

            for (Map.Entry<String, EncryptJSONField> entry : encryptJSONFieldMap.entrySet()) {
                String property = entry.getKey();
                Object value = objMetaObject.getValue(property);
                if (null != value) {
                    value =  value instanceof String ? JsonUtils.parseToObjectMap(value.toString()):(Map<String, Object>)value;
                    for(EncryptJSONFieldKey encryptJSONFieldKey: entry.getValue().encryptList()){
                        String keyValue = (String) ((Map<?, ?>) value).get(encryptJSONFieldKey.key());
                        if(keyValue != null && !"".equals(keyValue)){
                            List<String> mapData = mapList.get(!"".equals(encryptJSONFieldKey.type()) ? encryptJSONFieldKey.type():encrypt.defaultType());
                            if(null == mapData){
                                mapData = Lists.newArrayList();
                            }
                            mapData.add(keyValue);
                            mapList.put(!"".equals(encryptJSONFieldKey.type()) ? encryptJSONFieldKey.type():encrypt.defaultType(),mapData);
                        }
                    }
                }
            }

        }

        Map<String, List<String>> rDatas = encrypt.batchDecrypt(mapList);
        for (Object obj : results) {
            final MetaObject objMetaObject = mappedStatement.getConfiguration().newMetaObject(obj);
            for (Map.Entry<String, EncryptField> entry : encryptFieldMap.entrySet()) {
                String property = entry.getKey();
                String value = (String) objMetaObject.getValue(property);
                if (null != value && !"".equals(value)) {
                    objMetaObject.setValue(property, rDatas.get(!"".equals(entry.getValue().type()) ? entry.getValue().type():encrypt.defaultType()).get(0));
                    rDatas.get(!"".equals(entry.getValue().type()) ? entry.getValue().type():encrypt.defaultType()).remove(0);
                }

            }

            for (Map.Entry<String, EncryptJSONField> entry : encryptJSONFieldMap.entrySet()) {
                String property = entry.getKey();
                Object value = objMetaObject.getValue(property);
                boolean isString = false;
                if (null != value) {
                    if(value instanceof String){
                        value = JsonUtils.parseToObjectMap(value.toString());
                        isString = true;
                    }
                    for(EncryptJSONFieldKey encryptJSONFieldKey: entry.getValue().encryptList()){
                        String keyValue = (String) ((Map<String, Object>) value).get(!"".equals(encryptJSONFieldKey.type()) ? encryptJSONFieldKey.type():encrypt.defaultType());
                        if(keyValue != null && !"".equals(keyValue)){
                            ((Map<String, Object>) value).put(encryptJSONFieldKey.key(), rDatas.get(encryptJSONFieldKey.type()).get(0));
                            rDatas.get(!"".equals(encryptJSONFieldKey.type()) ? encryptJSONFieldKey.type():encrypt.defaultType()).remove(0);
                        }
                    }
                    objMetaObject.setValue(property, isString ? JsonUtils.parseToJSONString(value) : value);
                }

            }
            for (Map.Entry<String, SensitiveBinded> entry : sensitiveBindedMap.entrySet()) {
                String property = entry.getKey();
                SensitiveBinded sensitiveBinded = entry.getValue();
                String bindProperty = sensitiveBinded.bindField();
                SensitiveType sensitiveType = sensitiveBinded.value();
                try {
                    String value = (String) objMetaObject.getValue(bindProperty);
                    String resultValue = SensitiveTypeRegisty.get(sensitiveType).handle(value);
                    objMetaObject.setValue(property, resultValue);
                } catch (Exception e) {
                    //ignore it;
                }
            }
        }

        return results;
    }

    private Map<String, SensitiveBinded> getSensitiveBindedByResultMap(ResultMap resultMap) {
        if (resultMap == null) {
            return new HashMap<>(16);
        }
        Map<String, SensitiveBinded> sensitiveBindedMap = new HashMap<>(16);
        Class<?> clazz = resultMap.getType();
        for (Field field : clazz.getDeclaredFields()) {
            SensitiveBinded sensitiveBinded = field.getAnnotation(SensitiveBinded.class);
            if (sensitiveBinded != null) {
                sensitiveBindedMap.put(field.getName(), sensitiveBinded);
            }
        }
        return sensitiveBindedMap;
    }

    private Map<String, EncryptField> getEncryptFieldByResultMap(ResultMap resultMap) {
        if (resultMap == null) {
            return new HashMap<>(16);
        }

        return getEncryptFieldByType(resultMap.getType());
    }

    private Map<String, EncryptField> getEncryptFieldByType(Class<?> clazz) {
        Map<String, EncryptField> encryptFieldMap = new HashMap<>(16);

        for (Field field : clazz.getDeclaredFields()) {
            EncryptField encryptField = field.getAnnotation(EncryptField.class);
            if (encryptField != null) {
                encryptFieldMap.put(field.getName(), encryptField);
            }
        }
        return encryptFieldMap;
    }

    private Map<String, EncryptJSONField> getEncryptJSONFieldByResultMap(ResultMap resultMap) {
        if (resultMap == null) {
            return new HashMap<>(16);
        }

        return getEncryptJSONByType(resultMap.getType());
    }

    private Map<String, EncryptJSONField> getEncryptJSONByType(Class<?> clazz) {
        Map<String, EncryptJSONField> encryptJSONFieldMap = new HashMap<>(16);

        for (Field field : clazz.getDeclaredFields()) {
            EncryptJSONField encryptJSONField = field.getAnnotation(EncryptJSONField.class);
            if (encryptJSONField != null) {
                encryptJSONFieldMap.put(field.getName(), encryptJSONField);
            }
        }
        return encryptJSONFieldMap;
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
                encrypt = (IEncrypt) clazz.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
