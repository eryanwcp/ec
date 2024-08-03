package com.eryansky.encrypt.handler;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * The type Scenario encrypt schedule.
 * 可以在此处重写你的调度策略
 * @author : 尔演@Eryan
 *
 */
public class ScenarioEncryptSchedule extends ScenarioSchedule {
    @Override
    public Object scenarioSchedule(ProceedingJoinPoint joinPoint) throws Throwable {
       return super.scenarioSchedule(joinPoint);
    }
}
