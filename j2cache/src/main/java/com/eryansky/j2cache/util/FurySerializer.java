package com.eryansky.j2cache.util;

import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.Language;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用 Apache Fury 实现序列化
 * j2cache.serialization = fury
 */
public class FurySerializer implements Serializer {

    private final ThreadSafeFury fury;

    public FurySerializer(){
        this.fury = Fury.builder().withLanguage(Language.JAVA)
                // Allow to deserialize objects unknown types, more flexible
                // but may be insecure if the classes contains malicious code.
                .withRefTracking(true)
                .requireClassRegistration(false)
                .buildThreadSafeFury();
    }

    @Override
    public String name() {
        return "fury";
    }

    @Override
    public byte[] serialize(Object obj) throws IOException {
        return fury.serialize(obj);
    }

    @Override
    public Object deserialize(byte[] bytes) throws IOException {
        return fury.deserialize(bytes);
    }

}