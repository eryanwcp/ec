package com.eryansky.configure;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import com.eryansky.modules.notice.utils.MessageUtils;
import com.eryansky.modules.sys.mapper.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
//@ComponentScan({"com.eryansky.modules.**.event"})
//开始异步支持
@EnableAsync
public class AopConfigurer implements AsyncConfigurer {

    private static Logger logger = LoggerFactory.getLogger(AopConfigurer.class);

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

    @Override
    public Executor getAsyncExecutor() {
        //线程池
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程池数量，方法: 返回可用处理器的Java虚拟机的数量。
        int processors = Runtime.getRuntime().availableProcessors();
        int initProcessors = processors < 4 ? processors : processors - 1;
        executor.setCorePoolSize(initProcessors);
        executor.setMaxPoolSize(initProcessors * 5);//最大线程数量
        executor.setQueueCapacity(initProcessors * 100);//线程池的队列容量
        executor.setRejectedExecutionHandler((Runnable r, ThreadPoolExecutor exe) -> {
            StringBuffer msg = new StringBuffer();
            msg.append("当前任务线程池队列已满：").append(executor.getActiveCount()).append("/").append(executor.getCorePoolSize()).append("~").append(executor.getMaxPoolSize());
            logger.error(msg.toString());
            MessageUtils.sendToUserMessage(User.SUPERUSER_ID,msg.toString());
        });
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            StringBuffer msg = new StringBuffer();
            msg.append("线程池执行任务发生未知异常：").append(method.getDeclaringClass().getName()).append(".").append(method.getName()).append(",").append(throwable.getMessage());
            logger.error(msg.toString(),throwable);
            MessageUtils.sendToUserMessage(User.SUPERUSER_ID,msg.toString());
        };
    }

}
