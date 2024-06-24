package com.eryansky.encrypt.anotation;

import com.eryansky.encrypt.enums.CipherMode;
import com.eryansky.encrypt.enums.Scenario;

import java.lang.annotation.*;

/**
 * 加密注解  满足多种场景需求 网络接传输、加密存储、远程调用加密
 * @author : 尔演@Eryan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Decrypt {
    /**
     * 应用场景 网络传输、或者 持久化
     *
     * @return {@link  Scenario}
     */
    Scenario scenario() default Scenario.storage;

    /**
     * 默认加密方式 AES算法加密
     *
     * @return {@link  CipherMode}
     */
    CipherMode cipher() default CipherMode.AES;

    /**
     * 区分字段大小写 默认是不区分
     *
     * @return false boolean
     */
    boolean caseSensitive() default false;

    /**
     * 加密的字段名方法加密需要指定字段名称 默认是对data字段解密
     *
     * @return the string [ ]
     */
    String[] fields() default {""};

    /**
     * SpEL表达式   对SpEL表达式的支持
     * * @beanName.method  or @beanName.field  the field not be -> private decorated
     * * @ss.abc()  @ss.name
     *
     * @return the string
     */
    String value() default "";
    /**
     * 动态密钥 可变的密钥  支持混合算法 sm4-rsa aes-rsa {@CipherMode}
     * @return the boolean
     */
    boolean dynamic() default false;
}
