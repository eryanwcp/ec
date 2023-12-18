/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.vo;

import com.eryansky.common.utils.mapper.JsonMapper;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Eryan
 * @date 2023-12-15
 */
public class ExtendAttr implements Serializable {

    private Collection<ExtendAttrItem> items;

    public ExtendAttr() {
        items = Sets.newHashSet();
    }


    public Collection<ExtendAttrItem> getItems() {
        return items;
    }

    public ExtendAttr setItems(Collection<ExtendAttrItem> items) {
        this.items = items;
        return this;
    }

    public boolean addIfNotExist(String key, Object value) {
        return this.items.add(new ExtendAttrItem(key,value));
    }

    public void addOrUpdate(String key, Object value) {
        boolean flag =  addIfNotExist(key,value);
        if(!flag){
            update(key,value);
        }
    }

    public ExtendAttr update(String key, Object value) {
        this.items.stream().filter(v -> v.getKey().equals(key)).findFirst().ifPresent(item -> item.setValue(value));
        return this;
    }

    public ExtendAttrItem getByKey(String key) {
        return this.items.stream().filter(v -> v.getKey().equals(key)).findFirst().orElse(null);
    }

    public Map<String, Object> toMap() {
        return this.items.stream().collect(Collectors.toMap(ExtendAttrItem::getKey, ExtendAttrItem::getValue));
    }

    public String toJson() {
        return JsonMapper.toJsonString(this);
    }

    @Override
    public String toString() {
        return JsonMapper.toJsonString(toMap());
    }

}
