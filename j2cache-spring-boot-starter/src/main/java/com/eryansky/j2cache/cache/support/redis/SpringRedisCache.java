package com.eryansky.j2cache.cache.support.redis;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import com.eryansky.j2cache.lock.LockCallback;
import com.eryansky.j2cache.lock.LockCantObtainException;
import com.eryansky.j2cache.lock.LockInsideExecutedException;
import com.eryansky.j2cache.lock.LockRetryFrequency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import com.eryansky.j2cache.Level2Cache;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.integration.redis.util.RedisLockRegistry;

/**
 * 重新实现二级缓存
 * 
 * @author zhangsaizz
 *
 */
public class SpringRedisCache implements Level2Cache {

	private final static Logger log = LoggerFactory.getLogger(SpringRedisCache.class);

	private final String namespace;

	private final String region;

	private final RedisTemplate<String, Serializable> redisTemplate;
	private final RedisLockRegistry redisLockRegistry;

	public SpringRedisCache(String namespace, String region, RedisTemplate<String, Serializable> redisTemplate,RedisLockRegistry redisLockRegistry) {
		if (region == null || region.isEmpty()) {
			region = "_"; // 缺省region
		}
		this.namespace = namespace;
		this.redisTemplate = redisTemplate;
		this.redisLockRegistry = redisLockRegistry;
		this.region = getRegionName(region);
	}

	private String getRegionName(String region) {
		if (namespace != null && !namespace.isEmpty())
			region = namespace + ":" + region;
		return region;
	}

	@Override
	public void clear() {
		redisTemplate.delete(region);
//		keys().forEach(k -> redisTemplate.opsForHash().delete(region,k));
	}

	@Override
	public boolean exists(String key) {
		return redisTemplate.opsForHash().hasKey(region, key);
	}

	@Override
	public void evict(String... keys) {
		HashOperations hashOperations = redisTemplate.opsForHash();
		for (String k : keys) {
			hashOperations.delete(region, k);
		}
	}

	@Override
	public Collection<String> keys() {
		Set<Object> list = redisTemplate.opsForHash().keys(region);
		List<String> keys = new ArrayList<>(list.size());
		for (Object object : list) {
			keys.add((String) object);
		}
		return keys;
	}

	@Override
	public byte[] getBytes(String key) {
		return redisTemplate.opsForHash().getOperations().execute((RedisCallback<byte[]>) redis -> redis.hGet(region.getBytes(), key.getBytes()));	
	}

	@Override
	public List<byte[]> getBytes(Collection<String> keys) {
		return redisTemplate.opsForHash().getOperations().execute((RedisCallback<List<byte[]>>) redis -> {
			byte[][] bytes = keys.stream().map(String::getBytes).toArray(byte[][]::new);
			return redis.hMGet(region.getBytes(), bytes);
		});
	}

	@Override
	public void put(String key, Object value) {
		redisTemplate.opsForHash().put(region, key, value);
	}

    /**
     * 设置缓存数据的有效期
     */
	@Override
	public void put(String key, Object value, long timeToLiveInSeconds) {
        redisTemplate.opsForHash().put(region, key, value);
    }

	@Override
	public void setBytes(String key, byte[] bytes) {
		redisTemplate.opsForHash().getOperations().execute((RedisCallback<List<byte[]>>) redis -> {
			redis.set(_key(key).getBytes(), bytes);
			redis.hSet(region.getBytes(), key.getBytes(), bytes);
			return null;
		});
	}

	@Override
	public void setBytes(Map<String, byte[]> bytes) {
		bytes.forEach(this::setBytes);
	}
	
	private String _key(String key) {
		return this.region + ":" + key;
	}

	@Override
	public Long ttl(String key) {
		return redisTemplate.opsForHash().getOperations().execute((RedisCallback<Long>) redis -> redis.ttl(key.getBytes(), TimeUnit.SECONDS));
	}


	@Override
	public void queuePush(String... values) {
		for(String value:values){
			redisTemplate.opsForHash().getOperations().execute((RedisCallback<Long>) redis -> redis.rPush(region.getBytes(), value.getBytes()));
		}
	}

	@Override
	public String queuePop() {
		byte[] result = redisTemplate.opsForHash().getOperations().execute((RedisCallback<byte[]>) redis -> redis.lPop(region.getBytes()));
		return null == result ? null : new String(result);
	}

	@Override
	public int queueSize() {
		Long result = redisTemplate.opsForHash().getOperations().execute((RedisCallback<Long>) redis -> redis.lLen(region.getBytes()));
		return null != result ? result.intValue():0;
	}

	@Override
	public Collection<String> queueList() {
		Long length = redisTemplate.opsForHash().getOperations().execute((RedisCallback<Long>) redis -> redis.lLen(region.getBytes()));
		if(null == length || length == 0){
			return Collections.emptyList();
		}
		List<byte[]> result = redisTemplate.opsForHash().getOperations().execute((RedisCallback<List<byte[]>>) redis -> redis.lRange(region.getBytes(),0,length-1));

		return null == result ? Collections.emptyList() : result.stream().map(String::new).collect(Collectors.toList());
	}

	@Override
	public void queueClear() {
		clear();
	}

	@Override
	public <T> T lock(LockRetryFrequency frequency, int timeoutInSecond, long keyExpireSeconds, LockCallback<T> lockCallback) throws LockInsideExecutedException, LockCantObtainException {
		int retryCount = Float.valueOf(timeoutInSecond * 1000 / frequency.getRetryInterval()).intValue();
//		long now = System.currentTimeMillis();
		for (int i = 0; i < retryCount; i++) {
			Lock lock = redisLockRegistry.obtain(region);
			boolean flag = lock.tryLock();
			if(flag) {
				try {
					return lockCallback.handleObtainLock();
				} catch (Exception e) {
					log.error(e.getMessage(),e);
					LockInsideExecutedException ie = new LockInsideExecutedException(e);
					return lockCallback.handleException(ie);
				} finally {
					redisTemplate.opsForHash().getOperations().execute((RedisCallback<Long>) redis -> redis.del(region.getBytes()));
				}
			} else {
				try {
					Thread.sleep(frequency.getRetryInterval());
				} catch (InterruptedException e) {
					log.error(e.getMessage(),e);
				}
			}
		}
		return lockCallback.handleNotObtainLock();
	}

}

