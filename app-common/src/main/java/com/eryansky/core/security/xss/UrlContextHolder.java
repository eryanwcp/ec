package com.eryansky.core.security.xss;

public class UrlContextHolder {
    private static final ThreadLocal<String> CURRENT_URI = new ThreadLocal<>();

    public static void setUri(String uri) {
        CURRENT_URI.set(uri);
    }

    public static String getUri() {
        return CURRENT_URI.get();
    }

    public static void clear() {
        CURRENT_URI.remove();
    }
}