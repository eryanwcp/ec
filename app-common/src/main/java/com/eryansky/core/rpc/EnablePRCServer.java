package com.eryansky.core.rpc;

import com.eryansky.encrypt.config.EncryptImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


/**
 * RPC服务开启 默认值true 为false 则关闭
 *
 * @author : 尔演@Eryan
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RPCImportSelector.class)
public @interface EnablePRCServer {}