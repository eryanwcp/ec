package com.eryansky.configure;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.core.aop.ContextCopyingDecorator;
import com.eryansky.core.orm.mybatis.entity.BaseEntity;
import com.eryansky.modules.notice.utils.MessageUtils;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.CacheUtils;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
// @ComponentScan({"com.eryansky.modules.**.event"})
@EnableAsync // 开始异步支持
public class DefaultAsyncConfigurer implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(DefaultAsyncConfigurer.class);

    private static final String REJECT_CACHE_KEY = "system_ops_warn_defaultAsyncExecutor";
    private static final String EXCEPTION_CACHE_KEY = "system_ops_warn_asyncUncaughtExceptionHandler";

    // @Value("${thread.pool.corePoolSize:10}")
    // private int corePoolSize;
    //
    // @Value("${thread.pool.maxPoolSize:20}")
    // private int maxPoolSize;
    //
    // @Value("${thread.pool.keepAliveSeconds:60}")
    // private int keepAliveSeconds;
    //
    // @Value("${thread.pool.queueCapacity:1024}")
    // private int queueCapacity;

    @Bean
    public Executor defaultAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程池数量，方法: 返回可用处理器的Java虚拟机的数量。
        int processors = Runtime.getRuntime().availableProcessors();
        int initProcessors = processors < 4 ? processors : processors - 1;

        executor.setCorePoolSize(initProcessors);
        executor.setMaxPoolSize(initProcessors * 2); // 最大线程数量
        executor.setQueueCapacity(Math.max(100000, initProcessors * 10000)); // 线程池的队列容量

        // for passing in request scope context 转换请求范围的上下文
        executor.setTaskDecorator(new ContextCopyingDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        executor.setRejectedExecutionHandler((Runnable r, ThreadPoolExecutor exe) -> {
            String msg = String.format(
                    "【%s】当前任务线程池队列已满（注：30分钟内仅提示一条）：%d; 默认线程数：%d; 最大线程数：%d; 执行中线程数：%d; 待执行队列数：%d; 提交任务数：%d; 完成任务数：%d; 可用队列长度：%d",
                    SpringContextHolder.getApplicationContext().getId(),
                    exe.getQueue().size(),
                    exe.getCorePoolSize(),
                    exe.getMaximumPoolSize(),
                    exe.getActiveCount(),
                    exe.getQueue().size(),
                    exe.getTaskCount(),
                    exe.getCompletedTaskCount(),
                    exe.getQueue().remainingCapacity()
            );

            log.error(msg);
            sendWarnMessageWithRateLimit(REJECT_CACHE_KEY, msg);

            // 如果线程池未关闭，则由调用者所在的线程执行任务 (CallerRunsPolicy 的逻辑)
            if (!exe.isShutdown()) {
                r.run();
            }
        });

        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return defaultAsyncExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            String msg = String.format(
                    "【%s】线程池执行任务发生未知异常（注：30分钟内仅提示一条）：%s.%s, %s",
                    SpringContextHolder.getApplicationContext().getId(),
                    method.getDeclaringClass().getName(),
                    method.getName(),
                    throwable.getMessage()
            );

            log.error(msg, throwable);
            sendWarnMessageWithRateLimit(EXCEPTION_CACHE_KEY, msg);
        };
    }

    /**
     * 发送系统警告消息，附带频率限制（依赖 CacheUtils 实现）
     *
     * @param cacheKey 缓存的 Key
     * @param msg      需要发送的消息内容
     */
    private void sendWarnMessageWithRateLimit(String cacheKey, String msg) {
        Boolean isTip = CacheUtils.get(cacheKey);
        if (isTip == null) {
            List<String> systemOpsWarnUserIds = UserUtils.findUsersByLoginNames(AppConstants.getSystemOpsWarnLoginNameList())
                    .stream()
                    .map(BaseEntity::getId)
                    .collect(Collectors.toList());

            if (Collections3.isEmpty(systemOpsWarnUserIds)) {
                systemOpsWarnUserIds = Lists.newArrayList(User.SUPERUSER_ID);
            }

            MessageUtils.sendToUserMessage(systemOpsWarnUserIds, msg);
            CacheUtils.put(cacheKey, true); // 存入缓存，依赖配置的过期时间（30分钟）
        }
    }
}