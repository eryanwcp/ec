/**
 * Copyright (c) 2015-2017, Winter Lau (javayou@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eryansky.j2cache.session;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Policy;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.*;

/**
 * Caffeine cache
 *
 * @author Winter Lau(javayou@gmail.com)
 */
public class CaffeineCache {

    private static final Logger logger = LoggerFactory.getLogger(CaffeineCache.class);

    private final Cache<String, Object> cache;
    private final int size ;
    private final int expire ;

    // 优化：有界固定线程池（核心/最大线程数=CPU核心数*1，有界队列，自定义线程名）
    private ExecutorService removalExecutorService;
    // 线程池核心参数（可根据业务调整）
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    private static final int QUEUE_CAPACITY = 10000; // 任务队列容量，避免无界堆积
    private static final long KEEP_ALIVE_TIME = 60L; // 空闲线程存活时间

    public CaffeineCache(int size, int expire, CacheExpiredListener listener,boolean enableL2) {
        if(enableL2){
            this.removalExecutorService = new ThreadPoolExecutor(
                    CORE_POOL_SIZE,
                    MAX_POOL_SIZE,
                    KEEP_ALIVE_TIME,
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(QUEUE_CAPACITY), // 有界队列，避免OOM
                    new NamedThreadFactory("j2cache-caffeine-cache-expire"), // 自定义线程名
                    new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时降级：提交线程执行，避免任务丢失
            );
        }


        cache = Caffeine.newBuilder()
                .maximumSize(size)
                .expireAfterAccess(expire + 1, TimeUnit.SECONDS)
                .expireAfterWrite(expire, TimeUnit.SECONDS)
                .removalListener((k,v, cause) -> {
                    //程序删除的缓存不做通知处理，因为上层已经做了处理
                    if (!RemovalCause.EXPLICIT.equals(cause) && !RemovalCause.REPLACED.equals(cause) && !RemovalCause.SIZE.equals(cause)) {
                        if(enableL2){
                            removalExecutorService.execute(()->{
                                try {
                                    listener.notifyElementExpired((String) k);
                                } catch (Exception e) {
                                    // 优化：打印完整异常堆栈+上下文，便于定位问题
                                    logger.error("J2Cache缓存过期通知执行失败，key: {}, value: {}, 移除原因: {}", k, v, cause, e);
                                }
                            });
                        }else{
                            listener.notifyElementExpired((String) k);
                        }


                    }
                })
                .build();

        this.size = size;
        this.expire = expire;
    }

    /**
     * 自定义线程工厂（给线程命名，方便排查问题）
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private int threadCount = 0;

        public NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(prefix + "-thread-" + (++threadCount));
            thread.setDaemon(true); // 守护线程，不阻塞应用关闭
            return thread;
        }
    }

    public Object get(String session_id) {
        return cache.getIfPresent(session_id);
    }

    public void put(String session_id, Object value) {
        cache.put(session_id, value);
    }

    public void evict(String session_id) {
        cache.invalidate(session_id);
    }

    /**
     * 优化：优雅关闭线程池（等待任务完成+超时强制关闭）
     */
    public void close() {
        if(null != removalExecutorService){
            removalExecutorService.shutdown(); // 拒绝新任务，等待现有任务执行
            try {
                // 等待30秒，让现有任务执行完成
                if (!removalExecutorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.warn("J2Cache缓存过期线程池关闭超时，强制关闭未完成任务");
                    removalExecutorService.shutdownNow(); // 强制关闭，中断未完成任务
                    // 再次等待10秒，确保线程池关闭
                    if (!removalExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                        logger.error("J2Cache缓存过期线程池强制关闭失败");
                    }
                }
            } catch (InterruptedException e) {
                logger.error("J2Cache缓存过期线程池关闭被中断", e);
                removalExecutorService.shutdownNow();
                // 恢复中断状态，避免上层逻辑异常
                Thread.currentThread().interrupt();
            }
        }

    }


    public int getSize() {
        return size;
    }

    public int getExpire() {
        return expire;
    }

    public Collection<String> keys() {
        return cache.asMap().keySet();
    }

    public ConcurrentMap<String,Object> map() {
        return cache.asMap();
    }

    public Long ttl(String key) {
        Policy.FixedExpiration<String,Object> p = cache.policy().expireAfterWrite().orElse(cache.policy().expireAfterAccess().orElse(null));
        long  total = null == p ? 0:p.getExpiresAfter(TimeUnit.SECONDS);
        Duration d = null == p ? null:p.ageOf(key).orElse(null);
        return null == d ? null:total - d.getSeconds();
    }

}
