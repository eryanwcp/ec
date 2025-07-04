package com.eryansky.j2cache.util;

import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.Language;
import org.apache.fory.logging.LoggerFactory;

import java.io.IOException;

/**
 * 使用 Apache Fory 实现序列化
 * j2cache.serialization = fory
 */
public class ForySerializer implements Serializer {

    private static ThreadSafeFory fory = null;

    static {
        LoggerFactory.useSlf4jLogging(true);
        fory = Fory.builder().withLanguage(Language.JAVA)
                // Allow to deserialize objects unknown types, more flexible
                // but may be insecure if the classes contains malicious code.
                .withRefTracking(true)
                .requireClassRegistration(false)
                .buildThreadSafeFory();
    }

    public ForySerializer(){

    }

    @Override
    public String name() {
        return "fory";
    }

    @Override
    public byte[] serialize(Object obj) throws IOException {
        return fory.serialize(obj);
    }

    @Override
    public Object deserialize(byte[] bytes) throws IOException {
        return fory.deserialize(bytes);
    }

}