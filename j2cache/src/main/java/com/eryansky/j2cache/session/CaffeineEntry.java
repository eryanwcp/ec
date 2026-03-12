package com.eryansky.j2cache.session;

import java.io.Serializable;

public class CaffeineEntry<T> implements Serializable {

    private String key;

    private T value;

    private long expire;

    public CaffeineEntry() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

}
