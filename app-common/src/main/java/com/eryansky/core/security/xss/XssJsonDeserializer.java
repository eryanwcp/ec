package com.eryansky.core.security.xss;

import com.eryansky.common.utils.encode.EncodeUtils;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * XSS JSON 反序列化器（结合静态常量单例与 Controller 级注解 Request 缓存）
 */
public class XssJsonDeserializer extends JsonDeserializer<String> implements ContextualDeserializer {

    private static final Logger log = LoggerFactory.getLogger(XssJsonDeserializer.class);

    // 预创建静态常量实例，彻底避免动态 new 对象
    private static final XssJsonDeserializer DEFAULT_INSTANCE = new XssJsonDeserializer(false);
    private static final XssJsonDeserializer DESERIALIZE_INSTANCE_SKIP = new XssJsonDeserializer(true);

    // Request 属性缓存 Key 与空注解占位符
    private static final String CONTROLLER_XSS_IGNORE_CACHE_KEY = "XSS_IGNORE_CONTROLLER_DESER_CACHE";
    private static final Object NULL_HOLDER = new Object();

    // 是否跳过 XSS 反序列化（反转义）
    private final boolean skipDeserialize;

    public XssJsonDeserializer() {
        this(false);
    }

    public XssJsonDeserializer(boolean skipDeserialize) {
        this.skipDeserialize = skipDeserialize;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }

        // 如果配置了跳过反序列化，或者命中全局白名单，直接返回原字符串
        if (skipDeserialize || XssWhiteListMatcher.isWhitelisted()) {
            return value;
        }

        return EncodeUtils.htmlUnescape(value);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        XssIgnore xssIgnore = null;

        // 1. 优先读取 Bean 字段/属性或 setter 参数上的 @XssIgnore
        if (property != null) {
            xssIgnore = property.getAnnotation(XssIgnore.class);
            if (xssIgnore == null) {
                xssIgnore = property.getContextAnnotation(XssIgnore.class);
            }
        }

        // 2. 字段未配置，检查 Controller 类或方法上是否有 @XssIgnore（带有 Request 缓存）
        if (xssIgnore == null) {
            xssIgnore = isControllerAnnotated(ctxt);
        }

        // 3. 根据注解中的 deserializer 配置匹配预设单例
        if (xssIgnore != null && !xssIgnore.deserializer()) {
            return DESERIALIZE_INSTANCE_SKIP;
        }

        return DEFAULT_INSTANCE;
    }

    /**
     * 查找当前请求对应的 Controller 或 HandlerMethod 上是否有注解（包含 Request 属性缓存）
     */
    private XssIgnore isControllerAnnotated(DeserializationContext ctxt) {
        HttpServletRequest request;
        try {
            request = SpringMVCHolder.getRequest();
            if (request == null) {
                return null;
            }

            // 尝试从 Request 属性中直接获取缓存结果
            Object cached = request.getAttribute(CONTROLLER_XSS_IGNORE_CACHE_KEY);
            if (cached != null) {
                return cached == NULL_HOLDER ? null : (XssIgnore) cached;
            }

            XssIgnore xssIgnore = null;
            Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
            if (handler instanceof HandlerMethod handlerMethod) {
                // 优先取 HandlerMethod 方法注解，其次取 Controller 类注解
                xssIgnore = handlerMethod.getMethodAnnotation(XssIgnore.class);
                if (xssIgnore == null) {
                    xssIgnore = handlerMethod.getBeanType().getAnnotation(XssIgnore.class);
                }
            }

            // 将解析到的结果（或 NULL_HOLDER）写入 Request 属性，供本次请求后续字段共享
            request.setAttribute(CONTROLLER_XSS_IGNORE_CACHE_KEY, xssIgnore != null ? xssIgnore : NULL_HOLDER);
            return xssIgnore;
        } catch (Exception e) {
//            log.error(e.getMessage());
            return null;
        }
    }
}