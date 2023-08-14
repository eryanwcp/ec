/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.sn;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Eryan
 * @date 2020-10-08
 */
public class MaxSerialItem implements Serializable {

    public static final String DEFAULT_KEY = "maxSerial";
    public static final Long DEFAULT_VALUE = 0L;

    private String key;
    private Long value;

    public MaxSerialItem() {
        this.key = DEFAULT_KEY;
        this.value = DEFAULT_VALUE;
    }

    public MaxSerialItem(String key, Long value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public MaxSerialItem setKey(String key) {
        this.key = key;
        return this;
    }

    public Long getValue() {
        return value;
    }

    public MaxSerialItem setValue(Long value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MaxSerialItem item = (MaxSerialItem) o;
        return Objects.equals(key, item.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
