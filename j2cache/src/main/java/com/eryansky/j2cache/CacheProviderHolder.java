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
import com.eryansky.j2cache.lettuce.LettuceCacheProvider;
import com.eryansky.j2cache.util.AesSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Properties;

/**
 * 两级的缓存管理器
 * @author Winter Lau(javayou@gmail.com)
 */
public class CacheProviderHolder {

	private final static Logger log = LoggerFactory.getLogger(CacheProviderHolder.class);

	private CacheProvider l1_provider;
	private CacheProvider l2_provider;

	private CacheExpiredListener listener;

	private CacheProviderHolder() {
	}

	/**
	 * Initialize Cache Provider
	 *
	 * @param config   j2cache config instance
	 * @param listener cache listener
	 * @return holder : return CacheProviderHolder instance
	 */
	public static CacheProviderHolder init(J2CacheConfig config, CacheExpiredListener listener) {

		CacheProviderHolder holder = new CacheProviderHolder();

		holder.listener = listener;
		holder.l1_provider = loadProviderInstance(config.getL1CacheName());
		if (!holder.l1_provider.isLevel(CacheObject.LEVEL_1))
			throw new CacheException(holder.l1_provider.getClass().getName() + " is not level_1 cache provider");
		holder.l1_provider.start(config.getL1CacheProperties());
		log.info("Using L1 CacheProvider : {}", holder.l1_provider.getClass().getName());

		holder.l2_provider = loadProviderInstance(config.getL2CacheName());
		if (!holder.l2_provider.isLevel(CacheObject.LEVEL_2))
			throw new CacheException(holder.l2_provider.getClass().getName() + " is not level_2 cache provider");
		Properties l2_props = config.getL2CacheProperties();
		String password_encrypt = l2_props.getProperty("passwordEncrypt");
		String passwordEncryptKey = l2_props.getProperty("passwordEncryptKey");//长度16位
		boolean passwordEncrypt = Boolean.parseBoolean(password_encrypt);
		if(passwordEncrypt && !ObjectUtils.isEmpty(l2_props.getProperty("password"))){
			try {
				AesSupport aesSupport = null;
				if(StringUtils.hasText(passwordEncryptKey)){
					aesSupport = new AesSupport(StringUtils.trimAllWhitespace(passwordEncryptKey));
				}else{
					aesSupport = new AesSupport();
				}
				l2_props.put("password",aesSupport.decrypt(l2_props.getProperty("password")));
			} catch (NoSuchAlgorithmException e) {
				log.error(e.getMessage(),e);
			}
		}
		if(passwordEncrypt && !ObjectUtils.isEmpty(l2_props.getProperty("sentinelPassword"))){
			try {
				l2_props.put("sentinelPassword",new AesSupport().decrypt(l2_props.getProperty("sentinelPassword")));
			} catch (NoSuchAlgorithmException e) {
				log.error(e.getMessage(),e);
			}
		}

		holder.l2_provider.start(config.getL2CacheProperties());
		log.info("Using L2 CacheProvider : {}", holder.l2_provider.getClass().getName());

		return holder;
	}

	/**
	 * 关闭缓存
	 */
	public void shutdown() {
		l1_provider.stop();
		l2_provider.stop();
	}

	private static CacheProvider loadProviderInstance(String cacheIdent) {
		switch (cacheIdent.toLowerCase()) {
			case "caffeine":
				return new CaffeineProvider();
			case "lettuce":
				return new LettuceCacheProvider();
			case "none":
				return new NullCacheProvider();
		}
		try {
			return (CacheProvider) Class.forName(cacheIdent).getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
			throw new CacheException("Failed to initialize cache providers", e);
		}
    }

	public CacheProvider getL1Provider() {
		return l1_provider;
	}

	public CacheProvider getL2Provider() {
		return l2_provider;
	}

	/**
	 * 一级缓存实例
	 *
	 * @param region cache region
	 * @return level 1 cache instance
	 */
	public Level1Cache getLevel1Cache(String region) {
		return (Level1Cache) l1_provider.buildCache(region, listener);
	}

	/**
	 * 一级缓存实例
	 *
	 * @param region            cache region
	 * @param timeToLiveSeconds cache ttl
	 * @return level 1 cache instance
	 */
	public Level1Cache getLevel1Cache(String region, long timeToLiveSeconds) {
		return (Level1Cache) l1_provider.buildCache(region, timeToLiveSeconds, listener);
	}

	/**
	 * 二级缓存实例
	 *
	 * @param region cache region
	 * @return level 2 cache instance
	 */
	public Level2Cache getLevel2Cache(String region) {
		return (Level2Cache) l2_provider.buildCache(region, listener);
	}

	/**
	 * return all regions
	 *
	 * @return all regions
	 */
	public Collection<CacheChannel.Region> regions() {
		return l1_provider.regions();
	}


	/**
	 * return all regions
	 *
	 * @return all regions
	 */
	public Collection<CacheChannel.Region> queues() {
		return l1_provider.queues();
	}

}