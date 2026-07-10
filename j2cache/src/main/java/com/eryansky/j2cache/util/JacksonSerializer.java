package com.eryansky.j2cache.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 使用 Jackson 实现序列化
 * j2cache.serialization = jackson
 */
public class JacksonSerializer implements Serializer {

    public static final String JACKSON = "jackson";

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, true);
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));

        //设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 允許反序列化空明文對象，防止低版本傳出空字節流時爆出 MismatchedInputException
//        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
//        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        // 开启全可见性支持，允许 Jackson 读写没有 getter/setter 的私有复杂属性
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 开启全局多态类型支持
        // 因为 deserialize 接口方法没有 Class 参数，必须将类元数据类型写入二进制流中
        // 使用 NON_FINAL 策略：在生成的 MsgPack 二进制中注入 ["类全限定名", {数据主体}]，从而完美还原 Object
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY
        );
    }

    @Override
    public String name() {
        return JACKSON;
    }


    @Override
    public byte[] serialize(Object obj) throws IOException {
        if (obj == null) {
            return new byte[0];
        }
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (Exception e) {
            throw new IOException("序列化失败, 目标对象类: " + obj.getClass().getName(), e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            // 依靠二进制流中自带的类元数据，自动寻找 ClassLoader 并精准闭环还原回原有的业务 DTO 对象
            return objectMapper.readValue(bytes, Object.class);
        } catch (Exception e) {
            throw new IOException("反序列化失败", e);
        }
    }

}




