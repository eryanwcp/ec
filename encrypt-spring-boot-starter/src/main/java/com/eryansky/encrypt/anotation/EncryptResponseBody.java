package com.eryansky.encrypt.anotation;

import com.eryansky.encrypt.advice.EncryptResultResponseBodyAdvice;
import com.eryansky.encrypt.advice.EncryptRResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.annotation.*;

/**
 * 加密注解
 * 注：需客户端自行传递加密方式以及加密密钥参数
 *
 * @author Eryan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EncryptResponseBody {
    /**
     * 是否启用
     * @return
     */
    String enable() default "true";
    /**
     * 指定自定义 ResponseBodyAdvice 处理策略 Class
     * 注：需要注入spring
     * @see EncryptResultResponseBodyAdvice
     * @see EncryptRResponseBodyAdvice
     * <p>
     * 默认为 EncryptResultResponseBodyAdvice.class 使用全局默认加密策略。
     *
     * @return 实现了 ResponseBodyAdvice 的 Class 类型
     */
    Class<? extends ResponseBodyAdvice> handle() default EncryptResultResponseBodyAdvice.class; // 优化 3：移除内层 <?>，避免默认值赋值时的泛型警告
}
