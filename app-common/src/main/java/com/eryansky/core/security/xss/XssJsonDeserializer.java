package com.eryansky.core.security.xss;

import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;

public class XssJsonDeserializer extends JsonDeserializer<String> implements ContextualDeserializer {

    private XSSConfig xssConfig;

    public XssJsonDeserializer() {
    }

    public XssJsonDeserializer(XSSConfig xssConfig) {
        this.xssConfig = xssConfig;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }

        if ((xssConfig != null && !xssConfig.deserializer()) || XssWhiteListMatcher.isWhitelisted()) {
            return value;
        }

        return HtmlUtils.htmlUnescape(value);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property != null) {
            XSSConfig xssConfig = property.getAnnotation(XSSConfig.class);
            if (xssConfig == null) {
                xssConfig = property.getContextAnnotation(XSSConfig.class);
            }
            if (xssConfig != null) {
                return new XssJsonDeserializer(xssConfig);
            }
        }

        XSSConfig xssConfig = isControllerAnnotated(ctxt);
        if (xssConfig != null) {
            return new XssJsonDeserializer(xssConfig);
        }

        return this;
    }

    /**
     * 查找当前请求对应的 Controller 或 HandlerMethod 上是否有注解
     */
    private XSSConfig isControllerAnnotated(DeserializationContext ctxt) {
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