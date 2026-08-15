package com.eryansky.modules.sys.task;

import com.eryansky.modules.sys.event.SysLogEvent;
import com.eryansky.modules.sys.mapper.Log;
import com.eryansky.modules.sys.service.LogService;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 日志监听器 - 批量异步写入优化版
 * @author Eryan
 */
@Component
public class SysLogListener implements ApplicationListener<SysLogEvent> {

    private static final Logger log = LoggerFactory.getLogger(SysLogListener.class);

    @Resource
    private LogService logService;

    // 内存缓冲队列，容量限制避免内存溢出 (OOM)
    private final BlockingQueue<Log> logQueue = new LinkedBlockingQueue<>(10000);

    // 单次批量插入的容量阈值
    private static final int BATCH_SIZE = 100;

    // 定时刷新到数据库的时间间隔（毫秒）
    private static final long FLUSH_INTERVAL_MS = 1000;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "sys-log-consumer");
        thread.setDaemon(true);
        return thread;
    });

    @PostConstruct
    public void init() {
        // 启动后台定时消费任务
        executorService.scheduleWithFixedDelay(this::consumeLogs, FLUSH_INTERVAL_MS, FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onApplicationEvent(SysLogEvent event) {
        Log sysLog = (Log) event.getSource();
        if (sysLog != null) {
            // offer 非阻塞入队，队列满了打日志丢弃或采取降级策略，避免阻塞业务线程
            if (!logQueue.offer(sysLog)) {
                log.warn("SysLog queue is full, log discarded: {}", sysLog);
            }
        }
    }

    private void consumeLogs() {
        try {
            List<Log> logList = new ArrayList<>(BATCH_SIZE);
            // 循环批量提取队列中的日志
            while (logQueue.drainTo(logList, BATCH_SIZE) > 0) {
                saveBatchLogs(logList);
                logList.clear();
            }
        } catch (Exception e) {
            log.error("Error occurred while consuming log queue", e);
        }
    }

    private void saveBatchLogs(List<Log> logList) {
        if (logList.isEmpty()) {
            return;
        }
        try {
            // 需要在 LogService 中提供 insertBatch/saveBatch 方法
            logService.insertBatch(logList);
        } catch (Exception e) {
            log.error("Failed to batch insert log records", e);
        }
    }

    @PreDestroy
    public void destroy() {
        // 容器销毁前将队列中剩余日志全部刷入数据库，防止数据丢失
        executorService.shutdown();
        consumeLogs();
    }
}