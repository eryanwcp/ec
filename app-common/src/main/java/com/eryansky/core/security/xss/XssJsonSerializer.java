package com.eryansky.core.security.xss;

import com.eryansky.common.utils.encode.EncodeUtils;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;

public class XssJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private final XssIgnore xssIgnore;

    public XssJsonSerializer() {
        this.xssIgnore = null;
    }

    public XssJsonSerializer(XssIgnore xssIgnore) {
        this.xssIgnore = xssIgnore;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        if ((xssIgnore != null && !xssIgnore.serializer()) || XssWhiteListMatcher.isWhitelisted()) {
            gen.writeString(value);
        } else {
            gen.writeString(EncodeUtils.htmlEscape(value));
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            XssIgnore xssIgnore = property.getAnnotation(XssIgnore.class);
            if (xssIgnore == null) {
                xssIgnore = property.getContextAnnotation(XssIgnore.class);
            }
            if (xssIgnore != null) {
                return new XssJsonSerializer(xssIgnore);
            }
        }
        XssIgnore xssIgnore = isControllerAnnotated(prov);
        if (xssIgnore != null) {
            return new XssJsonSerializer(xssIgnore);
        }

        return this;
    }

    private XssIgnore isControllerAnnotated(SerializerProvider prov) {
        HttpServletRequest request = null;
        try {
            request = SpringMVCHolder.getRequest();
        } catch (Exception e) {
            return null;
        }
        try {
            Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                XssIgnore xssIgnore = handlerMethod.getMethodAnnotation(XssIgnore.class);
                if (xssIgnore != null) {
                    return xssIgnore;
                }
                return handlerMethod.getBeanType().getAnnotation(XssIgnore.class);
            }
        } catch (Exception ignored) {
            // 解析失败时降级走默认过滤逻辑
        }
        return null;
    }

}