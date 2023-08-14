package com.eryansky.modules.sys.aop;

import com.eryansky.common.utils.Identities;
import com.eryansky.core.aop.annotation.QueuePoll;
import com.eryansky.j2cache.CacheChannel;
import com.eryansky.utils.CacheUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * 排队(串行执行)
 */
@Aspect
@Order(1)
@Component
public class QueuePollAspect {

    @Pointcut("@annotation(queuePoll)")
    public void poll(QueuePoll queuePoll) {
    }


    @Around("poll(queuePoll)")
    public Object translateReturning(ProceedingJoinPoint proceedingJoinPoint, QueuePoll queuePoll) throws Throwable {
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        // Thread currentThread = Thread.currentThread();
        // 线程 ID 是唯一的，并且在其生命周期内保持不变。 当一个线程终止时，这个线程 ID 可能会被重用。
        // String threadId = String.valueOf(currentThread.getId());
        // 在多实例多情况下线程ID可能会导致重复，所以使用UUID
        String uuid = Identities.uuid2();
        // 相同的租户放入同一个redis队列里，实现同租户串行不同的租户并行
        String region = queuePoll.region() + "_" + queuePoll.value();
        CacheChannel cacheChannel = CacheUtils.getCacheChannel();
        cacheChannel.queuePush(region, uuid);
        boolean waitFlag = Boolean.TRUE;
        while (waitFlag) {
            waitFlag = Boolean.FALSE;
            List<String> top = (List<String>) cacheChannel.queueList(region);
            if (top != null && top.size() > 0) {
                // redis 里有数据
                if (!uuid.equals(top.get(0))) {
                    // 队列顶部不是该接口，线程等待
                    waitFlag = Boolean.TRUE;
                }
            }
            if (waitFlag) {
                // 根据接口执行平均时长来适度调整休眠时间，休眠时会让出cpu给其他的线程
                Thread.sleep(100);
            }
        }

        Object result = proceedingJoinPoint.proceed();

        // 执行结束，推出队列顶端元素
        cacheChannel.queuePop(region);
        return result;
    }

    @AfterThrowing(value = "poll(queuePoll)", throwing = "e")
    public void throwingAdvice(JoinPoint joinPoint, QueuePoll queuePoll, Exception e) {
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String region = queuePoll.region() + "_" + queuePoll.value();
        // 抛出错误时也要退出队列顶端元素，否则后面的接口就堵死了
        CacheChannel cacheChannel = CacheUtils.getCacheChannel();
        cacheChannel.queuePop(region);
    }
}
