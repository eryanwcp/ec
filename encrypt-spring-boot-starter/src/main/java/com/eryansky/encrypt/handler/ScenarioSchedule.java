package com.eryansky.encrypt.handler;

import com.eryansky.encrypt.anotation.Decrypt;
import com.eryansky.encrypt.anotation.Encrypt;
import com.eryansky.encrypt.enums.Scenario;
import com.eryansky.encrypt.spel.SpELExpressionHandler;
import com.eryansky.encrypt.spel.SpELParserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * The type Scenario schedule.
 *
 * @author : 尔演@Eryan
 *
 */
public abstract class ScenarioSchedule{
    /**
     * The constant spELExpressionHandler.
     */
    public static SpELExpressionHandler spELExpressionHandler;

    /**
     * 场景选择
     *
     * @param joinPoint the join point
     * @return the object
     * @throws Throwable the throwable
     */
    public Object scenarioSchedule(ProceedingJoinPoint joinPoint) throws Throwable {
       MethodSignature signature = (MethodSignature)joinPoint.getSignature();
       Annotation[] annotations = signature.getMethod().getAnnotations();
       for (Annotation annotation : annotations) {
           if (annotation instanceof Encrypt){
               Encrypt encrypt = (Encrypt) annotation;
               String expression = encrypt.value();
               String SPEL_EXPRESSION = SpELParserContext.SUFFIX + expression + SpELParserContext.PREFIX;
               if (StringUtils.hasText(expression)){
                   String[] fields = spELExpressionHandler.parseSpELExpression(SPEL_EXPRESSION);
                   processorAnnotation(encrypt,fields);
               }
               ScenarioHandler scenarioHandler = ScenarioHolder.getScenarios(encrypt.scenario());
               switch (encrypt.scenario()){
                   case storage:
                       Object[] args = joinPoint.getArgs();
                       StorageScenario scenario = (StorageScenario)scenarioHandler;
                       scenario.storageEncryptProcessor(args,signature,encrypt);
                       break;
                   case transmit:
                       Object proceed = joinPoint.proceed();
                       TransmitScenario transmit = (TransmitScenario)scenarioHandler;
                        transmit.transmitEncryptProcessor(proceed,signature,encrypt);
                       return proceed;
                   default: return "No such algorithm "+encrypt.scenario();
               }
           }
           if (annotation instanceof Decrypt){
               Decrypt decrypt = (Decrypt) annotation;
               String expression = decrypt.value();
               String SPEL_EXPRESSION = SpELParserContext.SUFFIX + expression + SpELParserContext.PREFIX;
               if (StringUtils.hasText(expression)){
                   String[] fields = spELExpressionHandler.parseSpELExpression(SPEL_EXPRESSION);
                   processorAnnotation(decrypt,fields);
               }
               ScenarioHandler scenarioHandler = ScenarioHolder.getScenarios(decrypt.scenario());
               switch (decrypt.scenario()){
                   case storage:
                       Object proceed = joinPoint.proceed();  //执行结果
                       StorageScenario scenario = (StorageScenario)scenarioHandler;
                       scenario.storageDecryptProcessor(proceed,signature,decrypt);
                       return proceed;
                   case transmit:
                       Object[] args = joinPoint.getArgs();   //请求参数
                       TransmitScenario transmit = (TransmitScenario)scenarioHandler;
                       transmit.transmitDecryptProcessor(args,signature,decrypt);
                       break;
                   default: return "No such algorithm "+decrypt.scenario();
               }
           }
       }
        return joinPoint.proceed();
   }

   //处理注解
    @SuppressWarnings({"all"})
   private void processorAnnotation(Annotation annotation,String[] fields) throws NoSuchFieldException, IllegalAccessException {
       InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
       // 获取 AnnotationInvocationHandler 的 memberValues 字段
       Field hField = invocationHandler.getClass().getDeclaredField("memberValues");
       // 这个字段是 private final 修饰，要打开权限
       hField.setAccessible(true);
       // 获取 memberValues
       Map memberValues = (Map) hField.get(invocationHandler);
       // 修改 value 属性值
       memberValues.put("fields", fields);
   }
}
