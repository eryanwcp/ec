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
package com.eryansky.j2cache;

import com.eryansky.j2cache.caffeine.CaffeineProvider;
import com.eryansky.j2cache.lock.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cache Channel, the J2Cache methods explored to developers
 *
 * @author Winter Lau(javayou@gmail.com)
 */
public abstract class CacheChannel implements Closeable , AutoCloseable {

	private final Logger logger = LoggerFactory.getLogger(CacheChannel.class);

	private static final Map<String, Object> _g_keyLocks = new ConcurrentHashMap<>();
	private final J2CacheConfig config;
	private final CacheProviderHolder holder;
    private final boolean defaultCacheNullObject ;
	private boolean closed;
	private static final Map<String, LinkedBlockingQueue<String>> mQueueMap = new ConcurrentHashMap<>();
	private static final Map<String, ReentrantLock> mLockMap = new ConcurrentHashMap<>();

	public CacheChannel(J2CacheConfig config, CacheProviderHolder holder) {
		this.config = config;
		this.holder = holder;
		this.defaultCacheNullObject = config.isDefaultCacheNullObject();
		this.closed = false;
	}

	private NullObject newNullObject() {
		return new NullObject();
	}

	private void assertNotClose() {
		if(closed)
			throw new IllegalStateException("CacheChannel closed");
	}

	/**
	 * <p>Just for Inner Use.</p>
	 *
	 * <p>To clear the whole region when received this event .</p>
	 *
	 * @param region Cache region name
	 */
	protected abstract void sendClearCmd(String region);

	/**
	 * <p>Just for Inner Use.</p>
	 *
	 * <p>To remove cached data when received this event .</p>
	 *
	 * @param region Cache region name
	 * @param keys	Cache data key
	 */
	protected abstract void sendEvictCmd(String region, String...keys);

	/**
	 * 读取缓存（用户无需判断返回的对象是否为空）
	 * @param region Cache region name
	 * @param key Cache data key
     * @param cacheNullObject 是否缓存空对象
	 * @return cache object
	 */
	public CacheObject get(String region, String key, boolean...cacheNullObject)  {

		assertNotClose();

		CacheObject obj = new CacheObject(region, key, CacheObject.LEVEL_1);
		obj.setValue(holder.getLevel1Cache(region).get(key));
		if(obj.rawValue() != null)
			return obj;

		String lock_key = key + '%' + region;
		synchronized (_g_keyLocks.computeIfAbsent(lock_key, v -> new Object())) {
			obj.setValue(holder.getLevel1Cache(region).get(key));
			if(obj.rawValue() != null)
				return obj;

			try {
				obj.setLevel(CacheObject.LEVEL_2);
				obj.setValue(holder.getLevel2Cache(region).get(key));
				if (obj.rawValue() != null) {
					holder.getLevel1Cache(region).put(key, obj.rawValue());
				}else {
					boolean cacheNull = (cacheNullObject.length > 0) ? cacheNullObject[0] : defaultCacheNullObject;
					if (cacheNull)
						set(region, key, newNullObject(), true,null);
				}
			} finally {
				_g_keyLocks.remove(lock_key);
			}
		}

		return obj;
	}

	/**
	 * 支持外部数据自动加载的缓存方法
	 * @param region Cache region name
	 * @param key Cache data key
	 * @param loader data loader
	 * @param cacheNullObject  true if you need to cache null object
	 * @return cache object
	 */
	public CacheObject get(String region, String key, Function<String, Object> loader, boolean...cacheNullObject) {

		assertNotClose();

		CacheObject cache = get(region, key, false);

		if (cache.rawValue() != null)
			return cache ;

		String lock_key = key + '@' + region;
		synchronized (_g_keyLocks.computeIfAbsent(lock_key, v -> new Object())) {
			cache = get(region, key, false);

			if (cache.rawValue() != null)
				return cache ;

			try {
				Object obj = loader.apply(key);
				boolean cacheNull = (cacheNullObject.length>0)?cacheNullObject[0]: defaultCacheNullObject;
				set(region, key, obj, cacheNull,null);
				cache = new CacheObject(region, key, CacheObject.LEVEL_OUTER, obj);
			} finally {
				_g_keyLocks.remove(lock_key);
			}
		}
		return cache;
	}

