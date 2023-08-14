package com.eryansky.common.utils.jackson;

import com.eryansky.common.orm._enum.IGenericEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * IGenericEnum 类型枚举类序列化String value
 * @author Eryan
 * @date 2020-02-12
 *
 */
public class GenericEnumStringSerializer extends JsonSerializer<IGenericEnum> {
    @Override
    public void serialize(IGenericEnum value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        String text = (value == null ? null : value.getValue());
        if (text != null) {
            jsonGenerator.writeString(text);
        }
    }
}