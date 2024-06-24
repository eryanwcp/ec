package com.eryansky.encrypt.handler;

import com.eryansky.encrypt.enums.Scenario;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * The type Scenario post processor.
 * 场景调度器 后置处理器
 * @author : 尔演@Eryan
 *
 */
public class ScenarioPostProcessor implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        StorageScenario storageScenario = applicationContext.getBean(StorageScenario.class);
        TransmitScenario transmitScenario = applicationContext.getBean(TransmitScenario.class);
        ScenarioHolder.abstractScenarios.put(Scenario.storage,storageScenario);
        ScenarioHolder.abstractScenarios.put(Scenario.transmit,transmitScenario);
    }
}
