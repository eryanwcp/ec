package com.eryansky.configure;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.eryansky.core.aop.ContextCopyingDecorator;
import com.eryansky.core.orm.mybatis.entity.BaseEntity;
import com.eryansky.modules.notice.utils.MessageUtils;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.utils.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
//@ComponentScan({"com.eryansky.modules.**.event"})
//开始异步支持
@EnableAsync
public class DefaultAsyncConfigurer implements AsyncConfigurer {

    private static Logger log = LoggerFactory.getLogger(DefaultAsyncConfigurer.class);

//    @Value("${thread.pool.corePoolSize:10}")
//    private int corePoolSize;
//
//    @Value("${thread.pool.maxPoolSize:20}")
//    private int maxPoolSize;
//
//    @Value("${thread.pool.keepAliveSeconds:60}")
//    private int keepAliveSeconds;
//
//    @Value("${thread.pool.queueCapacity:1024}")
//    private int queueCapacity;

    @Bean
    public Executor defaultAsyncExecutor() {
        //线程池
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程池数量，方法: 返回可用处理器的Java虚拟机的数量。
        int processors = Runtime.getRuntime().availableProcessors();
        int initProcessors = processors < 4 ? processors : processors - 1;
        executor.setCorePoolSize(initProcessors);
        executor.setMaxPoolSize(initProcessors * 10);//最大线程数量
        executor.setQueueCapacity(initProcessors * 1000);//线程池的队列容量
        // for passing in request scope context 转换请求范围的上下文
        executor.setTaskDecorator(new ContextCopyingDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setRejectedExecutionHandler((Runnable r, ThreadPoolExecutor exe) -> {
            StringBuffer msg = new StringBuffer();
            msg.append("当前任务线程池队列已满：").append(executor.getQueueSize())
                    .append(";最大线程数：").append(executor.getMaxPoolSize())
                    .append(";提交任务数：").append(executor.getThreadPoolExecutor().getTaskCount())
                    .append(";完成任务数：").append(executor.getThreadPoolExecutor().getCompletedTaskCount())
                    .append(";执行中线程数：").append(executor.getActiveCount())
                    .append(";待执行队列数：").append(executor.getThreadPoolExecutor().getQueue().size())
                    .append(";可用队列长度：").append(executor.getThreadPoolExecutor().getQueue().remainingCapacity());
            log.error(msg.toString());
            MessageUtils.sendToUserMessage(User.SUPERUSER_ID,msg.toString());
            List<String>  systemOpsWarnUserIds = UserUtils.findUsersByLoginNames(AppConstants.getSystemOpsWarnLoginNameList()).stream().map(BaseEntity::getId).filter(id ->!User.SUPERUSER_ID.equals(id)).collect(Collectors.toList());
            MessageUtils.sendToUserMessage(systemOpsWarnUserIds,msg.toString());
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
            StringBuffer msg = new StringBuffer();
            msg.append("线程池执行任务发生未知异常：").append(method.getDeclaringClass().getName()).append(".").append(method.getName()).append(",").append(throwable.getMessage());
            log.error(msg.toString(),throwable);
            MessageUtils.sendToUserMessage(User.SUPERUSER_ID,msg.toString());
            List<String>  systemOpsWarnUserIds = UserUtils.findUsersByLoginNames(AppConstants.getSystemOpsWarnLoginNameList()).stream().map(BaseEntity::getId).filter(id ->!User.SUPERUSER_ID.equals(id)).collect(Collectors.toList());
            MessageUtils.sendToUserMessage(systemOpsWarnUserIds,msg.toString());
        };
    }

}
