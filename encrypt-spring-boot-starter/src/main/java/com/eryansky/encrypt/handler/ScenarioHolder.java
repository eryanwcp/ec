package com.eryansky.encrypt.handler;

import com.eryansky.encrypt.anotation.Decrypt;
import com.eryansky.encrypt.anotation.Encrypt;
import com.eryansky.encrypt.enums.CipherMode;
import com.eryansky.encrypt.enums.Scenario;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * The interface Scenario holder.
 * 场景
 * @author : 尔演@Eryan
 *
 */
public interface ScenarioHolder {
    /**
     * The constant abstractScenarios.
     */
//存储场景
     ConcurrentHashMap<Scenario, ScenarioHandler> abstractScenarios = new ConcurrentHashMap<>();
    /**
     * The constant scenarioSchedules.
     */
    ArrayList<ScenarioSchedule> scenarioSchedules = new ArrayList<>();  //存储所有的调度器

    /**
     * 字段对应的算法
     */
    ConcurrentHashMap<String, CipherMode> fieldNames = new ConcurrentHashMap<>();

    /**
     * 记录字段对应的算法
     * @param fieldName 字段名
     * @param cipherMode 算法
     */
    static void addField(String fieldName,CipherMode cipherMode){
        fieldNames.put(fieldName,cipherMode);
    }
    /**
     * 记录字段对应的算法
     * @return  the CipherMode
     */
    static String getFieldCipherModes(){
        ConcurrentHashMap.KeySetView<String, CipherMode> keySetView = fieldNames.keySet();
        StringBuilder stringBuilder = new StringBuilder();
        for (String filedName : keySetView) {
            stringBuilder.append(filedName)
                         .append(":").append(fieldNames.get(filedName))
                         .append("-");
        }
        String newStr = "";
        if (stringBuilder.length() > 0){
            int lastIndexOf = stringBuilder.lastIndexOf("-");
            newStr = stringBuilder.substring(0,lastIndexOf);
        }
        try {
            return newStr;
        }finally {
            fieldNames.clear();
        }
    }
    /**
     * Gets scenarios.
     *
     * @param scenario the scenario
     * @return the scenarios
     */
    static ScenarioHandler getScenarios(Scenario scenario) {
        return abstractScenarios.get(scenario);
     }

    /**
     * Scenario schedule object.
     *
     * @param joinPoint the join point
     * @return the object
     * @throws Throwable the throwable
     */
    static Object scenarioSchedule(ProceedingJoinPoint joinPoint) throws Throwable {
         ScenarioSchedule scenarioSchedule = scenarioSchedules.get(0);
         Assert.notNull(scenarioSchedule,"调度器未加载至容器");
         return scenarioSchedule.scenarioSchedule(joinPoint);
     }

    /**
     * Get executor executor.
     *
     * @return the executor
     */
    static Executor getExecutor(){
         return ScenarioHandler.executor;
    }

    /**
     * 加密链路 存储  {@link Encrypt}
     *
     * @param args      参数
     * @param signature 方法源信息
     * @param encrypt   加密实例
     * @throws Throwable 异常
     */
    void storageEncryptProcessor(Object[] args, MethodSignature signature, Encrypt encrypt) throws Throwable;

    /**
     * 解密链路 存储  {@link Decrypt}
     *
     * @param process   目标方法执行结果
     * @param signature 方法源信息
     * @param decrypt   解密注解源信息
     * @throws Throwable 异常
     */
    void storageDecryptProcessor(Object process, MethodSignature signature, Decrypt decrypt) throws Throwable;

    /**
     * 加密链路 网络传输 {@link Encrypt}
     *
     * @param process   参数
     * @param signature 方法源信息
     * @param encrypt   加密实例
     * @throws Throwable 异常
     */
    void transmitEncryptProcessor(Object process, MethodSignature signature, Encrypt encrypt) throws Throwable;

    /**
     * 解密链路 网络传输 {@link Decrypt}
     *
     * @param args      目标方法执行结果
     * @param signature 方法源信息
     * @param decrypt   解密注解源信息
     * @throws Throwable 异常
     */
    void transmitDecryptProcessor(Object[] args, MethodSignature signature, Decrypt decrypt) throws Throwable;


}
