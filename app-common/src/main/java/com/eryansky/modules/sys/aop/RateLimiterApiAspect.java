package com.eryansky.modules.sys.aop;

import com.eryansky.common.model.Result;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.core.aop.annotation.RateLimiterApi;
import com.eryansky.j2cache.CacheChannel;
import com.eryansky.j2cache.CacheObject;
import com.eryansky.utils.CacheUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;

/**
 * API访问频率限制切面
 *
 * @author Eryan
 * @date : 2021-11-05
 */
@Component
@Order(1)
@Aspect
public class RateLimiterApiAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterApiAspect.class);

    @Pointcut("@annotation(rateLimiterApi)")
    public void limit(RateLimiterApi rateLimiterApi) {
    }

    @Around("limit(rateLimiterApi)")
    public Object aroundLog(ProceedingJoinPoint joinpoint, RateLimiterApi rateLimiterApi) throws Throwable {
        if(!Boolean.parseBoolean(rateLimiterApi.enable())){
            return joinpoint.proceed();
        }
        HttpServletRequest request = SpringMVCHolder.getRequest();
        Assert.notNull(request, "request can not null");

        // 此处可以用token或者JSessionId
        String token = request.getSession().getId();
        String path = request.getServletPath();
        StringBuffer key = new StringBuffer();
        key.append(rateLimiterApi.scopeAll() ? "all":token).append("_").append(path);

        MethodSignature methodSignature = (MethodSignature) joinpoint.getSignature();
        int frequency = rateLimiterApi.frequency();
        String paramKey = rateLimiterApi.paramKey();

        if (null != paramKey) {
            //入参
            String[] parameterNames = methodSignature.getParameterNames();
            Object[] args = joinpoint.getArgs();
            Object obj = null;

            for (int i = 0; i < parameterNames.length; i++) {
                if (parameterNames[i].equals(paramKey)) {
                    obj = args[i];
                    break;
                }
            }
            key.append("_").append(paramKey).append("_").append(obj);
        }

        CacheChannel cacheChannel = CacheUtils.getCacheChannel();
        CacheObject cacheObject = cacheChannel.get(rateLimiterApi.region(), key.toString());
        if (null == cacheObject || null == cacheObject.getValue()) {
            cacheChannel.set(rateLimiterApi.region(), key.toString(), frequency - 1);
        } else {
            int l = (int) cacheObject.getValue();
            if (l > 0) {
                cacheChannel.set(rateLimiterApi.region(), key.toString(), --l);
            } else {
                Result result = Result.errorApiResult().setMsg("接口访问频率限制，请稍后再试！").setData(key);
                log.warn(result.toString());
                return result;
            }
        }
        return joinpoint.proceed();
    }


}
