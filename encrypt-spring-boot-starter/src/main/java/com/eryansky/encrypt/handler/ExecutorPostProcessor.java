package com.eryansky.encrypt.handler;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.concurrent.Executor;

/**
 * The type Executor post processor.
 * 线程池后置处理 装配
 * @author : 尔演@Eryan
 *
 */
public class ExecutorPostProcessor implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Object executor = applicationContext.getBean("encrypt-ThreadPoolExecutor");
        if (executor instanceof Executor){
            ScenarioHandler.executor = (Executor) executor;
        }
    }
}
