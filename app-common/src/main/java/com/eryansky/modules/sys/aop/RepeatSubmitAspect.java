package com.eryansky.modules.sys.aop;

import com.eryansky.common.model.Result;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.core.aop.annotation.NoRepeatSubmit;
import com.eryansky.j2cache.CacheChannel;
import com.eryansky.utils.CacheUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;

/**
 * 对标记了@NoRepeatSubmit的方法进行拦截 防止重复提交
 *
 * @author Eryan
 * @date : 2021-11-05
 */
@Order(1)
@Aspect
@Component
public class RepeatSubmitAspect {

    private static final Logger logger = LoggerFactory.getLogger(RepeatSubmitAspect.class);

    @Pointcut("@annotation(noRepeatSubmit)")
    public void pointCut(NoRepeatSubmit noRepeatSubmit) {
    }

    /**
     * 在业务方法执行前，获取当前用户的
     * token（或者JSessionId）+ 当前请求地址，作为一个唯一 KEY，
     * 去获取 Redis 分布式锁（如果此时并发获取，只有一个线程会成功获取锁。）
     */

    @Around("pointCut(noRepeatSubmit)")
    public Object around(ProceedingJoinPoint pjp, NoRepeatSubmit noRepeatSubmit) throws Throwable {
        HttpServletRequest request = SpringMVCHolder.getRequest();
        Assert.notNull(request, "request can not null");

        // 此处可以用token或者JSessionId
        String token = request.getSession().getId();
        String path = request.getServletPath();
        String key = getKey(token, path);
        CacheChannel cacheChannel = CacheUtils.getCacheChannel();
        boolean isSuccess = cacheChannel.tryLock(noRepeatSubmit.region(), key);
        // 主要逻辑
        if (isSuccess) {
            // 获取锁成功
            Object result;
            try {
                // 执行进程
                result = pjp.proceed();
                cacheChannel.set(noRepeatSubmit.region(), key, true);
            } finally {

            }
            return result;
        } else {
            // 获取锁失败，认为是重复提交的请求。
            Result result = Result.errorResult().setMsg("操作过于频繁，请稍后再试！").setData(key);
            logger.warn(result.toString());
            return result;
        }
    }

    /**
     * token（或者JSessionId）+ 当前请求地址，作为一个唯一KEY
     *
     * @param token
     * @param path
     * @return
     */
    private String getKey(String token, String path) {
        return token + ":" + path;
    }

}
