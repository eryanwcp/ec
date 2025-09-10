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

import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.j2cache.lettuce.LettuceByteCodec;
import com.eryansky.j2cache.util.IpUtils;
import com.eryansky.j2cache.util.SerializationUtils;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存封装入口
 * @author Winter Lau(javayou@gmail.com)
 */
public class CacheFacade extends RedisPubSubAdapter<String, String> implements Closeable, CacheExpiredListener {

    private final Logger logger = LoggerFactory.getLogger(CacheFacade.class);

    private static final Map<String, Object> _g_keyLocks = new ConcurrentHashMap<>();

    private final CaffeineCache cache1;
    private LettuceCache cache2;

    private AbstractRedisClient redisClient;
    private String pubsub_channel;
    private GenericObjectPool<StatefulConnection<String, byte[]>> pool;
    private StatefulRedisPubSubConnection<String, String> pubsub_subscriber;
//    private RedisPubSubCommands<String, String> pubSubCommands;
    private StatefulRedisPubSubConnection<String, String> pubConnection;
    private static final LettuceByteCodec codec = new LettuceByteCodec();

    private final boolean discardNonSerializable;

    public CacheFacade(int maxSizeInMemory, int maxAge, Properties redisConf, boolean discardNonSerializable)  {
        this.discardNonSerializable = discardNonSerializable;
        this.cache1 = new CaffeineCache(maxSizeInMemory, maxAge, this);

        logger.info("J2Cache Session L1 CacheProvider {}.",this.cache1.getClass().getName());

        String enabled = redisConf.getProperty("enabled");
        if(!"true".equalsIgnoreCase(enabled)){
            logger.info("J2Cache Session L2/redis not enabled.");
            return;
        }

        String hosts = redisConf.getProperty("hosts","127.0.0.1:6379");

        int scanCount = Integer.parseInt(redisConf.getProperty("scanCount","1000"));
        String password = redisConf.getProperty("password");
        String mDatabase = redisConf.getProperty("database");
        int database = mDatabase != null ? Integer.parseInt(mDatabase):0;

        this.pubsub_channel = redisConf.getProperty("channel","j2cache-session");

        long ct = System.currentTimeMillis();

        String scheme = redisConf.getProperty("scheme", "redis");
        String clusterName = redisConf.getProperty("cluster_name","j2cache-session");
        String sentinelMasterId = redisConf.getProperty("sentinelMasterId");
        String sentinelPassword = redisConf.getProperty("sentinelPassword");
        long clusterTopologyRefreshMs = Long.valueOf(redisConf.getProperty("clusterTopologyRefresh", "3000"));
        String protocolVersion = redisConf.getProperty("protocolVersion");

        if("redis-cluster".equalsIgnoreCase(scheme)) {
            scheme = "redis";
            List<RedisURI> redisURIs = new ArrayList<>();
            String[] hostArray = hosts.split(",");
            for(String host : hostArray) {
                String[] redisArray = host.split(":");
                RedisURI uri = RedisURI.create(redisArray[0], Integer.valueOf(redisArray[1]));
                uri.setDatabase(database);
                uri.setPassword(password);
                uri.setSentinelMasterId(sentinelMasterId);
                redisURIs.add(uri);
            }
            redisClient = RedisClusterClient.create(redisURIs);
            ClusterTopologyRefreshOptions topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                    //开启自适应刷新
                    .enableAdaptiveRefreshTrigger(ClusterTopologyRefreshOptions.RefreshTrigger.MOVED_REDIRECT, ClusterTopologyRefreshOptions.RefreshTrigger.PERSISTENT_RECONNECTS)
                    .enableAllAdaptiveRefreshTriggers()
                    .adaptiveRefreshTriggersTimeout(Duration.ofMillis(clusterTopologyRefreshMs))
                    //开启定时刷新,时间间隔根据实际情况修改
                    .enablePeriodicRefresh(Duration.ofMillis(clusterTopologyRefreshMs))
                    .build();
            ClusterClientOptions.Builder builder = ClusterClientOptions.builder().topologyRefreshOptions(topologyRefreshOptions);
            if(StringUtils.hasText(protocolVersion)){
                builder.protocolVersion(ProtocolVersion.valueOf(protocolVersion));
            }
            ((RedisClusterClient)redisClient).setOptions(builder.build());
        } else if("redis-sentinel".equalsIgnoreCase(scheme)) {
            scheme = "redis";
            String[] hostArray = hosts.split(",");
            RedisURI.Builder builder = null;
            boolean isFirst = true;
            for(String host : hostArray) {
                String[] redisArray = host.split(":");
                if(isFirst) {
                    builder = RedisURI.Builder.sentinel(
                            redisArray[0],
                            Integer.valueOf(redisArray[1]),
                            sentinelMasterId,
                            sentinelPassword);
                    isFirst = false;
                }
                else {
                    builder.withSentinel(redisArray[0], Integer.valueOf(redisArray[1]));
                }
            }
            assert builder != null;
            builder.withDatabase(database).withPassword(password);

            RedisURI uri = builder.build();
            redisClient = io.lettuce.core.RedisClient.create(uri);
        }else {
            String[] redisArray = hosts.split(":");
            RedisURI uri = RedisURI.create(redisArray[0], Integer.valueOf(redisArray[1]));
            uri.setDatabase(database);
            uri.setPassword(password);
            redisClient = io.lettuce.core.RedisClient.create(uri);
        }

        if(StringUtils.hasText(protocolVersion) && redisClient instanceof RedisClient){
            ((RedisClient)redisClient).setOptions(ClientOptions.builder().protocolVersion(ProtocolVersion.valueOf(protocolVersion)).build());
        }

        try {
            int timeout = Integer.parseInt(redisConf.getProperty("timeout", "10000"));
            redisClient.setDefaultTimeout(Duration.ofMillis(timeout));
        }catch(Exception e){
            logger.warn("Failed to set default timeout, using default 10000 milliseconds.", e);
        }

        //connection pool configurations
        GenericObjectPoolConfig<StatefulConnection<String, byte[]>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(Integer.parseInt(redisConf.getProperty("maxTotal", "100")));
        poolConfig.setMaxIdle(Integer.parseInt(redisConf.getProperty("maxIdle", "10")));
        poolConfig.setMinIdle(Integer.parseInt(redisConf.getProperty("minIdle", "10")));

        pool = ConnectionPoolSupport.createGenericObjectPool(() -> {
            if(redisClient instanceof io.lettuce.core.RedisClient)
                return ((io.lettuce.core.RedisClient)redisClient).connect(codec);
            else if(redisClient instanceof RedisClusterClient)
                return ((RedisClusterClient)redisClient).connect(codec);
            return null;
        }, poolConfig);

        this.cache2 = new LettuceCache(clusterName, redisClient,pool,scanCount);
        logger.info("J2Cache Session L2 CacheProvider {}.",this.cache2.getClass().getName());

        this.pubsub_subscriber = this.pubsub();
        this.pubsub_subscriber.addListener(this);
        RedisPubSubAsyncCommands<String, String> async = this.pubsub_subscriber.async();
        async.subscribe(this.pubsub_channel);
        logger.info("Connected to redis session channel:{}, time {}ms.", this.pubsub_channel, System.currentTimeMillis()-ct);

//        pubSubCommands = this.pubsub_subscriber.sync();
//        this.pubConnection = this.pubsub();
        this.publish(Command.join());
    }

