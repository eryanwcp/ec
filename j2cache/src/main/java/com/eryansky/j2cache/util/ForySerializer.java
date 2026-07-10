package com.eryansky.j2cache.util;

import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.Language;
import org.apache.fory.logging.LoggerFactory;
import org.apache.fory.resolver.AllowListChecker;
import org.apache.fory.resolver.DisallowedList;
import org.slf4j.Logger;
import java.io.IOException;

/**
 * 使用 Apache Fory 实现序列化
 * j2cache.serialization = fory
 */
public class ForySerializer implements Serializer {

    private final static Logger log = org.slf4j.LoggerFactory.getLogger(ForySerializer.class);


    private static volatile ThreadSafeFory fory = null;
    private static final AllowListChecker typeChecker;

    public static final String FORY = "fory";

    static {
        LoggerFactory.useSlf4jLogging(true);
//        AllowListChecker checker = new AllowListChecker(AllowListChecker.CheckLevel.STRICT);
        AllowListChecker checker = new AllowListChecker(AllowListChecker.CheckLevel.WARN);
        checker.disallowClasses(DisallowedList.getDisallowedClasses());

        log.info("Using default TypeChecker: {} checkLevel: {}", checker.getClass().getName(), checker.getCheckLevel().name());
        typeChecker = checker;

        fory = Fory.builder().withLanguage(Language.JAVA)
                // Allow to deserialize objects unknown types, more flexible
                // but may be insecure if the classes contains malicious code.
                .withRefTracking(true)
                .requireClassRegistration(false)
                .withTypeChecker(typeChecker)
                .buildThreadSafeFory();
    }

    public ForySerializer(){

    }

    @Override
    public String name() {
        return FORY;
    }

    @Override
    public byte[] serialize(Object obj) throws IOException {
        return fory.serialize(obj);
    }

    @Override
    public Object deserialize(byte[] bytes) throws IOException {
        return fory.deserialize(bytes);
    }

    public static ThreadSafeFory getFory() {
        return fory;
    }

    public static AllowListChecker getTypeChecker() {
        return typeChecker;
    }
}