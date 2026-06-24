package com.eryansky.listener;

import org.apache.fory.resolver.TypeResolver;

public class DefaultForyTypeChecker implements org.apache.fory.resolver.TypeChecker {


    @Override
    public boolean checkType(TypeResolver resolver, String className) {
        boolean allow = className.startsWith("com.eryansky")
                || className.startsWith("java.")
                || className.startsWith("javax.");
        return allow;
    }
}
