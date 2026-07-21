package com.eryansky.core.rpc.utils;

import com.eryansky.j2cache.util.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializerFactory {

    private static final Map<String, Serializer> SERIALIZERS = new ConcurrentHashMap<>();

    static {
        // 注册支持的序列化器
        register(new JacksonMsgPackSerializer());
        register(new JacksonSerializer());
        register(new ForySerializer());
        register(new JavaSerializer());
        register(new JsonSerializer());
    }

    public static void register(Serializer serializer) {
        SERIALIZERS.put(serializer.name(), serializer);
    }

    public static Serializer getSerializer(String type) {
        if (type == null || type.isEmpty()) {
            return SERIALIZERS.get(ForySerializer.NAME);
//            return SERIALIZERS.get(JacksonMsgPackSerializer.NAME);
        }
        return SERIALIZERS.getOrDefault(type, SERIALIZERS.get(ForySerializer.NAME));
//        return SERIALIZERS.getOrDefault(type, SERIALIZERS.get(JacksonMsgPackSerializer.NAME));
    }
}