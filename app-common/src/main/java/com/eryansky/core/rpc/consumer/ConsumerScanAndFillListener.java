package com.eryansky.core.rpc.consumer;

import com.eryansky.core.rpc.annotation.RPCConsumer;
import com.eryansky.core.rpc.utils.FieldAnnotationUtils;
import com.eryansky.core.rpc.utils.RPCProxyUtils;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 扫描到@RPCConsumer，并注入动态代理对象
 * 通过@EnableRPCClients 指定
 */
@Deprecated
public class ConsumerScanAndFillListener implements ApplicationListener<WebServerInitializedEvent> {

    /**
     * 标识事件监听器是否已经注册，避免重复注册
     */
    private final AtomicBoolean flag = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        if (flag.compareAndSet(false, true)) {
            WebServerApplicationContext applicationContext = event.getApplicationContext();
            String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
            for (String name : beanDefinitionNames) { // 遍历所有的bean
                Object bean = applicationContext.getBean(name);
                List<FieldAnnotationUtils.FieldAnnotationInfo> fieldAnnotationInfos = FieldAnnotationUtils.parseFieldAnnotationInfo(bean, RPCConsumer.class);
                if (!CollectionUtils.isEmpty(fieldAnnotationInfos)) { // 判断bean的字段上是否存在@CustomRpcConsumer注解
                    for (FieldAnnotationUtils.FieldAnnotationInfo fieldAnnotationInfo : fieldAnnotationInfos) {
                        Field field = fieldAnnotationInfo.getField();
                        boolean accessFlag = field.isAccessible();
                        if (!accessFlag) {
                            field.setAccessible(true);
                        }

                        Class<?> type = field.getType();
                        // 生成代理对象,将代理对象注入到当前bean对象中
                        Object proxyObj = RPCProxyUtils.createProxyObj("",type);
                        try {
                            field.set(fieldAnnotationInfo.getObj(), proxyObj);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }

                        field.setAccessible(accessFlag);
                    }
                }
            }

        }
    }
}
