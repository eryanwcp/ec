/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.aop;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.Exceptions;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys.event.SysLogEvent;
import com.eryansky.modules.sys.mapper.Log;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Objects;

/**
 * 使用AspectJ实现登录登出日志AOP
 *
 * @author Eryan
 */
//@Component
//@Aspect
public class SysLogAspect {

    private static Logger logger = LoggerFactory.getLogger(SysLogAspect.class);

    private ThreadLocal<Log> sysLogThreadLocal = new ThreadLocal<>();

    /***
     * 定义controller切入点拦截规则，拦截SysLog注解的方法
     */
    @Pointcut("@annotation(com.eryansky.core.aop.annotation.Logging)")
    public void sysLogAspect() {

    }

    /***
     * 拦截控制层的操作日志
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Before(value = "sysLogAspect()&&@annotation(logging)")
    public void recordLog(JoinPoint joinPoint, Logging logging) throws Throwable {
        Log log = new Log();
        //将当前实体保存到threadLocal
        sysLogThreadLocal.set(log);

        Long start = System.currentTimeMillis();
        log.setStartTime(start);
        // 执行方法名
        String methodName = null;
        String className = null;
        if (joinPoint != null) {
            methodName = joinPoint.getSignature().getName();
            className = joinPoint.getTarget().getClass().getSimpleName();
        }
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        HttpServletRequest request = null;
        try {
            request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        // 执行方法所消耗的时间
        try {

            log.setType(logging.logType().getValue());
            log.setUserId(null != sessionInfo ? sessionInfo.getUserId() : User.SUPERUSER_ID);
            log.setModule(className + "-" + methodName);
            log.setIp(null != sessionInfo ? sessionInfo.getIp() : null);
            log.setTitle(logging.value());
            log.setAction(null != request ? request.getMethod() : StringUtils.EMPTY);
            log.setUserAgent(null != sessionInfo ? sessionInfo.getUserAgent() : StringUtils.EMPTY);
            log.setDeviceType(null != sessionInfo ? sessionInfo.getDeviceType() : StringUtils.EMPTY);
            log.setBrowserType(null != sessionInfo ? sessionInfo.getBrowserType() : StringUtils.EMPTY);
            log.setOperTime(Calendar.getInstance().getTime());
            log.setRemark(logging.remark());
            SpringContextHolder.publishEvent(new SysLogEvent(log));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

    /**
     * 返回通知
     *
     * @param ret
     */
    @AfterReturning(returning = "ret", pointcut = "sysLogAspect()")
    public void doAfterReturning(Object ret) {
        //得到当前线程的log对象
        Log log = sysLogThreadLocal.get();
        long end = System.currentTimeMillis();
        long opTime = end - log.getStartTime();
        log.setActionTime(String.valueOf(opTime));

        // 发布事件
        SpringContextHolder.publishEvent(new SysLogEvent(log));
        //移除当前log实体
        sysLogThreadLocal.remove();
    }

    /**
     * 异常通知
     *
     * @param e
     */
    @AfterThrowing(pointcut = "sysLogAspect()", throwing = "e")
    public void doAfterThrowable(Throwable e) {
        Log log = sysLogThreadLocal.get();
        long end = System.currentTimeMillis();
        long opTime = end - log.getStartTime();
        log.setActionTime(String.valueOf(opTime));
        // 异常
        log.setType(LogType.exception.getValue());
        log.setException(Exceptions.getStackTraceAsString(new Exception(e)));
        // 发布事件
        SpringContextHolder.publishEvent(new SysLogEvent(log));
        //移除当前log实体
        sysLogThreadLocal.remove();
    }


}
