package com.eryansky.j2cache.util;

import com.eryansky.common.utils.mapper.JsonMapper;

import java.io.IOException;

/**
 * 使用 Jackson 实现序列化
 * j2cache.serialization = jackson
 */
public class JacksonSerializer implements Serializer {

    private final JsonMapper objectMapper;

    public JacksonSerializer(){
        this.objectMapper = new JsonMapper();
    }

    @Override
    public String name() {
        return "jackson";
    }

    @Override
    public byte[] serialize(Object obj) throws IOException {
        return objectMapper.writeValueAsBytes(obj);
    }

    @Override
    public Object deserialize(byte[] bytes) throws IOException {
        return objectMapper.readValue(new String(bytes),Object.class);
    }

}