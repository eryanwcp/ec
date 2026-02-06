package com.eryansky.core.security;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.j2cache.session.CacheFacade;
import com.eryansky.j2cache.session.J2CacheSessionFilter;
import com.eryansky.j2cache.session.SessionObject;
import org.joda.time.Instant;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 应用Session上下文
 */
public class ApplicationSessionContext {
	private J2CacheSessionFilter sessionFilter;
	private CacheFacade cacheFacade;

	/**
	 * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
	 */
	public static final class Static {
		private static final FilterRegistrationBean<J2CacheSessionFilter> filterRegistrationBean = SpringContextHolder.getBean("j2CacheSessionFilter", FilterRegistrationBean.class);
		private static final ApplicationSessionContext instance = new ApplicationSessionContext(filterRegistrationBean.getFilter());
	}

	private ApplicationSessionContext() {
	}

	public ApplicationSessionContext(J2CacheSessionFilter sessionFilter) {
		this.sessionFilter = sessionFilter;
		this.cacheFacade = sessionFilter.getCache();
	}

	public static ApplicationSessionContext getInstance() {
		return Static.instance;
	}

	public void addSession(SessionInfo sessionInfo) {
		if (sessionInfo != null) {
			SessionObject sessionObject = cacheFacade.getSession(sessionInfo.getSessionId());
			sessionObject.put(SessionObject.KEY_SESSION_DATA,sessionInfo);
			cacheFacade.setSessionAttribute(sessionObject,SessionObject.KEY_SESSION_DATA);
		}
	}


	public void removeSessionInfo(String sessionId) {
		if (sessionId != null) {
			SessionObject sessionObject = cacheFacade.getSession(sessionId);
			if(null != sessionObject){
				sessionObject.remove(SessionObject.KEY_SESSION_DATA);
				cacheFacade.removeSessionAttribute(sessionObject,SessionObject.KEY_SESSION_DATA);
			}
		}
	}

	public Long sessionTTL1(String sessionId) {
		return cacheFacade.ttl1(sessionId);
	}

	public Long sessionTTL2(String sessionId) {
		return cacheFacade.ttl2(sessionId);
	}

	public SessionInfo getSession(String sessionId) {
		SessionObject sessionObject = getSessionObjectBySessionId(sessionId);
        SessionInfo sessionInfo = null != sessionObject ? (SessionInfo) sessionObject.get(SessionObject.KEY_SESSION_DATA) : null;
        if(null != sessionInfo){
            sessionInfo.setUpdateTime(Instant.ofEpochMilli(sessionObject.getLastAccess_at()).toDate());
        }
        return sessionInfo;
	}

	public List<SessionInfo> findSessionInfoData() {
		Collection<String> keys = cacheFacade.keys();
		return findSessionInfoData(keys);
	}

