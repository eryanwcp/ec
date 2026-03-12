package com.eryansky.j2cache.session;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class CaffeineEntry<T> implements Serializable {

    private String key;

    private T value;

    private long expire;

    private TimeUnit timeUnit;

    private boolean accessFresh;

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

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public boolean isAccessFresh() {
        return accessFresh;
    }

    public void setAccessFresh(boolean accessFresh) {
        this.accessFresh = accessFresh;
    }
}
