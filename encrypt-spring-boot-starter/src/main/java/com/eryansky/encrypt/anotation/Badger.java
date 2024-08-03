package com.eryansky.encrypt.anotation;

import com.eryansky.encrypt.enums.CipherMode;

import java.lang.annotation.*;

/**
 * 字段标识 獾是一种小型哺乳动物 无所畏惧 取该名的来源
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Badger {
    /**
     * 默认加密方式 AES算法加密 DEFAULT则会根据@Encrypt 或者@Decrypt cipher 的值
     *
     * @return {@link  CipherMode}
     */
    CipherMode cipher() default CipherMode.DEFAULT;
}
