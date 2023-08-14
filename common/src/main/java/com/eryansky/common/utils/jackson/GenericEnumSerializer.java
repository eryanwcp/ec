package com.eryansky.common.utils.jackson;

import com.eryansky.common.orm._enum.IGenericEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * IGenericEnum 类型枚举类序列化
 * @author Eryan
 * @date 2020-02-12
 *
 */
public class GenericEnumSerializer extends JsonSerializer<IGenericEnum> {
    @Override
    public void serialize(IGenericEnum value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (value != null) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("value",value.getValue());
            jsonGenerator.writeStringField("description",value.getDescription());
            jsonGenerator.writeEndObject();
        }
    }
}