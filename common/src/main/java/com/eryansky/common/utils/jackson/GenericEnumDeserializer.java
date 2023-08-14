package com.eryansky.common.utils.jackson;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * IGenericEnum 类型枚举类反序列化
 *
 * @author Eryan
 * @date 2020-02-12
 */
public class GenericEnumDeserializer<T extends Enum<T> & IGenericEnum<T>> extends JsonDeserializer<T> implements ContextualDeserializer {

    private static final Logger logger = LoggerFactory.getLogger(GenericEnumDeserializer.class);

    private JavaType type;

    public GenericEnumDeserializer() {
    }

    public GenericEnumDeserializer(final JavaType type) {
        this.type = type;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        try {
            return GenericEnumUtils.getByValue((Class<T>) type.getRawClass(), value);
        } catch (Exception e) {
            logger.error("解析IGenericEnum错误", e);
            return null;
        }
    }

    @Override
    public JsonDeserializer<T> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JavaType type = ctxt.getContextualType() != null
                ? ctxt.getContextualType()
                : property.getMember().getType();
        return new GenericEnumDeserializer(type);
    }
}