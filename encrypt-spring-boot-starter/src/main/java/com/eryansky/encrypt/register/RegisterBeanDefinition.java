package com.eryansky.encrypt.register;

import com.eryansky.encrypt.enums.CipherMode;
import com.eryansky.encrypt.generator.GeneratorSecretKey;
import cn.hutool.crypto.asymmetric.AsymmetricAlgorithm;
import cn.hutool.crypto.asymmetric.RSA;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.lang.NonNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * The type Register bean definition.
 * BeanDefinition 后置处理
 * @author : 尔演@Eryan
 *
 */
public class RegisterBeanDefinition implements BeanFactoryPostProcessor{
    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        //密钥工厂 信息注册
        BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry)beanFactory;
        GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
        genericBeanDefinition.setBeanClass(GeneratorSecretKeyFactory.class);
        genericBeanDefinition.setAutowireCandidate(true);
        genericBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON); //单例模式
        beanDefinitionRegistry.registerBeanDefinition("generatorSecretKeyProxy",genericBeanDefinition);
    }


    /**
     * The type Generator secret key factory.
     */
//生产密钥工厂 generatorSecretKeyProxy
    public static class GeneratorSecretKeyFactory implements FactoryBean<GeneratorSecretKey>,InvocationHandler {

        @Override
        public GeneratorSecretKey getObject() {
            return  (GeneratorSecretKey)Proxy.newProxyInstance(GeneratorSecretKeyFactory.class.getClassLoader(),new Class[]{GeneratorSecretKey.class}, this);
        }

        @Override
        public Class<?> getObjectType() {
            return GeneratorSecretKey.class;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch ((CipherMode)args[0]){
                case AES:
                    Map<String,String> aesKey = new HashMap<>();
                    aesKey.put("AES_KEY",UUID.randomUUID().toString().replace("-", ""));
                    StringBuilder stringBuffer = new StringBuilder(UUID.randomUUID().toString().replace("-",""));
                    String substring = stringBuffer.substring(0,16);
                    aesKey.put("AES_IV",substring);
                    return aesKey;
                case RSA:
                    RSA rsa = new RSA(AsymmetricAlgorithm.RSA.toString());
                    Map<String,String> rsaKeyMap = new HashMap<>();
                    rsaKeyMap.put("RSA_PrivateKey",rsa.getPrivateKeyBase64());
                    rsaKeyMap.put("RSA_PublicKey",rsa.getPrivateKeyBase64());
                    return rsaKeyMap;
                case SM4:
                    String SM4KEY = UUID.randomUUID().toString().replace("-", "");
                    String SM4IV = UUID.randomUUID().toString().replace("-", "").substring(0,16);
                    Map<String,Object> sm4Map = new HashMap<>();
                    sm4Map.put("SM4_KEY",SM4KEY);
                    sm4Map.put("SM4_IV",SM4IV);
                    return sm4Map;
                default: throw new RuntimeException("com/eryansky/encrypt/register");
            }
        }
    }

}
