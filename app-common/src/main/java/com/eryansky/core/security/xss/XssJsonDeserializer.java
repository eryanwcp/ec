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

    private static final XssJsonDeserializer DEFAULT_INSTANCE = new XssJsonDeserializer();

    private final XssIgnore xssIgnore;

    public XssJsonDeserializer() {
        this.xssIgnore = null;
    }

    public XssJsonDeserializer(XssIgnore xssIgnore) {
        this.xssIgnore = xssIgnore;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }

        if ((xssIgnore != null && !xssIgnore.deserializer()) || XssWhiteListMatcher.isWhitelisted()) {
            return value;
        }

        return HtmlUtils.htmlUnescape(value);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property != null) {
            XssIgnore xssIgnore = property.getAnnotation(XssIgnore.class);
            if (xssIgnore == null) {
                xssIgnore = property.getContextAnnotation(XssIgnore.class);
            }
            if (xssIgnore != null) {
                return new XssJsonDeserializer(xssIgnore);
            }
        }

        XssIgnore xssIgnore = isControllerAnnotated(ctxt);
        if (xssIgnore != null) {
            return new XssJsonDeserializer(xssIgnore);
        }

        return DEFAULT_INSTANCE;
    }

    /**
     * 查找当前请求对应的 Controller 或 HandlerMethod 上是否有注解
     */
    private XssIgnore isControllerAnnotated(DeserializationContext ctxt) {
        HttpServletRequest request = null;
        try {
            request = SpringMVCHolder.getRequest();
        } catch (Exception e) {
            return null;
        }
        try {
            Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
            if (handler instanceof HandlerMethod handlerMethod) {
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