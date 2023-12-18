/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.vo;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Eryan
 * @date 2023-12-15
 */
public class ExtendAttrItem implements Serializable {

    private String key;
    private Object value;

    public ExtendAttrItem() {
    }

    public ExtendAttrItem(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public ExtendAttrItem setKey(String key) {
        this.key = key;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public ExtendAttrItem setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtendAttrItem item = (ExtendAttrItem) o;
        return Objects.equals(key, item.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