    /**
     * Get PubSub connection
     * @return connection instance
     */
    private StatefulRedisPubSubConnection pubsub() {
        if(redisClient instanceof io.lettuce.core.RedisClient)
            return ((RedisClient)redisClient).connectPubSub();
        else if(redisClient instanceof RedisClusterClient)
            return ((RedisClusterClient)redisClient).connectPubSub();
        return null;
    }

    /**
     * 发送广播消息
     * @param cmd 待发布的消息
     */
    public void publish(Command cmd) {
        if(this.cache2 == null){
            return;
        }
        synchronized (CacheFacade.class){
            try (StatefulRedisPubSubConnection<String, String> connection = this.pubsub()){
                RedisPubSubCommands<String, String> sync = connection.sync();
                sync.publish(this.pubsub_channel, cmd.toString());
            }
        }

//        RedisPubSubCommands<String, String> sync = pubConnection.sync();
//        sync.publish(this.pubsub_channel, cmd.toString());

    }

    /**
     * 当接收到订阅频道获得的消息时触发此方法
     * @param channel 频道名称
     * @param message 消息体
     */
    @Override
    public void message(String channel, String message) {
        try {
            Command cmd = Command.parse(message);
            if (cmd == null || cmd.isLocal())
                return;

            switch (cmd.getOperator()) {
                case Command.OPT_JOIN:
                    logger.info("Server-"+cmd.getSrc() + " joined !");
                    break;
                case Command.OPT_DELETE_SESSION:
                    cache1.evict(cmd.getSession());
                    logger.debug("Received session clear command, session=" + cmd.getSession());
                    break;
                case Command.OPT_QUIT:
                    logger.info("Server-"+cmd.getSrc() + " quit !");
                    break;
                default:
                    logger.warn("Unknown command type = " + cmd.getOperator());
            }
        } catch (Exception e) {
            logger.error("Failed to handle received command", e);
        }
    }

