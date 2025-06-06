package com.eryansky.j2cache.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * 使用 Jackson 实现序列化
 * @see  GenericJackson2JsonRedisSerializer
 * j2cache.serialization = jackson
 */
public class JacksonSerializer extends GenericJackson2JsonRedisSerializer implements Serializer {

    public JacksonSerializer() {
        super();
    }

    public JacksonSerializer(ObjectMapper mapper) {
        super(mapper);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, true);
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
        //设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public String name() {
        return "jackson";
    }


    @Override
    public byte[] serialize(Object obj) {
        return super.serialize(obj);
    }

    @Override
    public Object deserialize(byte[] bytes) {
        return super.deserialize(bytes);
    }

}




