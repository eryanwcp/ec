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

import com.eryansky.j2cache.util.SerializationUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

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

    public CaffeineCache(int size, int expire, CacheExpiredListener listener) {
        cache = Caffeine.newBuilder()
                .maximumSize(size)
                .expireAfterAccess(expire, TimeUnit.SECONDS)
                .removalListener((k,v, cause) -> {
                    //程序删除的缓存不做通知处理，因为上层已经做了处理
                    if (!RemovalCause.EXPLICIT.equals(cause) && !RemovalCause.REPLACED.equals(cause) && !RemovalCause.SIZE.equals(cause)) {
                        try {
                            synchronized (CaffeineCache.class){
                                listener.notifyElementExpired((String) k);
                            }
                        } catch (Exception e) {
                            logger.error("{}:{} {}",k, v, cause);
                            logger.error(e.getMessage(),e);
                        }
                    }
                })
                .build();

        this.size = size;
        this.expire = expire;
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

    public void close() {
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
}
