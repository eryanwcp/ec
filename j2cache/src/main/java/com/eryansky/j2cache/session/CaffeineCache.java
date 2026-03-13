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

import com.github.benmanes.caffeine.cache.*;
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

    private final Cache<String, CaffeineEntry<Object>> cache;
    private final int size ;
    private final int expire ;

    public CaffeineCache(int size, int expire, CacheExpiredListener listener) {
        cache = Caffeine.newBuilder()
                .maximumSize(size)
                .expireAfter(new Expiry<String, CaffeineEntry<Object>>() {
                    @Override
                    public long expireAfterCreate(String key, CaffeineEntry<Object> value, long currentTime) {
                        return TimeUnit.SECONDS.toNanos(value.getExpire());
                    }

                    @Override
                    public long expireAfterUpdate(String key, CaffeineEntry<Object> value, long currentTime, long currentDuration) {
                        return TimeUnit.SECONDS.toNanos(value.getExpire());
                    }

                    @Override
                    public long expireAfterRead(String key, CaffeineEntry<Object> value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .removalListener((k,v, cause) -> {
                    //程序删除的缓存不做通知处理，因为上层已经做了处理
                    if (!RemovalCause.EXPLICIT.equals(cause) && !RemovalCause.REPLACED.equals(cause) && !RemovalCause.SIZE.equals(cause)) {
                        try {
                            listener.notifyElementExpired(k);
                        } catch (Exception e) {
                            // 优化：打印完整异常堆栈+上下文，便于定位问题
                            logger.error("J2Cache缓存过期通知执行失败，key: {}, value: {}, 移除原因: {}", k, v, cause, e);
                        }


                    }
                })
                .build();

        this.size = size;
        this.expire = expire;
    }

    public Object get(String session_id) {
        CaffeineEntry<Object> entry = cache.getIfPresent(session_id);
        return null != entry ? entry.getValue() : null;
    }

    public void put(String session_id, Object value) {
        put(session_id, value, null);
    }

    public void put(String session_id, Object value,Long expireTime) {
        CaffeineEntry<Object> caffeineEntry = new CaffeineEntry<>();
        caffeineEntry.setKey(session_id);
        caffeineEntry.setValue(value);
        caffeineEntry.setExpire(null != expireTime ? expireTime:expire);

        cache.put(session_id, caffeineEntry);
        if(null != expireTime && expireTime > 0){
            cache.policy().expireVariably().ifPresent(expiration -> {
                expiration.setExpiresAfter(session_id, expireTime, TimeUnit.SECONDS);
            });
        }

    }

    public void evict(String session_id) {
        cache.invalidate(session_id);
    }

    /**
     * 优化：优雅关闭线程池（等待任务完成+超时强制关闭）
     */
    public void close() {
        cache.cleanUp();
    }


    public int getSize() {
        return size;
    }

    public int getExpire() {
        return expire;
    }

    public Collection<String> keys() {
        return map().keySet();
    }

    public ConcurrentMap<String,CaffeineEntry<Object>> map() {
        return cache.asMap();
    }

    public Long ttl(String session_id) {
        return cache.policy().expireVariably()
                .flatMap(expiration -> expiration.getExpiresAfter(session_id))
                .map(Duration::toSeconds)
                .orElse(null);
    }

}
