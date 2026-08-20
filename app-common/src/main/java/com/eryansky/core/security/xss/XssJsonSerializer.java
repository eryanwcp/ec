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

/**
 * XSS JSON 序列化器（结合静态常量单例与 Controller 级注解请求缓存）
 */
public class XssJsonSerializer extends JsonSerializer<String> implements ContextualSerializer {

    // 预创建静态常量实例，避免频繁 new 对象
    private static final XssJsonSerializer DEFAULT_INSTANCE = new XssJsonSerializer(false);
    private static final XssJsonSerializer SERIALIZE_INSTANCE_SKIP = new XssJsonSerializer(true);

    // 缓存 Key 与空占位符（避免无注解时重复解析反射）
    private static final String CONTROLLER_XSS_IGNORE_CACHE_KEY = "XSS_IGNORE_CONTROLLER_ATTRIBUTE_CACHE";
    private static final Object NULL_HOLDER = new Object();

    // 是否跳过序列化脱敏
    private final boolean skipSerialize;

    public XssJsonSerializer() {
        this(false);
    }

    public XssJsonSerializer(boolean skipSerialize) {
        this.skipSerialize = skipSerialize;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        // 如果注解指定跳过脱敏，或者在全局白名单内，则直接输出原字符串
        if (skipSerialize || XssWhiteListMatcher.isWhitelisted()) {
            gen.writeString(value);
        } else {
            gen.writeString(EncodeUtils.htmlEscape(value));
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        XssIgnore xssIgnore = null;

        // 1. 优先读取属性/字段及 getter 上标注的 @XssIgnore
        if (property != null) {
            xssIgnore = property.getAnnotation(XssIgnore.class);
            if (xssIgnore == null) {
                xssIgnore = property.getContextAnnotation(XssIgnore.class);
            }
        }

        // 2. 字段上未配置，再检查 Controller 类或方法上是否标注了 @XssIgnore（带 Request 级缓存）
        if (xssIgnore == null) {
            xssIgnore = isControllerAnnotated(prov);
        }

        // 3. 根据注解配置匹配预创建的单例对象
        if (xssIgnore != null && !xssIgnore.serializer()) {
            return SERIALIZE_INSTANCE_SKIP;
        }

        return DEFAULT_INSTANCE;
    }

    /**
     * 判断当前 Controller 方法或类上是否有 @XssIgnore 注解（包含 Request 属性缓存机制）
     */
    private XssIgnore isControllerAnnotated(SerializerProvider prov) {
        HttpServletRequest request;
        try {
            request = SpringMVCHolder.getRequest();
            if (request == null) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        // 从当前 Request 属性中尝试读取缓存结果
        Object cached = request.getAttribute(CONTROLLER_XSS_IGNORE_CACHE_KEY);
        if (cached != null) {
            return cached == NULL_HOLDER ? null : (XssIgnore) cached;
        }

        XssIgnore xssIgnore = null;
        try {
            Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                // 优先取方法上的注解，再取类/Controller 上的注解
                xssIgnore = handlerMethod.getMethodAnnotation(XssIgnore.class);
                if (xssIgnore == null) {
                    xssIgnore = handlerMethod.getBeanType().getAnnotation(XssIgnore.class);
                }
            }
        } catch (Exception ignored) {
            // 解析失败时降级处理
        }

        // 将解析结果（或空标记）写入 Request 属性中，避免同一次请求内后续字段重复反射
        request.setAttribute(CONTROLLER_XSS_IGNORE_CACHE_KEY, xssIgnore != null ? xssIgnore : NULL_HOLDER);
        return xssIgnore;
    }
}