	public List<SessionInfo> findSessionInfoData(Collection<String> keys) {
	    if (Objects.isNull(keys) || keys.isEmpty()) {
	        return Collections.emptyList();
	    }

	    final int threshold = 1000;
	    if (keys.size() <= threshold) {
	        return keys.stream()
	                .map(cacheFacade::getSession)
	                .filter(Objects::nonNull)
	                .map(this::convertToSessionInfoWithUpdateTime)
	                .filter(Objects::nonNull)
	                .collect(Collectors.toList());
	    }

	    List<String> keyList = new ArrayList<>(keys);
	    int parallelism = Math.min(Runtime.getRuntime().availableProcessors(), keyList.size());
	    int batchSize = (keyList.size() + parallelism - 1) / parallelism;
	    ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, parallelism));
	    try {
	        List<CompletableFuture<List<SessionInfo>>> futures = new ArrayList<>(parallelism);
	        for (int i = 0; i < parallelism; i++) {
	            int from = i * batchSize;
	            if (from >= keyList.size()) break;
	            int to = Math.min(from + batchSize, keyList.size());
	            List<String> slice = keyList.subList(from, to);
	            futures.add(CompletableFuture.supplyAsync(() ->
	                    slice.stream()
	                            .map(cacheFacade::getSession)
	                            .filter(Objects::nonNull)
	                            .map(this::convertToSessionInfoWithUpdateTime)
	                            .filter(Objects::nonNull)
	                            .collect(Collectors.toList())
	                    , executor));
	        }

	        return futures.stream()
	                .map(CompletableFuture::join)
	                .flatMap(List::stream)
	                .collect(Collectors.toList());
	    } finally {
	        executor.shutdown();
	    }
	}

	/**
	 * 将SessionObject转换为SessionInfo，并设置更新时间
	 * @param sessionObject 缓存中的会话对象
	 * @return 填充了更新时间的SessionInfo（转换失败返回null）
	 */
	private SessionInfo convertToSessionInfoWithUpdateTime(SessionObject sessionObject) {
		try {
			// 提取会话数据并做类型校验，避免强制类型转换异常
			Object sessionData = sessionObject.get(SessionObject.KEY_SESSION_DATA);
			if (!(sessionData instanceof SessionInfo)) {
				return null;
			}
			SessionInfo sessionInfo = (SessionInfo) sessionData;

			// 转换最后访问时间为Date类型，填充到SessionInfo
			long lastAccessTime = sessionObject.getLastAccess_at();
			Date updateTime = Instant.ofEpochMilli(lastAccessTime).toDate();
			sessionInfo.setUpdateTime(updateTime);

			return sessionInfo;
		} catch (Exception e) {
			// 捕获时间转换/空值等异常，避免单个异常导致整个流终止
			// 可根据需要添加日志：log.warn("转换SessionObject失败", e);
			return null;
		}
	}

	public SessionInfo getSessionInfoBySessionInfoId(String sessionInfoId) {
		Collection<String> keys = cacheFacade.keys();
		if (Objects.isNull(keys) || keys.isEmpty()) {
			return null;
		}
		return keys.stream()
				.map(key -> cacheFacade.getSession(key))
				.filter(Objects::nonNull)
				.map(sessionObject -> (SessionInfo) sessionObject.get(SessionObject.KEY_SESSION_DATA))
				.filter(Objects::nonNull)
				.filter(sessionInfo -> sessionInfoId.equals(sessionInfo.getId()))
				.findFirst()
				.orElse(null);
	}

	public Collection<String> findSessionInfoKeys() {
	    Collection<String> keys = cacheFacade.keys();
	    if (Objects.isNull(keys) || keys.isEmpty()) {
	        return Collections.emptyList();
	    }

	    final int threshold = 1000;
	    if (keys.size() <= threshold) {
	        List<String> result = new ArrayList<>();
	        for (String key : keys) {
	            SessionObject sessionObject = cacheFacade.getSession(key);
	            if (sessionObject != null && sessionObject.get(SessionObject.KEY_SESSION_DATA) != null) {
	                result.add(key);
	            }
	        }
	        return result;
	    }

	    List<String> keyList = new ArrayList<>(keys);
	    int parallelism = Math.min(Runtime.getRuntime().availableProcessors(), keyList.size());
	    int batchSize = (keyList.size() + parallelism - 1) / parallelism;
	    ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, parallelism));
	    try {
	        List<CompletableFuture<List<String>>> futures = new ArrayList<>(parallelism);
	        for (int i = 0; i < parallelism; i++) {
	            int from = i * batchSize;
	            if (from >= keyList.size()) break;
	            int to = Math.min(from + batchSize, keyList.size());
	            List<String> slice = keyList.subList(from, to);
	            futures.add(CompletableFuture.supplyAsync(() -> {
	                List<String> sub = new ArrayList<>();
	                for (String key : slice) {
	                    SessionObject sessionObject = cacheFacade.getSession(key);
	                    if (sessionObject != null && sessionObject.get(SessionObject.KEY_SESSION_DATA) != null) {
	                        sub.add(key);
	                    }
	                }
	                return sub;
	            }, executor));
	        }

	        return futures.stream()
	                .map(CompletableFuture::join)
	                .flatMap(List::stream)
	                .collect(Collectors.toList());
	    } finally {
	        executor.shutdown();
	    }
	}

	public int findSessionInfoKeySize() {
		return findSessionInfoKeys().size();
	}

	public SessionObject getSessionObjectBySessionId(String sessionId) {
		if (sessionId == null) return null;
		SessionObject sessionObject = cacheFacade.getSession(sessionId);
		return sessionObject;
	}

	public void removeSession(String sessionId) {
		if (sessionId != null) {
			cacheFacade.deleteSession(sessionId);
		}
	}


    /**
     * 清空过期缓存L2
     */
    public long cleanupExpiredSessions() {
        return cacheFacade.cleanupExpiredL2Sessions();
    }

	public int findSessionKeySize() {
		Collection<String> keys = cacheFacade.keys();
		return keys.size();
	}

	public Collection<String> findSessionKeys() {
		Collection<String> keys = cacheFacade.keys();
		return keys;
	}


}