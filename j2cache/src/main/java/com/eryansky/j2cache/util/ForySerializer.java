package com.eryansky.j2cache.util;

import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.Language;
import org.apache.fory.logging.LoggerFactory;
import org.apache.fory.resolver.TypeChecker;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * 使用 Apache Fory 实现序列化
 * j2cache.serialization = fory
 */
public class ForySerializer implements Serializer {

    private final static Logger log = org.slf4j.LoggerFactory.getLogger(ForySerializer.class);

    public static final String TYPE_CHECKER_CLASS = "j2cache.fory.typeCheckerClass";
    private static volatile ThreadSafeFory fory = null;
    private static volatile TypeChecker typeChecker;

    static {
        LoggerFactory.useSlf4jLogging(true);
        // Try to load an external TypeChecker implementation from system property
        String typeCheckerClass = System.getProperty(TYPE_CHECKER_CLASS);
        if (typeCheckerClass != null && !typeCheckerClass.trim().isEmpty()) {
            try {
                Class<?> cls = Class.forName(typeCheckerClass.trim());
                Object inst = cls.getDeclaredConstructor().newInstance();
                if (inst instanceof TypeChecker) {
                    typeChecker = (TypeChecker) inst;
                    log.info("Loaded external TypeChecker implementation: {}", typeCheckerClass);
                } else {
                    throw new IllegalArgumentException(typeCheckerClass + " does not implement TypeChecker");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate TypeChecker: " + typeCheckerClass, e);
            }
        }


        buildFory();
    }

    private static synchronized void buildFory() {
        fory = Fory.builder().withLanguage(Language.JAVA)
                // Allow to deserialize objects unknown types, more flexible
                // but may be insecure if the classes contains malicious code.
                .withRefTracking(true)
                .requireClassRegistration(null != typeChecker)
                .withTypeChecker(typeChecker)
                .buildThreadSafeFory();
    }

    /**
     * Replace the TypeChecker at runtime and rebuild the underlying Fory instance.
     * Provide a non-null TypeChecker implementation.
     */
    public static synchronized void setTypeChecker(TypeChecker checker) {
        if (checker == null) {
            throw new IllegalArgumentException("TypeChecker must not be null");
        }
        typeChecker = checker;
        buildFory();
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