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
package com.eryansky.j2cache.ehcache;

import java.io.Serializable;
import java.util.*;

import com.eryansky.j2cache.CacheExpiredListener;
import com.eryansky.j2cache.Level1Cache;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

/**
 * <p>EHCache 2.x 的缓存封装</p>
 * <p>该封装类实现了缓存操作以及对缓存数据失效的侦听</p>
 *
 * @author Winter Lau(javayou@gmail.com)
 */
public class EhCache implements Level1Cache, CacheEventListener {
	
	private net.sf.ehcache.Cache cache;
	private CacheExpiredListener listener;

	/**
	 * Creates a new EhCache instance
	 *
	 * @param cache The underlying EhCache instance to use.
	 * @param listener cache listener
	 */
	public EhCache(Cache cache, CacheExpiredListener listener) {
		this.cache = cache;
		this.cache.getCacheEventNotificationService().registerListener(this);
		this.listener = listener;
	}

	@Override
	public long ttl() {
		return cache.getCacheConfiguration().getTimeToLiveSeconds();
	}

	@Override
	public long size() {
		return cache.getCacheConfiguration().getMaxEntriesLocalHeap();
	}

	@Override
	public Collection<String> keys() {
		return this.cache.getKeys();
	}

	/**
	 * Gets a value of an element which matches the given key.
	 *
	 * @param key the key of the element to return.
	 * @return The value placed into the cache with an earlier put, or null if not found or expired
	 */
	@Override
	public Serializable get(String key) {
		if ( key == null )
			return null;
		Element elem = cache.get( key );
		Object obj = (elem != null)?elem.getObjectValue() : null;
		return (obj == null || obj.getClass().equals(Object.class))?null:(Serializable)elem.getObjectValue();
	}

	/**
	 * Puts an object into the cache.
	 *
	 * @param key   a key
	 * @param value a value
	 */
	@Override
	public void put(String key, Object value) {
		cache.put(new Element(key, value));
	}

	/**
	 * Removes the element which matches the key
	 * If no element matches, nothing is removed and no Exception is thrown.
	 *
	 * @param keys the key of the element to remove
	 */
	@Override
	public void evict(String...keys) {
		cache.removeAll(Arrays.asList(keys));
	}

	@Override
	public Map<String, Object> get(Collection<String> keys) {
		Map<Object,Element> elements = cache.getAll(keys);
		Map<String, Object> results = new HashMap<>();
		elements.forEach((k,v)-> {
			if(v != null)
				results.put((String)k, v.getObjectValue());
		});
		return results;
	}

	@Override
	public boolean exists(String key) {
		return cache.isKeyInCache(key);
	}

	@Override
	public void put(Map<String, Object> elements) {
		List<Element> elems = new ArrayList<>();
		elements.forEach((k,v) -> elems.add(new Element(k,v)));
		cache.putAll(elems);
	}

	/**
	 * Remove all elements in the cache, but leave the cache
	 * in a useable state.
	 */
	public void clear() {
		cache.removeAll();
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public void notifyElementExpired(Ehcache cache, Element elem) {
		if(listener != null){
			listener.notifyElementExpired(cache.getName(), (String)elem.getObjectKey());
		}
	}

	@Override
	public void notifyElementEvicted(Ehcache cache, Element elem) {}

	@Override
	public void notifyElementPut(Ehcache cache, Element elem) {}

	@Override
	public void notifyElementRemoved(Ehcache cache, Element elem) {}

	@Override
	public void notifyElementUpdated(Ehcache cache, Element elem) {}

	@Override
	public void notifyRemoveAll(Ehcache cache) {}

	@Override
	public void dispose() {}

	@Override
	public Long ttl(String key) {
		Element element = cache.get(key);
		if(null == element){
			return null;
		}
		long lastAccessTime = element.getLastAccessTime();
		long creationTime = element.getCreationTime();
		int timeToLive = element.getTimeToLive() == 0 ? -1 : (int)
				(element.getTimeToLive() - ((lastAccessTime - creationTime)/1000)) ;
		return Long.valueOf(timeToLive);
	}
}