    @Override
    public void notifyElementExpired(String session_id) {
        this.publish(new Command(Command.OPT_DELETE_SESSION, session_id, null));
    }

    @Override
    public void close() {
        try {
            this.publish(Command.quit());
            this.unsubscribed(this.pubsub_channel, 1);
        } finally {
            this.cache1.close();
            if(null != this.pubConnection){
                this.pubConnection.close();
            }
            if(null != this.pubsub_subscriber){
                this.pubsub_subscriber.close();
            }
            if(null != this.redisClient){
                this.redisClient.close();
            }

        }
    }


    /**
     * 读取 Session 对象信息
     * @param session_id  会话id
     * @return 返回会话对象
     */
    public SessionObject getSession(String session_id) {
        SessionObject session = (SessionObject)cache1.get(session_id);
        if(session != null)
            return session;
        synchronized (_g_keyLocks.computeIfAbsent(session_id, v -> new Object())) {
            session = (SessionObject)cache1.get(session_id);
            if(session != null)
                return session;
            try {
                if(this.cache2 == null){
                    return session;
                }
                List<String> keys = cache2.keys(session_id);
                if(keys.size() == 0)
                    return null;

                List<byte[]> datas = cache2.getBytes(session_id, keys);
                session = new SessionObject(session_id, keys, datas);
                cache1.put(session_id, session);
            } catch (Exception e) {
                logger.error("Failed to read session from j2cache", e);
            } finally {
                _g_keyLocks.remove(session_id);
            }
        }
        return session;
    }

    /**
     * 保存 Session 对象信息
     * @param session 会话对象
     */
    public void saveSession(SessionObject session) {
        session.setHost(IpUtils.getActivityLocalIp());
        session.setClientIP(com.eryansky.common.utils.net.IpUtils.getIpAddr(SpringMVCHolder.getRequest()));
        //write to caffeine
        cache1.put(session.getId(), session);
        if(this.cache2 == null){
            return;
        }
        //write to redis
        cache2.setBytes(session.getId(), new HashMap<String,byte[]>() {{
            put(SessionObject.KEY_CREATE_AT, String.valueOf(session.getCreated_at()).getBytes());
            put(SessionObject.KEY_ACCESS_AT, String.valueOf(session.getLastAccess_at()).getBytes());
            put(SessionObject.KEY_SERVICE_HOST, session.getHost().getBytes());
            put(SessionObject.KEY_CLIENT_IP, session.getClientIP().getBytes());
            put(SessionObject.KEY_ACCESS_COUNT,String.valueOf(session.getAccessCount()).getBytes());
            session.getAttributes().forEach((key, value) -> {
                try {
                    put(key, SerializationUtils.serialize(value));
                } catch (RuntimeException | IOException excp) {
                    if (!discardNonSerializable)
                        throw ((excp instanceof RuntimeException) ? (RuntimeException) excp : new RuntimeException(excp));
                }
            });
        }}, cache1.getExpire());
    }

