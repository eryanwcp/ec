package com.eryansky.j2cache.cache.support.redis;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import com.eryansky.j2cache.Level2Cache;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.integration.redis.util.RedisLockRegistry;

public class SpringRedisGenericCache implements Level2Cache {

	private final static Logger log = LoggerFactory.getLogger(SpringRedisGenericCache.class);

	private final String namespace;

	private final String region;

	private final RedisTemplate<String, Serializable> redisTemplate;
	private final RedisLockRegistry redisLockRegistry;
    private final int scanCount;

	public SpringRedisGenericCache(String namespace, String region, RedisTemplate<String, Serializable> redisTemplate,RedisLockRegistry redisLockRegistry, int scanCount) {
		if (region == null || region.isEmpty()) {
			region = "_"; // 缺省region
		}
		this.namespace = namespace;
		this.redisTemplate = redisTemplate;
		this.redisLockRegistry = redisLockRegistry;
		this.region = getRegionName(region);
		this.scanCount = scanCount;
	}

	private String getRegionName(String region) {
		if (namespace != null && !namespace.isEmpty())
			region = namespace + ":" + region;
		return region;
	}

	@Override
	public void clear() {
		Collection<String> keys = keys();
		keys.forEach(k -> {
			redisTemplate.delete(this.region + ":" + k);
		});
	}

	@Override
	public boolean exists(String key) {
		return Boolean.TRUE.equals(redisTemplate.execute((RedisCallback<Boolean>) redis -> redis.exists(_key(key))));
	}

	@Override
	public void evict(String... keys) {
		for (String k : keys) {
			redisTemplate.execute((RedisCallback<Long>) redis -> redis.del(_key(k)));
		}
	}

//	@Override
//	public Collection<String> keys() {
//		return redisTemplate.keys(this.region + ":*").stream().map(k->k.substring(this.region.length()+1)).collect(Collectors.toSet());
//	}

    @Override
    public Collection<String> keys() {
        return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = new HashSet<>();
            ScanOptions scanOptions = ScanOptions.scanOptions()
                    .match(this.region + ":*")  // 匹配命名空间前缀
                    .count(this.scanCount)         // 每次扫描数量
                    .build();

            try (Cursor<byte[]> cursor = connection.scan(scanOptions)) {
                while (cursor.hasNext()) {
                    String fullKey = new String(cursor.next(), StandardCharsets.UTF_8);
                    String keyWithoutNamespace = fullKey.substring(this.region.length() + 1);
                    keys.add(keyWithoutNamespace);
                }
            }
            return keys;
        });
    }

	@Override
	public byte[] getBytes(String key) {
		return redisTemplate.execute((RedisCallback<byte[]>) redis -> {
			return redis.get(_key(key));
		});
	}

	@Override
	public List<byte[]> getBytes(Collection<String> keys) {
		return redisTemplate.execute((RedisCallback<List<byte[]>>) redis -> {
			byte[][] bytes = keys.stream().map(this::_key).toArray(byte[][]::new);
			return redis.mGet(bytes);
		});
	}

	@Override
	public void setBytes(String key, byte[] bytes, long timeToLiveInSeconds) {
		if (timeToLiveInSeconds <= 0) {
			log.debug(String.format("Invalid timeToLiveInSeconds value : %d , skipped it.", timeToLiveInSeconds));
			setBytes(key, bytes);
		} else {
			redisTemplate.execute((RedisCallback<List<byte[]>>) redis -> {
				redis.setEx(_key(key), (int) timeToLiveInSeconds, bytes);
				return null;
			});
		}
	}

	@Override
	public void setBytes(Map<String, byte[]> bytes, long timeToLiveInSeconds) {
		bytes.forEach((k, v) -> setBytes(k, v, timeToLiveInSeconds));
	}

	@Override
	public void setBytes(String key, byte[] bytes) {
		redisTemplate.execute((RedisCallback<byte[]>) redis -> {
			redis.set(_key(key), bytes);
			return null;
		});
	}

	@Override
	public void setBytes(Map<String, byte[]> bytes) {
		 bytes.forEach(this::setBytes);
	}

	private byte[] _key(String key) {
		byte[] k;
		k = (this.region + ":" + key).getBytes(StandardCharsets.UTF_8);
		return k;
	}

	@Override
	public Long ttl(String key) {
		return redisTemplate.execute((RedisCallback<Long>) redis -> redis.ttl(_key(key), TimeUnit.SECONDS));
	}

	@Override
	public void queuePush(String... values) {
		for(String value:values){
			redisTemplate.execute((RedisCallback<Long>) redis -> redis.rPush(region.getBytes(), value.getBytes()));
		}
	}

	@Override
	public String queuePop() {
		byte[] result = redisTemplate.execute((RedisCallback<byte[]>) redis -> redis.lPop(region.getBytes()));
		return null == result ? null : new String(result);
	}

	@Override
	public int queueSize() {
		Long result = redisTemplate.execute((RedisCallback<Long>) redis -> redis.lLen(region.getBytes()));
		return null != result ? result.intValue():0;
	}

	@Override
	public Collection<String> queueList() {
		Long length = redisTemplate.execute((RedisCallback<Long>) redis -> redis.lLen(region.getBytes()));
		if(null == length || length == 0){
			return Collections.emptyList();
		}
		List<byte[]> result = redisTemplate.execute((RedisCallback<List<byte[]>>) redis -> redis.lRange(region.getBytes(),0,length-1));

		return null == result ? Collections.emptyList() : result.stream().map(String::new).collect(Collectors.toList());
	}

	@Override
	public void queueClear() {
		clear();
	}

	@Override
	public <T> T lock(LockRetryFrequency frequency, int timeoutInSecond, long keyExpireSeconds, LockCallback<T> lockCallback) throws LockInsideExecutedException, LockCantObtainException {
		long now = System.currentTimeMillis();
//		long expireSecond = now / 1000L + keyExpireSeconds;//设置加锁过期时间
//		long expireMillisSecond = now + keyExpireSeconds * 1000L;//作为值存入锁中(记录这把锁持有最终时限)
		int retryCount = Float.valueOf(timeoutInSecond * 1000 / frequency.getRetryInterval()).intValue();
		for (int i = 0; i < retryCount; i++) {
			Lock lock = redisLockRegistry.obtain(region);
			boolean flag = lock.tryLock();
			if(flag) {
				try {
					return lockCallback.handleObtainLock();
				} catch (Exception e) {
					LockInsideExecutedException ie = new LockInsideExecutedException(e);
					return lockCallback.handleException(ie);
				} finally {
					try {
						lock.unlock();
//						redisLockRegistry.expireUnusedOlderThan(keyExpireSeconds);
					} catch (Exception e) {
						log.error(e.getMessage());
					}
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
