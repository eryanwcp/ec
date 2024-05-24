package com.eryansky.core.aop;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;

/**
 * <p> @Title 上下文拷贝装饰者模式
 *
 * @author Eryan
 * @date 2024-0524
 */
public class ContextCopyingDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        try {
            // 从父线程中获取上下文，然后应用到子线程中
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
            Map<String, String> previous = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (previous == null) {
                        MDC.clear();
                    } else {
                        MDC.setContextMap(previous);
                    }
                    RequestContextHolder.setRequestAttributes(requestAttributes);
                    runnable.run();
                } finally {
                    // 清除请求数据
                    MDC.clear();
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        } catch (IllegalStateException e) {
            return runnable;
        }
    }
}
