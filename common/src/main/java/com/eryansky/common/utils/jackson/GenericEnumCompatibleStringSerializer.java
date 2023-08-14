package com.eryansky.common.utils.jackson;

import com.eryansky.common.orm._enum.IGenericEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * IGenericEnum 类型枚举类序列化（兼容旧程序）
 * 增加 属性名称+View字段 例如：enumName + enumNameView
 * @author Eryan
 * @date 2020-02-12
 */
public class GenericEnumCompatibleStringSerializer extends JsonSerializer<IGenericEnum> {
    @Override
    public void serialize(IGenericEnum value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (value != null) {
            String fieldName = jsonGenerator.getOutputContext().getCurrentName();
            jsonGenerator.writeString(value.getValue());
            jsonGenerator.writeStringField(fieldName + "View", value.getDescription());
        }
    }
}