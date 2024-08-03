package com.eryansky.common.utils.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Deserializing JSON property as String with Jackson<br>
 * 实现将有{@link com.fasterxml.jackson.annotation.JsonRawValue}注解的
 * 内容为JSON的String类型字段反序列化为String的反序列化器实现
 *
 * @JsonRawValue
 * @JsonDeserialize(using = RawJsonDeserializer.class)
 * private String json;
 */
public class RawJsonDeserializer extends JsonDeserializer<String> {
    private static final Logger logger = LoggerFactory.getLogger(RawJsonDeserializer.class);

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        try {
            /*
             * 输入的JSON字符串解析为JSON对象(JsonNode),再输出为JSON字符串,
             * 相当于对JSON字符串进行了格式化
             */
            ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
            JsonNode node = mapper.readTree(jsonParser);
            return mapper.writeValueAsString(node);
        } catch (NumberFormatException e) {
            logger.error("解析字符串错误", e);
            return null;
        }
    }
}