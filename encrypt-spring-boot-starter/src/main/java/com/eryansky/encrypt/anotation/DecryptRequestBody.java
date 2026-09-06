package com.eryansky.encrypt.anotation;

import com.eryansky.encrypt.advice.DecryptRequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.lang.annotation.*;

/**
 * 加密注解
 * @author Eryan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DecryptRequestBody {
    /**
     * 是否启用
     * @return
     */
    String enable() default "true";

    /**
     * 是否使用默认处理策略
     * @return
     */
    boolean defaultHandle() default true;
    /**
     * 指定自定义 RequestBodyAdvice 处理策略 Class
     * 注：需要注入spring
     * @see DecryptRequestBodyAdvice
     * <p>
     * 默认为 DecryptRequestBodyAdvice.class 使用全局默认加密策略。
     *
     * @return 实现了 RequestBodyAdvice 的 Class 类型
     */
    Class<? extends RequestBodyAdvice> handle() default DecryptRequestBodyAdvice.class;

}
