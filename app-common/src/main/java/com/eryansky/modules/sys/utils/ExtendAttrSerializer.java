package com.eryansky.modules.sys.utils;

import com.eryansky.modules.sys.vo.ExtendAttr;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 自定义Json序列化
 */
public class ExtendAttrSerializer extends JsonSerializer<ExtendAttr> {

    @Override
    public void serialize(ExtendAttr value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
            JsonProcessingException {
        jgen.writeObject(value.toMap());
    }
}