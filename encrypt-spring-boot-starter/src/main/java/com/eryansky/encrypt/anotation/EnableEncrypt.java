package com.eryansky.encrypt.anotation;

import com.eryansky.encrypt.config.EncryptImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启加密 默认值true 为false 则关闭
 *
 * @author Eryan
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EncryptImportSelector.class)
public @interface EnableEncrypt {}
