/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.aop;

import com.eryansky.client.common.vo.ExtendAttr;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.Exceptions;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.UserAgentUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.jwt.JWTUtils;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys.event.SysLogEvent;
import com.eryansky.modules.sys.mapper.Log;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.utils.SpringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 使用AspectJ实现登录登出日志AOP
 *
 * @author Eryan
 */
@Order(1)
@Component
@Aspect
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
        Long start = System.currentTimeMillis();
        log.setStartTime(start);
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Object[] args = joinPoint.getArgs();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        HttpServletRequest request = null;
        try {
            request = SpringMVCHolder.getRequest();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        // 执行方法所消耗的时间
        try {
            log.setType(logging.logType().getValue());

            log.setModule(className + "-" + methodName);
            log.setIp(null != request ? IpUtils.getIpAddr0(request) : (null != sessionInfo ? sessionInfo.getIp():StringUtils.EMPTY));
            log.setTitle(SpringUtils.parseSpel(logging.value(), method, args));
            log.setAction(null != request ? request.getMethod():StringUtils.EMPTY);
            ExtendAttr extendAttr = new ExtendAttr();
            if(null != sessionInfo){
                log.setUserAgent(sessionInfo.getUserAgent());
                log.setDeviceType(sessionInfo.getDeviceType());
                log.setBrowserType(sessionInfo.getBrowserType());
                log.setUserId(sessionInfo.getUserId());
                extendAttr.put("userType",sessionInfo.getUserType());
                extendAttr.put("userName",sessionInfo.getName());
                extendAttr.put("userLoginName",sessionInfo.getLoginName());
                extendAttr.put("userMobile",sessionInfo.getMobile());
            }else {
                String userLoginName = null;
                if(null != request){
                    log.setUserAgent(UserAgentUtils.getHTTPUserAgent(request));
                    log.setDeviceType(UserAgentUtils.getDeviceType(request).toString());
                    log.setBrowserType(UserAgentUtils.getBrowser(request).getName());
                    Map<String, List<String>> headers = WebUtils.getHeaders(request);
                    userLoginName = Collections3.getFirst(headers.get("appCode"));
                    if(StringUtils.isBlank(userLoginName)){
                        userLoginName = request.getParameter("appCode");
                    }
                    if(StringUtils.isBlank(userLoginName)){
                        String access_token = Collections3.getFirst(headers.get("access_token"));;
                        if(StringUtils.isNotBlank(access_token)){
                            try {
                                userLoginName = JWTUtils.getUsername(access_token);
                            } catch (Exception e) {
                                logger.error(e.getMessage());
                            }
                        }

                    }


                }
                extendAttr.put("userType","S");//自定义 系统
                log.setUserId(StringUtils.isNotBlank(userLoginName) ? userLoginName:User.SUPERUSER_ID);
                extendAttr.put("userName",StringUtils.isNotBlank(userLoginName) ? userLoginName:"系统");
                extendAttr.put("userLoginName", userLoginName);
            }

            if(StringUtils.isNotBlank(logging.data())){
                extendAttr.put("requestData",SpringUtils.parseSpel(logging.data(), method, args));
            }else{
                extendAttr.put("requestData", null != request ? JsonMapper.toJsonString(request.getParameterMap()):null);
            }

            if(logging.requestHeaders() && null != request){
                extendAttr.put("requestHeaders",JsonMapper.toJsonString(WebUtils.getHeaders(request)));
            }

            log.setExtendAttr(extendAttr);
            if(StringUtils.isNotBlank(logging.remark())){
                log.setRemark(SpringUtils.parseSpel(logging.remark(), method, args));
            }
            log.setOperTime(Calendar.getInstance().getTime());
            //将当前实体保存到threadLocal
            sysLogThreadLocal.set(log);
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
        if(null == log){
            return;
        }
        long end = System.currentTimeMillis();
        long opTime = end - log.getStartTime();
        log.setActionTime(String.valueOf(opTime));
        log.setEndTime(end);
        log.prePersist();
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
        if(null == log){
            return;
        }
        long end = System.currentTimeMillis();
        long opTime = end - log.getStartTime();
        log.setActionTime(String.valueOf(opTime));
        // 异常
        log.setType(LogType.exception.getValue());
        log.setException(Exceptions.getStackTraceAsString(new Exception(e)));
        log.prePersist();
        // 发布事件
        SpringContextHolder.publishEvent(new SysLogEvent(log));
        //移除当前log实体
        sysLogThreadLocal.remove();
    }


}
