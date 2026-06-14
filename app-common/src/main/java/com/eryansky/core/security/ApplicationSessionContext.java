package com.eryansky.core.security;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.j2cache.session.CacheFacade;
import com.eryansky.j2cache.session.J2CacheSessionFilter;
import com.eryansky.j2cache.session.SessionObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 应用Session上下文
 */
public class ApplicationSessionContext {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationSessionContext.class);

	// 定义并行处理的阈值，避免“魔术数字”
	private static final int PARALLEL_PROCESSING_THRESHOLD = 1000;

	private J2CacheSessionFilter sessionFilter;
	private CacheFacade cacheFacade;

	/**
	 * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
	 */
	public static final class Static {
		// 假设j2CacheSessionFilter在Spring上下文中是FilterRegistrationBean<J2CacheSessionFilter>类型
		private static final FilterRegistrationBean<?> filterRegistrationBean = SpringContextHolder.getBean("j2CacheSessionFilter", FilterRegistrationBean.class);
		// 确保获取到的是J2CacheSessionFilter实例
		private static final ApplicationSessionContext instance = new ApplicationSessionContext((J2CacheSessionFilter) filterRegistrationBean.getFilter());
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
			if (sessionObject == null) {
				// 如果sessionObject不存在，可能需要创建一个新的，或者根据业务逻辑决定如何处理
				// 这里假设getSession会返回一个可用的SessionObject，或者需要先创建
				logger.warn("SessionObject for sessionId {} not found when adding session. This might indicate an issue or a new session.", sessionInfo.getSessionId());
				// 考虑是否需要在这里创建新的SessionObject并添加到缓存
				// 例如: sessionObject = new SessionObject(sessionInfo.getSessionId()); cacheFacade.setSession(sessionObject);
				return; // 或者继续处理，取决于cacheFacade.getSession的行为
			}
			// 假设cacheFacade.setSessionAttribute会负责将SessionObject的修改持久化到缓存
			sessionObject.put(SessionObject.KEY_SESSION_DATA, sessionInfo);
			cacheFacade.setSessionAttribute(sessionObject, SessionObject.KEY_SESSION_DATA);
		}
	}

	public void removeSessionInfo(String sessionId) {
		if (sessionId != null) {
			SessionObject sessionObject = cacheFacade.getSession(sessionId);
			if (null != sessionObject) {
				// 假设cacheFacade.removeSessionAttribute会负责将SessionObject的修改持久化到缓存
				sessionObject.remove(SessionObject.KEY_SESSION_DATA);
				cacheFacade.removeSessionAttribute(sessionObject, SessionObject.KEY_SESSION_DATA);
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
		if (null != sessionInfo) {
			sessionInfo.setUpdateTime(Date.from(Instant.ofEpochMilli(sessionObject.getLastAccess_at())));
		}
		return sessionInfo;
	}

	public List<SessionInfo> findSessionInfoData() {
		Collection<String> keys = cacheFacade.keys();
		return findSessionInfoData(keys);
	}

	public List<SessionInfo> findSessionInfoData(Collection<String> keys) {
		if (Objects.isNull(keys) || keys.isEmpty()) {
			return Collections.emptyList(); // 使用Collections.emptyList()代替List.of()以兼容旧版本Java或避免不必要的对象创建
		}

		if (keys.size() <= PARALLEL_PROCESSING_THRESHOLD) {
			return keys.stream()
					.map(cacheFacade::getSession)
					.filter(Objects::nonNull)
					.map(this::convertToSessionInfoWithUpdateTime)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		}

		List<String> keyList = new ArrayList<>(keys);
		// 优化：使用ForkJoinPool.commonPool()，除非cacheFacade::getSession是I/O密集型操作
		// 如果是I/O密集型，当前创建固定线程池的方式是合适的
		ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors()));
		try {
			int parallelism = Math.max(1, Runtime.getRuntime().availableProcessors());
			int batchSize = (keyList.size() + parallelism - 1) / parallelism;
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
//				logger.warn("SessionObject with id {} contains non-SessionInfo data for KEY_SESSION_DATA. Actual type: {}",
//						sessionObject.getId(), sessionData != null ? sessionData.getClass().getName() : "null");
				return null;
			}
			SessionInfo sessionInfo = (SessionInfo) sessionData;

			// 转换最后访问时间为Date类型，填充到SessionInfo
			long lastAccessTime = sessionObject.getLastAccess_at();
			Date updateTime = Date.from(Instant.ofEpochMilli(lastAccessTime));
			sessionInfo.setUpdateTime(updateTime);

			return sessionInfo;
		} catch (Exception e) {
			// 捕获时间转换/空值等异常，避免单个异常导致整个流终止
			logger.warn("Failed to convert SessionObject to SessionInfo for session id: {}. Error: {}",
					sessionObject != null ? sessionObject.getId() : "unknown", e.getMessage(), e);
			return null;
		}
	}

	public SessionInfo getSessionInfoBySessionInfoId(String sessionInfoId) {
		Collection<String> keys = cacheFacade.keys();
		if (Objects.isNull(keys) || keys.isEmpty()) {
			return null;
		}
		return keys.stream()
				.map(cacheFacade::getSession)
				.filter(Objects::nonNull)
				.map(sessionObject -> {
					Object sessionData = sessionObject.get(SessionObject.KEY_SESSION_DATA);
					return (sessionData instanceof SessionInfo) ? (SessionInfo) sessionData : null;
				})
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

		if (keys.size() <= PARALLEL_PROCESSING_THRESHOLD) {
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
		ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors()));
		try {
			int parallelism = Math.max(1, Runtime.getRuntime().availableProcessors());
			int batchSize = (keyList.size() + parallelism - 1) / parallelism;
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
		if (sessionId == null) {
			return null;
		}
		return cacheFacade.getSession(sessionId);
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

	/**
	 * 获取所有会话键的数量
	 * 优化：直接调用cacheFacade.keys().size()，避免创建中间集合
	 */
	public int findSessionKeySize() {
		Collection<String> keys = cacheFacade.keys();
		return keys != null ? keys.size() : 0;
	}

	/**
	 * 获取所有会话键
	 * 优化：直接返回cacheFacade.keys()，避免创建中间集合
	 */
	public Collection<String> findSessionKeys() {
		return cacheFacade.keys();
	}

}