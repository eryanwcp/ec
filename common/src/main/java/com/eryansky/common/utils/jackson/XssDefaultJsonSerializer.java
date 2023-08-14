package com.eryansky.common.utils.jackson;

import com.eryansky.common.utils.encode.EncodeUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Xss序列化
 *
 * @author Eryan
 * @date 2021-11-29
 */
public class XssDefaultJsonSerializer extends JsonSerializer<String> {


    public XssDefaultJsonSerializer() {
    }


    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(EncodeUtils.htmlEscape(value));
    }

}