package com.eryansky.common.utils.jackson;

import java.io.IOException;

import com.eryansky.common.utils.encode.EncodeUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * 反序列化
 */
public class XssDefaultJsonDeserializer extends StdDeserializer<String> {
    public XssDefaultJsonDeserializer() {
        this(null);
    }

    public XssDefaultJsonDeserializer(Class<String> vc) {
        super(vc);
    }

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        //return StringEscapeUtils.escapeEcmaScript(jsonParser.getText());
        return EncodeUtils.htmlUnescape(jsonParser.getText());
    }
}