    /**
     * 更新 session 的最后一次访问时间
     * @param session 会话对象
     */
    public void updateSessionAccessTime(SessionObject session) {
        try {
            session.setAccessCount(session.getAccessCount() + 1);
            session.setLastAccess_at(System.currentTimeMillis());
            cache1.put(session.getId(), session);
            if(this.cache2 != null){
                cache2.setBytes(session.getId(), SessionObject.KEY_ACCESS_AT, String.valueOf(session.getLastAccess_at()).getBytes());
                cache2.setBytes(session.getId(), SessionObject.KEY_ACCESS_COUNT, String.valueOf(session.getAccessCount()).getBytes());
                cache2.ttl(session.getId(), cache1.getExpire());
            }
        } finally {
            if(this.cache2 != null){
                this.publish(new Command(Command.OPT_DELETE_SESSION, session.getId(), null));
            }
        }
    }

    public void setSessionAttribute(SessionObject session, String key) {
        try {
            cache1.put(session.getId(), session);
            if(this.cache2 != null){
                try {
                    cache2.setBytes(session.getId(), key, SerializationUtils.serialize(session.get(key)));
                    cache2.ttl(session.getId(), cache1.getExpire());
                } catch (RuntimeException | IOException e) {
                    if(!this.discardNonSerializable)
                        throw ((e instanceof RuntimeException)?(RuntimeException)e : new RuntimeException(e));
                }
            }
        } finally {
            if(this.cache2 != null){
                this.publish(new Command(Command.OPT_DELETE_SESSION, session.getId(), null));
            }
        }
    }


    public void removeSessionAttribute(SessionObject session, String key) {
        try {
            cache1.put(session.getId(), session);
            if(this.cache2 != null){
                cache2.evict(session.getId(), key);
            }
        } finally {
            if(this.cache2 != null){
                this.publish(new Command(Command.OPT_DELETE_SESSION, session.getId(), null));
            }
        }
    }

    /**
     * 删除会话
     * @param session_id 会话id
     */
    public void deleteSession(String session_id) {
        try {
            cache1.evict(session_id);
            if(this.cache2 != null){
                cache2.clear(session_id);
            }
        } finally {
            if(this.cache2 != null){
                this.publish(new Command(Command.OPT_DELETE_SESSION, session_id, null));
            }
        }
    }

    /**
     * 获取所有keys
     * @return
     */
    public Collection<String> keys()  {
        Set<String> keys = new HashSet<>(cache1.keys());
        if(null != cache2){
            keys.addAll(cache2.keys());
        }
        return keys;
    }

    /**
     * 获取session超时时间
     * @return
     */
    public int getExpire() {
        return cache1.getExpire();
    }

    /**
     * 获取session_id的ttl时间
     * @param session_id
     * @return
     */
    public Long ttl1(String session_id) {
        return cache1.ttl(session_id);
//        SessionObject sessionObject = (SessionObject)cache1.get(session_id);
//        return null != sessionObject ? cache1.getExpire() - Duration.ofMillis(System.currentTimeMillis() - sessionObject.getLastAccess_at()).getSeconds() : null;
    }

    /**
     * 获取session_id的ttl时间
     * @param session_id
     * @return
     */
    public Long ttl2(String session_id)  {
        if(null != cache2){
            return cache2.ttl(session_id);
        }
        return null;
    }
}