	/**
	 * 批量读取缓存中的对象（用户无需判断返回的对象是否为空）
	 * @param region Cache region name
	 * @param keys cache keys
	 * @return cache object
	 */
	public Map<String, CacheObject> get(String region, Collection<String> keys)  {

		assertNotClose();

		final Map<String, Object> objs = holder.getLevel1Cache(region).get(keys);
		List<String> level2Keys = keys.stream().filter(k -> !objs.containsKey(k) || objs.get(k) == null).collect(Collectors.toList());
		Map<String, CacheObject> results = objs.entrySet().stream().filter(p -> p.getValue() != null).collect(
			Collectors.toMap(
				p -> p.getKey(),
				p -> new CacheObject(region, p.getKey(), CacheObject.LEVEL_1, p.getValue())
			)
		);

		Map<String, Object> objs_level2 = holder.getLevel2Cache(region).get(level2Keys);
		objs_level2.forEach((k,v) -> {
			results.put(k, new CacheObject(region, k, CacheObject.LEVEL_2, v));
			if (v != null)
				holder.getLevel1Cache(region).put(k, v);
		});

		return results;
	}

	/**
	 * 使用数据加载器的批量缓存读取
	 * @param region Cache region name
	 * @param keys cache keys
	 * @param loader data loader
	 * @param cacheNullObject true if you need to cache null object
	 * @return multiple cache data
	 */
	public Map<String, CacheObject> get(String region, Collection<String> keys, Function<String, Object> loader, boolean...cacheNullObject)  {

		assertNotClose();

		Map<String, CacheObject> results = get(region, keys);
		results.entrySet().stream().filter(e -> e.getValue().rawValue() == null).forEach( e -> {
			String lock_key = e.getKey() + '@' + region;
			synchronized (_g_keyLocks.computeIfAbsent(lock_key, v -> new Object())) {
				CacheObject cache = get(region, e.getKey(), false);
				if(cache.rawValue() == null) {
					try {
						Object obj = loader.apply(e.getKey());
						boolean cacheNull = (cacheNullObject.length>0)?cacheNullObject[0]: defaultCacheNullObject;
						set(region, e.getKey(), obj, cacheNull,null);
						e.getValue().setValue(obj);
						e.getValue().setLevel(CacheObject.LEVEL_OUTER);
					} finally {
						_g_keyLocks.remove(lock_key);
					}
				}
				else {
					e.setValue(cache);
				}
			}
		});
		return results;
	}

    /**
     * 将缓存对象转换为指定类型对象
     * @param cache
     * 缓存对象
     * @param dataClass
     * 指定数据类型
     * @param <T>
     *     数据类型
     * @return 缓存数据
     */
    private <T extends Serializable> T parse(final CacheObject cache, final Class<T> dataClass){
        if(cache == null || cache.getValue() == null){
            return null;
        }
        return dataClass.cast(cache.getValue());
    }

