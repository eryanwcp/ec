package com.eryansky.encrypt.config;

import com.eryansky.encrypt.advice.DecryptRequestBodyAdvice;
import com.eryansky.encrypt.advice.EncryptRResponseBodyAdvice;
import com.eryansky.encrypt.advice.EncryptResultResponseBodyAdvice;
import com.eryansky.encrypt.aspectj.EncryptHandler;
import com.eryansky.encrypt.badger.HoneyBadgerEncrypt;
import com.eryansky.encrypt.register.RegisterBeanDefinition;
import com.eryansky.encrypt.spel.SpELExpressionHandler;
import com.eryansky.encrypt.handler.*;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The type Encrypt auto configuration.
 * 加密容器
 *
 * @author : 尔演@Eryan
 * @date : 2022-07-23 16:22
 */
//导入选择
public class EncryptAutoConfiguration {

    /**
     * Encrypt handler encrypt handler.
     *
     * @return 加密处理器 encrypt handler
     */
    @Bean
    @ConditionalOnBean(value = {EncryptProvider.class})
    public EncryptHandler encryptHandler(HoneyBadgerEncrypt honeyBadgerEncrypt){
        return new EncryptHandler(honeyBadgerEncrypt);
    }

    /**
     * 场景调度器
     *
     * @return the scenario schedule
     */
    @Bean
    @ConditionalOnBean(EncryptHandler.class)
    public ScenarioSchedule scenarioEncryptSchedule(){
        ScenarioSchedule scenarioEncryptSchedule = new ScenarioEncryptSchedule();
        ScenarioHolder.scenarioSchedules.add(scenarioEncryptSchedule);
        return scenarioEncryptSchedule;
    }

    /**
     * 加密存储场景
     *
     * @return the storage scenario
     */
    @Bean
    @ConditionalOnMissingBean(StorageScenario.class)
    public StorageScenario storageScenario(){
        return new StorageScenario();
    }

    /**
     * 加密传输场景
     *
     * @return the transmit scenario
     */
    @Bean
    @ConditionalOnMissingBean(TransmitScenario.class)
    public TransmitScenario transmitScenario(){
        return new TransmitScenario();
    }

    /**
     * 自动装配场景
     *
     * @return the scenario post processor
     */
    @Bean
    @ConditionalOnBean(ScenarioHandler.class)
    public ScenarioPostProcessor scenarioPostProcessor(){
        return new ScenarioPostProcessor();
    }

    /**
     * 生产密钥工厂
     *
     * @return the register bean definition
     */
    @Bean
    public RegisterBeanDefinition beanFactoryPostProcessor(){
        return new RegisterBeanDefinition();
    }

    /**
     * 加解密工具类
     *
     * @return the honey badger encrypt
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @ConditionalOnBean(EncryptHandler.class)
    public HoneyBadgerEncrypt getHoneyBadgerEncrypt(){
        HoneyBadgerEncrypt honeyBadgerEncrypt = new HoneyBadgerEncrypt();
        ScenarioHandler.honeyBadgerEncrypt = honeyBadgerEncrypt;
        return honeyBadgerEncrypt;
    }

    /**
     * 线程池后置处理器
     *
     * @return the executor post processor
     */
    @Bean("executorPostProcessor")
    @ConditionalOnBean(name = "encryptThreadPoolExecutor")
    public ExecutorPostProcessor executorPostProcessor(){
        return new ExecutorPostProcessor();
    }


    /**
     * Sp el expression handler sp el expression handler.
     *
     * @param spelExpressionParser the spel expression parser
     * @return the sp el expression handler
     */
//spel表达式 解析处理
    @Bean("encrypt-SpELExpressionHandler")
    @ConditionalOnBean(value = {EncryptHandler.class})
    public SpELExpressionHandler spELExpressionHandler(SpelExpressionParser spelExpressionParser){
        SpELExpressionHandler spELExpressionHandler = new SpELExpressionHandler();
        spELExpressionHandler.setSpelExpressionParser(spelExpressionParser);
        return spELExpressionHandler;
    }

    /**
     * Spel expression parser spel expression parser.
     *
     * @return the spel expression parser
     */
    @Bean("encrypt-SpelExpressionParser")
    @ConditionalOnBean(name = "encrypt-SpELExpressionHandler")
    public SpelExpressionParser spelExpressionParser(){
        return new SpelExpressionParser();
    }

    @Bean
    public EncryptResultResponseBodyAdvice encryptResultResponseBodyAdvice() {
        return new EncryptResultResponseBodyAdvice();
    }

    @Bean
    public EncryptRResponseBodyAdvice encryptRResponseBodyAdvice() {
        return new EncryptRResponseBodyAdvice();
    }


    @Bean
    public DecryptRequestBodyAdvice decryptRequestBodyAdvice() {
        return new DecryptRequestBodyAdvice();
    }

}
