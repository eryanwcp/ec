package com.eryansky.core.security.xss;

import com.eryansky.common.utils.encode.EncodeUtils;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;

public class XssJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private XSSConfig xssConfig;

    public XssJsonSerializer() {
    }

    public XssJsonSerializer(XSSConfig xssConfig) {
        this.xssConfig = xssConfig;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        if ((xssConfig != null && !xssConfig.serializer()) || XssWhiteListMatcher.isWhitelisted()) {
            gen.writeString(value);
        } else {
            gen.writeString(EncodeUtils.htmlEscape(value));
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        if (property != null) {
            XSSConfig xssConfig = property.getAnnotation(XSSConfig.class);
            if (xssConfig == null) {
                xssConfig = property.getContextAnnotation(XSSConfig.class);
            }
            if (xssConfig != null) {
                return new XssJsonSerializer(xssConfig);
            }
        }
        XSSConfig xssConfig = isControllerAnnotated(prov);
        if (xssConfig != null) {
            return new XssJsonSerializer(xssConfig);
        }

        return this;
    }

    private XSSConfig isControllerAnnotated(SerializerProvider prov) {
        HttpServletRequest request = SpringMVCHolder.getRequest();
        try {
            Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
            if (handler instanceof HandlerMethod handlerMethod) {
                XSSConfig xssConfig = handlerMethod.getMethodAnnotation(XSSConfig.class);
                if (xssConfig != null) {
                    return xssConfig;
                }
                return handlerMethod.getBeanType().getAnnotation(XSSConfig.class);
            }
        } catch (Exception ignored) {
            // 解析失败时降级走默认过滤逻辑
        }
        return null;
    }

}