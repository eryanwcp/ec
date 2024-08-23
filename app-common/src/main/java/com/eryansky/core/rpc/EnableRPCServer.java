package com.eryansky.core.rpc;

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
@Import(RPCProviderImportSelector.class)
public @interface EnableRPCServer {}