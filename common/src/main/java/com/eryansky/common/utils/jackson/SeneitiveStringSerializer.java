package com.eryansky.common.utils.jackson;

import com.eryansky.common.orm.mybatis.sensitive.annotation.SensitiveField;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveType;
import com.eryansky.common.orm.mybatis.sensitive.utils.SensitiveUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.util.Objects;

/**
 * Seneitive 类型枚举类序列化String value
 *
 * @author Eryan
 * @date 2020-02-12
 */
public class SeneitiveStringSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private SensitiveType type;

    public SeneitiveStringSerializer() {
    }

    public SeneitiveStringSerializer(final SensitiveType type) {
        this.type = type;
    }


    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(SensitiveUtils.getSensitive(value, type));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) { // 为空直接跳过
            if (Objects.equals(property.getType().getRawClass(), String.class)) { // 非 String 类直接跳过
                SensitiveField sensitiveInfo = property.getAnnotation(SensitiveField.class);
                if (sensitiveInfo == null) {
                    sensitiveInfo = property.getContextAnnotation(SensitiveField.class);
                }
                if (sensitiveInfo != null) { // 如果能得到注解，就将注解的 value 传入 SensitiveInfoSerialize
                    return new SeneitiveStringSerializer(sensitiveInfo.value());
                }
            }
            return prov.findValueSerializer(property.getType(), property);
        }
        return prov.findNullValueSerializer(property);
    }
}