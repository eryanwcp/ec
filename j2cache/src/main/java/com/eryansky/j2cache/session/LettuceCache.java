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

import com.eryansky.j2cache.CacheException;
import com.google.common.collect.Maps;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.BaseRedisCommands;
import io.lettuce.core.api.sync.RedisHashCommands;
import io.lettuce.core.api.sync.RedisKeyCommands;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import org.apache.commons.pool2.impl.GenericObjectPool;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Redis 缓存操作封装，基于 Hashs 实现多个 Region 的缓存
 * @author Winter Lau(javayou@gmail.com)
 *
 * 重要提示！！！  hash 存储模式无法单独对 key 设置 expire
 */
public class LettuceCache {

    protected String namespace;
    protected AbstractRedisClient client;
    protected GenericObjectPool<StatefulConnection<String, byte[]>> pool;
    protected int scanCount;

    /**
     * 缓存构造
     * @param namespace 命名空间，用于在多个实例中避免 key 的重叠
     * @param client 缓存客户端接口
     */
    public LettuceCache(String namespace, AbstractRedisClient client, GenericObjectPool<StatefulConnection<String, byte[]>> pool, int scanCount) {
        this.client = client;
        this.namespace = namespace;
        this.pool = pool;
        this.scanCount = scanCount;
    }

    protected StatefulConnection connect() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    protected BaseRedisCommands sync(StatefulConnection conn) {
        if(conn instanceof StatefulRedisClusterConnection)
            return ((StatefulRedisClusterConnection)conn).sync();
        else if(conn instanceof StatefulRedisConnection)
            return ((StatefulRedisConnection)conn).sync();
        return null;
    }


    /**
     * 在region里增加一个可选的层级,作为命名空间,使结构更加清晰
     * 同时满足小型应用,多个J2Cache共享一个redis database的场景
     *
     * @param session_id
     * @return
     */
    private String getRegionName(String session_id) {
        if (namespace != null && !namespace.isEmpty())
            session_id = namespace + ":" + session_id;
        return session_id;
    }

    public byte[] getBytes(String session_id, String key) {
        try(StatefulConnection<String, byte[]> connection = connect()) {
            RedisHashCommands<String, byte[]> cmd = (RedisHashCommands)sync(connection);
            return cmd.hget(getRegionName(session_id), key);
        }
    }

    public List<byte[]> getBytes(String session_id, Collection<String> keys) {
        try(StatefulConnection<String, byte[]> connection = connect()) {
            RedisHashCommands<String, byte[]> cmd = (RedisHashCommands)sync(connection);
            return cmd.hmget(getRegionName(session_id), keys.stream().toArray(String[]::new)).stream().map(kv -> kv.hasValue()?kv.getValue():null).collect(Collectors.toList());
        }
    }

    /**
     * 更新某个Key
     * @param session_id
     * @param key
     * @param bytes
     */
    public void updateKeyBytes(String session_id, String key, byte[] bytes) {
        try(StatefulConnection<String, byte[]> connection = connect()) {
            RedisHashCommands<String, byte[]> cmd = (RedisHashCommands)sync(connection);
            Map<String, byte[]> data = cmd.hgetall(getRegionName(session_id));
            if(null == data){
                data = Maps.newHashMap();
            }
            data.put(key,bytes);
            cmd.hmset(getRegionName(session_id),data);
        }
    }



    public void setBytes(String session_id, Map<String,byte[]> bytes, int expireInSeconds) {
        try(StatefulConnection<String, byte[]> connection = connect()) {
            RedisHashCommands<String, byte[]> cmd = (RedisHashCommands)sync(connection);
            cmd.hset(getRegionName(session_id),bytes);
            ttl(session_id,expireInSeconds);
        }
    }

    public void evict(String session_id, String...keys) {
        try(StatefulConnection<String, byte[]> connection = connect()) {
            RedisHashCommands<String, byte[]> cmd = (RedisHashCommands)sync(connection);
            cmd.hdel(getRegionName(session_id), keys);
        }
    }

    public boolean ttl(String session_id, int ttl) {
        try(StatefulConnection<String, byte[]> connection = connect()) {
            RedisKeyCommands<String, byte[]> cmd = (RedisKeyCommands)sync(connection);
            return cmd.expire(getRegionName(session_id),ttl);
        }
    }

    public List<String> keys(String session_id) {
        try(StatefulConnection<String, byte[]> connection = connect()) {
            RedisHashCommands<String, byte[]> cmd = (RedisHashCommands)sync(connection);
            return cmd.hkeys(getRegionName(session_id));
        }
    }

    public void clear(String session_id) {
        try(StatefulConnection<String, byte[]> connection = connect()) {
            RedisKeyCommands<String, byte[]> cmd = (RedisKeyCommands)sync(connection);
            cmd.del(getRegionName(session_id));
        }
    }

    public Collection<String> keys() {
        try(StatefulConnection<String, byte[]> connection = connect()) {
            RedisHashCommands<String, byte[]> cmd = (RedisHashCommands)sync(connection);
            return cmd.hkeys(namespace);
        }

    }

}