    /**
     * 将缓存对象转换为指定类型对象
     * @param cacheObjectMap
     * 缓存Map
     * @param dataClass
     * 指定数据类型
     * @param <T>
     *     数据类型
     * @return 转换后的Map
     */
    private <T extends Serializable> Map<String, T> parse(final Map<String, CacheObject> cacheObjectMap, final Class<T> dataClass) {
        if (cacheObjectMap == null || cacheObjectMap.size() == 0 || dataClass == null) {
            return null;
        }
        return cacheObjectMap.entrySet().stream()
                .map(entry -> {
                    if (entry != null && entry.getValue() != null) {
                        final T data = parse(entry.getValue(), dataClass);
                        if (data != null) {
                            return new Map.Entry<String, T>() {

                                @Override
                                public String getKey() {
                                    return entry.getKey();
                                }

                                @Override
                                public T getValue() {
                                    return data;
                                }

                                @Override
                                public T setValue(T value) {
                                    return value;
                                }
                            };
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    }

    /**
     * 读取指定类型的缓存
     * @param region
     * Cache region name
     * @param key
     * Cache data key
     * @param dataClass
     * 数据类型class
     * @param <T>
     *     数据类型
     * @return 缓存数据
     */
    public <T extends Serializable> T get(final String region, final String key, final Class<T> dataClass){
        return parse(get(region, key), dataClass);
    }

    /**
     * 支持外部数据自动加载的缓存方法
     * @param region
     * Cache region name
     * @param key
     * Cache data key
     * @param dataClass
     * 数据类型class
     * @param loader
     * data loader
     * @param <T>
     *     数据类型
     * @return 缓存数据
     */
    public <T extends Serializable> T get(final String region, final String key, final Class<T> dataClass,final Function<String, T> loader){
        return parse(get(region, key, loader::apply), dataClass);
    }

    /**
     * 批量读取缓存中的指定类型对象
     * @param region
     * Cache region name
     * @param keys
     * cache keys
     * @param dataClass
     * 数据类型class
     * @param <T>
     *     数据类型
     * @return 缓存对象Map
     */
    public <T extends Serializable> Map<String, T> get(final String region,final Collection<String> keys, final Class<T> dataClass){
        return parse(get(region, keys), dataClass);
    }

    /**
     * 使用数据加载器的批量缓存读取指定类型
     * @param region
     * Cache region name
     * @param keys
     * cache keys
     * @param dataClass
     * 数据类型class
     * @param loader
     * data loader
     * @param <T>
     *     数据类型
     * @return 缓存对象Map
     */
    public <T extends Serializable> Map<String, T> get(final String region,final Collection<String> keys, final Class<T> dataClass,final Function<String, T> loader){
        return parse(get(region, keys, loader::apply), dataClass);
    }

	/**
	 * 判断某个缓存键是否存在
	 * @param region Cache region name
	 * @param key cache key
	 * @return true if key exists
	 */
	public boolean exists(String region, String key) {
		return check(region, key) > 0;
	}

	/**
	 * 判断某个key存在于哪级的缓存中
	 * @param region cache region
	 * @param key cache key
	 * @return  0(不存在),1(一级),2(二级)
	 */
	public int check(String region, String key) {

		assertNotClose();

		if(holder.getLevel1Cache(region).exists(key))
			return 1;
		if(holder.getLevel2Cache(region).exists(key))
			return 2;
		return 0;
	}

	/**
	 * Write data to J2Cache
	 *
	 * @param region: Cache Region name
	 * @param key: Cache key
	 * @param value: Cache value
	 */
	public void set(String region, String key, Object value) {
		set(region, key, value, defaultCacheNullObject,null);
	}

	/**
	 * Write data to J2Cache
	 *
	 * @param region: Cache Region name
	 * @param key: Cache key
	 * @param value: Cache value
	 * @param level1Cache 仅限一级缓存
	 */
	public void set(String region, String key, Object value,Boolean level1Cache) {
		set(region, key, value, defaultCacheNullObject,level1Cache);
	}

	/**
	 * Write data to J2Cache
	 *
	 * @param region: Cache Region name
	 * @param key: Cache key
	 * @param value: Cache value
	 * @param cacheNullObject if allow cache null object
	 */
//	public void set(String region, String key, Object value, boolean cacheNullObject) {
//		set(region, key, value, cacheNullObject,null);
//    }

	/**
	 * Write data to J2Cache
	 *
	 * @param region: Cache Region name
	 * @param key: Cache key
	 * @param value: Cache value
	 * @param cacheNullObject if allow cache null object
	 * @param level1Cache 仅限一级缓存
	 */
	public void set(String region, String key, Object value, boolean cacheNullObject,Boolean level1Cache) {
		if (!cacheNullObject && value == null)
			return ;

		assertNotClose();

		try {
			Level1Cache level1 = holder.getLevel1Cache(region);
			level1.put(key, (value == null && cacheNullObject) ? newNullObject() : value);
			if (null != level1Cache && level1Cache) {
				return;
			}
			Level2Cache level2 = holder.getLevel2Cache(region);
			if (config.isSyncTtlToRedis())
				level2.put(key, (value == null && cacheNullObject) ? newNullObject() : value, level1.ttl());
			else
				level2.put(key, (value == null && cacheNullObject) ? newNullObject() : value);
		} finally {
			this.sendEvictCmd(region, key);//清除原有的一级缓存的内容
		}
	}


	/**
	 * Write data to j2cache with expired setting
	 *
	 * <strong>注意：强烈不推荐使用带 TTL 的 set 方法，所有的缓存 TTL 都应该预先配置好，避免多个节点的缓存 Region 配置不同步</strong>
	 *
	 * @param region Cache Region name
	 * @param key Cache Key
	 * @param value Cache value
	 * @param timeToLiveInSeconds cache expired in second
	 */
	public void set(String region, String key, Object value, long timeToLiveInSeconds ) {
		set(region, key, value, timeToLiveInSeconds, defaultCacheNullObject,null);
	}

	/**
	 * Write data to j2cache with expired setting
	 *
	 * <strong>注意：强烈不推荐使用带 TTL 的 set 方法，所有的缓存 TTL 都应该预先配置好，避免多个节点的缓存 Region 配置不同步</strong>
	 *
	 * @param region Cache Region name
	 * @param key Cache Key
	 * @param value Cache value
	 * @param timeToLiveInSeconds cache expired in second
	 * @param level1Cache 仅限一级缓存
	 */
	public void set(String region, String key, Object value, long timeToLiveInSeconds,Boolean level1Cache) {
		set(region, key, value, timeToLiveInSeconds, defaultCacheNullObject,level1Cache);
	}

	/**
	 * Write data to j2cache with expired setting
	 *
	 * <strong>注意：强烈不推荐使用带 TTL 的 set 方法，所有的缓存 TTL 都应该预先配置好，避免多个节点的缓存 Region 配置不同步</strong>
	 *
	 * @param region Cache Region name
	 * @param key Cache Key
	 * @param value Cache value
	 * @param timeToLiveInSeconds cache expired in second
	 * @param cacheNullObject if allow cache null object
	 * @param level1Cache 仅限一级缓存
	 */
    public void set(String region, String key, Object value, long timeToLiveInSeconds, boolean cacheNullObject,Boolean level1Cache) {

		assertNotClose();

		if (!cacheNullObject && value == null)
			return ;

    	if(timeToLiveInSeconds <= 0)
    		set(region, key, value, cacheNullObject,null);
    	else {
			try {
				holder.getLevel1Cache(region, timeToLiveInSeconds).put(key, (value==null && cacheNullObject)?newNullObject():value);
				if (null != level1Cache && level1Cache) {
					return;
				}
				Level2Cache level2 = holder.getLevel2Cache(region);
				if(config.isSyncTtlToRedis())
					level2.put(key, (value==null && cacheNullObject)?newNullObject():value, timeToLiveInSeconds);
				else
					level2.put(key, (value==null && cacheNullObject)?newNullObject():value);
			} finally {
				this.sendEvictCmd(region, key);//清除原有的一级缓存的内容
			}
		}
	}

	/**
	 * 批量插入数据
	 * @param region Cache Region name
	 * @param elements Cache Elements
	 */
	public void set(String region, Map<String, Object> elements){
    	set(region, elements, defaultCacheNullObject,null);
	}

	/**
	 * 批量插入数据
	 * @param region Cache Region name
	 * @param elements Cache Elements
	 * @param level1Cache 仅限一级缓存
	 */
	public void set(String region, Map<String, Object> elements,Boolean level1Cache){
		set(region, elements, defaultCacheNullObject,level1Cache);
	}

	/**
	 * 批量插入数据
	 * @param region Cache Region name
	 * @param elements Cache Elements
	 * @param cacheNullObject if allow cache null object
	 * @param level1Cache 仅限一级缓存
	 */
	public void set(String region, Map<String, Object> elements, boolean cacheNullObject,Boolean level1Cache)  {

		assertNotClose();

		try {
			if (cacheNullObject && elements.containsValue(null)) {
				Map<String, Object> newElems = new HashMap<>(elements);
				newElems.forEach((k,v) -> {
					if (v == null)
						newElems.put(k, newNullObject());
				});
				Level1Cache level1 = holder.getLevel1Cache(region);
				level1.put(newElems);
				if (null != level1Cache && level1Cache) {
					return;
				}
				if(config.isSyncTtlToRedis())
					holder.getLevel2Cache(region).put(newElems, level1.ttl());
				else
					holder.getLevel2Cache(region).put(newElems);
			}
			else {
				Level1Cache level1 = holder.getLevel1Cache(region);
				level1.put(elements);
				if (null != level1Cache && level1Cache) {
					return;
				}
				if(config.isSyncTtlToRedis())
					holder.getLevel2Cache(region).put(elements, level1.ttl());
				else

					holder.getLevel2Cache(region).put(elements);
			}
		} finally {
			//广播
			this.sendEvictCmd(region, elements.keySet().stream().toArray(String[]::new));
		}
	}

	/**
	 * 带失效时间的批量缓存数据插入
	 *
	 * <strong>注意：强烈不推荐使用带 TTL 的 set 方法，所有的缓存 TTL 都应该预先配置好，避免多个节点的缓存 Region 配置不同步</strong>
	 *
	 * @param region Cache Region name
	 * @param elements Cache Elements
	 * @param timeToLiveInSeconds cache expired in second
	 */
	public void set(String region, Map<String, Object> elements, long timeToLiveInSeconds){
		set(region, elements, timeToLiveInSeconds, defaultCacheNullObject,null);
	}


	/**
	 * 带失效时间的批量缓存数据插入
	 *
	 * <strong>注意：强烈不推荐使用带 TTL 的 set 方法，所有的缓存 TTL 都应该预先配置好，避免多个节点的缓存 Region 配置不同步</strong>
	 *
	 * @param region Cache Region name
	 * @param elements Cache Elements
	 * @param timeToLiveInSeconds cache expired in second
	 * @param level1Cache 仅限一级缓存
	 */
	public void set(String region, Map<String, Object> elements, long timeToLiveInSeconds,Boolean level1Cache){
		set(region, elements, timeToLiveInSeconds, defaultCacheNullObject,level1Cache);
	}

	/**
	 * 带失效时间的批量缓存数据插入
	 *
	 * <strong>注意：强烈不推荐使用带 TTL 的 set 方法，所有的缓存 TTL 都应该预先配置好，避免多个节点的缓存 Region 配置不同步</strong>
	 *
	 * @param region Cache Region name
	 * @param elements Cache Elements
	 * @param timeToLiveInSeconds cache expired in second
	 * @param cacheNullObject if allow cache null object
	 * @param level1Cache 仅限一级缓存
	 */
	public void set(String region, Map<String, Object> elements, long timeToLiveInSeconds, boolean cacheNullObject,Boolean level1Cache)  {

		assertNotClose();

		if(timeToLiveInSeconds <= 0)
			set(region, elements, cacheNullObject,level1Cache);
		else {
			try {
				if (cacheNullObject && elements.containsValue(null)) {
					Map<String, Object> newElems = new HashMap<>(elements);
					newElems.forEach((k,v) -> {
						if (v == null)
							newElems.put(k, newNullObject());
					});
					holder.getLevel1Cache(region, timeToLiveInSeconds).put(newElems);
					if (null != level1Cache && level1Cache) {
						return;
					}
					if(config.isSyncTtlToRedis())
						holder.getLevel2Cache(region).put(newElems, timeToLiveInSeconds);
					else
						holder.getLevel2Cache(region).put(newElems);
				}
				else {
					holder.getLevel1Cache(region, timeToLiveInSeconds).put(elements);
					if (null != level1Cache && level1Cache) {
						return;
					}
					if(config.isSyncTtlToRedis())
						holder.getLevel2Cache(region).put(elements, timeToLiveInSeconds);
					else
						holder.getLevel2Cache(region).put(elements);
				}
			} finally {
				//广播
				this.sendEvictCmd(region, elements.keySet().toArray(new String[0]));
			}
		}
	}

	/**
	 * Remove cached data in J2Cache
	 *
	 * @param region:  Cache Region name
	 * @param keys: Cache key
	 */
	public void evict(String region, String...keys)  {

		assertNotClose();

		try {
			//先清比较耗时的二级缓存，再清一级缓存
			holder.getLevel2Cache(region).evict(keys);
			holder.getLevel1Cache(region).evict(keys);
		} finally {
			this.sendEvictCmd(region, keys); //发送广播
		}
    }

	/**
	 * Clear the cache
	 *
	 * @param region: Cache region name
	 */
	public void clear(String region)  {

		assertNotClose();

		try {
			//先清比较耗时的二级缓存，再清一级缓存
			holder.getLevel2Cache(region).clear();
			holder.getLevel1Cache(region).clear();
		}finally {
			this.sendClearCmd(region);
		}
    }

	/**
	 * 返回所有的缓存区域
	 * @return all the regions
	 */
	public Collection<Region> regions() {

		if(closed)
			throw new IllegalStateException("CacheChannel closed");

		return holder.regions();
	}

	/**
	 * 删除缓存 Region
	 * @param region Cache Region Name
	 */
	public void removeRegion(String region) {

		assertNotClose();

		holder.getL1Provider().removeCache(region);
	}

	/**
	 * <p>Get cache region keys</p>
	 * <p><strong>Notice: ehcache3 not support keys</strong></p>
	 *
	 * @param region: Cache region name
	 * @return key list
	 */
	public Collection<String> keys(String region)  {
		assertNotClose();

		Set<String> keys = new HashSet<>();
		keys.addAll(holder.getLevel1Cache(region).keys());
		keys.addAll(holder.getLevel2Cache(region).keys());
		return keys;
    }

	/**
	 * key大小
	 * @param region
	 * @return
	 */
	public int keySize(String region)  {
		return keys(region).size();
	}


	/**
	 * 获取key的ttl时间
	 * @param region
	 * @param key
	 * @return
	 */
	public Long ttl(String region,String key)  {
		assertNotClose();
		Long ttl =  ttl(region,key,1);
		return null != ttl ? ttl:ttl(region,key,2);
	}

	/**
	 * 获取key的ttl时间
	 * @param region
	 * @param key
	 * @param level 缓存级别
	 * @return
	 */
	public Long ttl(String region,String key,int level)  {
		assertNotClose();
		return level > 1 ? holder.getLevel2Cache(region).ttl(key):holder.getLevel1Cache(region).ttl(key);
	}

	/**
	 * Close J2Cache
	 */
	@Override
	public void close() {
		this.closed = true;
	}

	/**
	 * 获取一级缓存接口
	 * @return 返回一级缓存的 CacheProvider 实例
	 */
	public CacheProvider getL1Provider() {
		return holder.getL1Provider();
	}

	/**
	 * <p>获取二级缓存的接口，该方法可用来直接获取 J2Cache 底层的 Redis 客户端实例</p>
	 * <p>方法如下：</p>
	 * <code>
	 *     CacheChannel channel = J2Cache.getChannel();
	 *     RedisClient client = ((RedisCacheProvider)channel.getL2Provider()).getRedisClient();
	 *     try {
	 *     	   client.get().xxxxx(...);
	 *     } finally {
	 *         client.release();
	 *     }
	 * </code>
	 * @return 返回二级缓存的 CacheProvider 实例
	 */
	public CacheProvider getL2Provider() {
		return holder.getL2Provider();
	}

	/**
	 * Cache Region Define
	 */
	public static class Region {

		private String name;
		private long size;
		private long ttl;

		public Region(String name, long size, long ttl) {
			this.name = name;
			this.size = size;
			this.ttl = ttl;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}

		public long getTtl() {
			return ttl;
		}

		public void setTtl(long ttl) {
			this.ttl = ttl;
		}

		@Override
		public String toString() {
			return String.format("[%s,size:%d,ttl:%d]", name, size, ttl);
		}
	}


	/**
	 * 队列 压入
	 * @param region
	 * @param values
	 */
	public void queuePush(String region, String... values){
		assertNotClose();

		Level2Cache level2Cache = holder.getLevel2Cache(region);
		if(!(level2Cache instanceof NullCache)){
			level2Cache.queuePush(values);
		}else{
			LinkedBlockingQueue<String> queue = mQueueMap.computeIfAbsent(region, k -> new LinkedBlockingQueue<>());
			queue.addAll(Arrays.asList(values));
		}
	}

	/**
	 * 队列 弹出
	 * @param region
	 * @return
	 */
	public String queuePop(String region) {
		assertNotClose();
		Level2Cache level2Cache = holder.getLevel2Cache(region);
		if(!(level2Cache instanceof NullCache)){
			return level2Cache.queuePop();
		}else{
			LinkedBlockingQueue<String> queue = mQueueMap.computeIfAbsent(region, k -> new LinkedBlockingQueue<>());
			return queue.poll();
		}
	}

	/**
	 * 队列 大小
	 * @param region
	 * @return
	 */
	public int queueSize(String region) {
		assertNotClose();
		Level2Cache level2Cache = holder.getLevel2Cache(region);
		if(!(level2Cache instanceof NullCache)){
			return level2Cache.queueSize();
		}else{
			LinkedBlockingQueue<String> queue = mQueueMap.computeIfAbsent(region, k -> new LinkedBlockingQueue<>());
			return queue.size();
		}
	}

	/**
	 * 队列 列表
	 * @param region
	 * @return
	 */
	public Collection<String> queueList(String region) {
		assertNotClose();
		Level2Cache level2Cache = holder.getLevel2Cache(region);
		if(!(level2Cache instanceof NullCache)){
			return level2Cache.queueList();
		}else{
			LinkedBlockingQueue<String> queue = mQueueMap.computeIfAbsent(region, k ->new LinkedBlockingQueue<>());
			return new ArrayList<>(queue);
		}
	}

	/**
	 * 队列 清空
	 * @param region
	 */
	public void queueClear(String region) {
		assertNotClose();
        Level2Cache level2Cache = holder.getLevel2Cache(region);
        if(!(level2Cache instanceof NullCache)){
            level2Cache.queueClear();
        }else{
			LinkedBlockingQueue<String> queue = mQueueMap.computeIfAbsent(region, k -> new LinkedBlockingQueue<>());
			queue.clear();
        }
	}

	/**
	 * 队列 key列表
	 * @return
	 */
	public Collection<Region> queues() {
		assertNotClose();
		return holder.queues();
//		Level2Cache level2Cache = holder.getLevel2Cache(CaffeineProvider.QUEUE_DEFAULT);
//		if(!(level2Cache instanceof NullCache)){
//			return holder.queues();
//		}else{
//			return mQueueMap.keySet().stream().map(v->new Region(v,-1,-1)).collect(Collectors.toList());
//		}
	}


	/**
	 * 锁定对象（自动释放锁）
	 * @param region 锁对象
	 * @param keyExpireSeconds 锁超时时间（使用redis有效） 单位：秒
	 * @param lockCallback 回调函数
	 * @param <T>
	 * @return
	 * @throws LockInsideExecutedException
	 * @throws LockCantObtainException
	 */
	public <T> T lock(String region, long keyExpireSeconds,
					  LockCallback<T> lockCallback) throws LockInsideExecutedException, LockCantObtainException {
		return lock(region,LockRetryFrequency.VERY_SLOW,1,keyExpireSeconds,lockCallback);
	}

	/**
	 * 锁定对象（自动释放锁）
	 * @param region 锁对象
	 * @param timeoutInSecond 获取锁超时时间 单位：秒
	 * @param keyExpireSeconds 锁超时时间（使用redis有效） 单位：秒
	 * @param lockCallback 回调函数
	 * @param <T>
	 * @return
	 * @throws LockInsideExecutedException
	 * @throws LockCantObtainException
	 */
	public <T> T lock(String region, int timeoutInSecond, long keyExpireSeconds,
					  LockCallback<T> lockCallback) throws LockInsideExecutedException, LockCantObtainException {
		return lock(region,LockRetryFrequency.NORMAL,timeoutInSecond,keyExpireSeconds,lockCallback);
	}

	/**
	 * 锁定对象（自动释放锁）
	 * @param region 锁对象
	 * @param timeoutInSecond 获取锁超时时间 单位：秒
	 * @param keyExpireSeconds 锁超时时间（使用redis有效） 单位：秒
	 * @param lockCallback 回调函数
	 * @param level1 仅限本地锁
	 * @param <T>
	 * @return
	 * @throws LockInsideExecutedException
	 * @throws LockCantObtainException
	 */
	public <T> T lock(String region, int timeoutInSecond, long keyExpireSeconds,
					  LockCallback<T> lockCallback,Boolean level1) throws LockInsideExecutedException, LockCantObtainException {
		return lock(region,LockRetryFrequency.NORMAL,timeoutInSecond,keyExpireSeconds,lockCallback,level1);
	}


	/**
	 * 锁定对象（自动释放锁）
	 * @param region 锁对象
	 * @param frequency {@link LockRetryFrequency}
	 * @param timeoutInSecond 获取锁超时时间 单位：秒
	 * @param keyExpireSeconds 锁超时时间（使用redis有效） 单位：秒
	 * @param lockCallback 回调函数
	 * @param <T>
	 * @return
	 * @throws LockInsideExecutedException
	 * @throws LockCantObtainException
	 */
	public <T> T lock(String region, LockRetryFrequency frequency, int timeoutInSecond, long keyExpireSeconds,
						LockCallback<T> lockCallback) throws LockInsideExecutedException, LockCantObtainException {
		return lock(region, frequency, timeoutInSecond, keyExpireSeconds, lockCallback,false);
	}


	/**
	 * 锁定对象（自动释放锁）
	 * @param region 锁对象
	 * @param frequency {@link LockRetryFrequency}
	 * @param timeoutInSecond 获取锁超时时间 单位：秒
	 * @param keyExpireSeconds 锁超时时间（使用redis有效） 单位：秒
	 * @param lockCallback 回调函数
	 * @param level1 仅限本地锁
	 * @param <T>
	 * @return
	 * @throws LockInsideExecutedException
	 * @throws LockCantObtainException
	 */
	public <T> T lock(String region, LockRetryFrequency frequency, int timeoutInSecond, long keyExpireSeconds,
					  LockCallback<T> lockCallback,Boolean level1) throws LockInsideExecutedException, LockCantObtainException {
		Level2Cache level2Cache = holder.getLevel2Cache(region);
		if((null == level1 || !level1) && !(level2Cache instanceof NullCache)){
			return level2Cache.lock(frequency,timeoutInSecond, keyExpireSeconds,lockCallback);
		}else{
			ReentrantLock lock  = mLockMap.computeIfAbsent(region, k -> new ReentrantLock());
			int retryCount = Float.valueOf(timeoutInSecond * 1000 / (float)frequency.getRetryInterval()).intValue();

			for (int i = 0; i < retryCount; i++) {
				boolean flag = lock.tryLock();
				if(flag) {
					try {
						return lockCallback.handleObtainLock();
					} catch (Exception e) {
						logger.error(e.getMessage(),e);
						LockInsideExecutedException ie = new LockInsideExecutedException(e.getMessage(),e);
						return lockCallback.handleException(ie);
					} finally {
						try {
							lock.unlock();
						} catch (Exception e) {
							logger.error(e.getMessage());
						}
					}
				} else {
					try {
						Thread.sleep(frequency.getRetryInterval());
					} catch (InterruptedException e) {
						logger.error(e.getMessage(),e);
					}
				}
			}
			return lockCallback.handleNotObtainLock();
		}
	}


	/**
	 * 尝试锁
	 * @param region 锁对象
	 * @param key
	 * @return
	 * @throws LockInsideExecutedException
	 * @throws LockCantObtainException
	 */
	public boolean  tryLock(String region, String key) throws LockInsideExecutedException, LockCantObtainException {
		CacheObject cacheObject = get(region,key);
		return null == cacheObject || null == cacheObject.getValue();
	}

}
