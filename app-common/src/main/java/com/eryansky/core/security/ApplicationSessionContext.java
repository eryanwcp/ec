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
import java.util.function.Function;
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
	private final ExecutorService executor;

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
		this.executor = null; // Not used in this constructor
	}

	public ApplicationSessionContext(J2CacheSessionFilter sessionFilter) {
		this.sessionFilter = sessionFilter;
		this.cacheFacade = sessionFilter.getCache();
		this.executor = Executors.newFixedThreadPool(
				Math.max(1, Runtime.getRuntime().availableProcessors()),
				r -> {
					Thread t = new Thread(r);
					t.setDaemon(true);
					t.setName("session-context-parallel-pool-" + t.getId());
					return t;
				});
	}

	public static ApplicationSessionContext getInstance() {
		return Static.instance;
	}

	public void addSession(SessionInfo sessionInfo) {
		if (sessionInfo != null) {
			SessionObject sessionObject = cacheFacade.getSession(sessionInfo.getSessionId());
			if (sessionObject == null) {
				logger.warn("SessionObject for sessionId {} not found when adding session. This might indicate an issueT or a new session.", sessionInfo.getSessionId());
				return;
			}
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
		return executeParallel(keys, batch -> batch.stream()
				.map(cacheFacade::getSession)
				.filter(Objects::nonNull)
				.map(this::convertToSessionInfoWithUpdateTime)
				.filter(Objects::nonNull)
				.collect(Collectors.toList()));
	}

	public Collection<String> findSessionInfoKeys() {
		Collection<String> keys = cacheFacade.keys();
		return executeParallel(keys, batch -> {
			List<String> sub = new ArrayList<>();
			for (String key : batch) {
				SessionObject sessionObject = cacheFacade.getSession(key);
				if (sessionObject != null && sessionObject.get(SessionObject.KEY_SESSION_DATA) != null) {
					sub.add(key);
				}
			}
			return sub;
		});
	}

	public int findSessionInfoKeySize() {
		return findSessionInfoKeys().size();
	}

	public SessionInfo getSessionInfoBySessionInfoId(String sessionInfoId) {
		Collection<String> keys = cacheFacade.keys();
		List<SessionInfo> results = executeParallel(keys, batch -> batch.stream()
				.map(cacheFacade::getSession)
				.filter(Objects::nonNull)
				.map(sessionObject -> {
					Object sessionData = sessionObject.get(SessionObject.KEY_SESSION_DATA);
					return (sessionData instanceof SessionInfo) ? (SessionInfo) sessionData : null;
				})
				.filter(Objects::nonNull)
				.filter(sessionInfo -> sessionInfoId.equals(sessionInfo.getId()))
				.collect(Collectors.toList()));
		return results.isEmpty() ? null : results.get(0);
	}

	/**
	 * 并行执行处理任务
	 * @param items 输入项集合
	 * @param batchTask 处理批次的函数
	 * @return 结果列表
	 */
	public <T, R> List<R> executeParallel(Collection<T> items, Function<List<T>, List<R>> batchTask) {
		if (Objects.isNull(items) || items.isEmpty()) {
			return Collections.emptyList();
		}

		if (items.size() <= PARALLEL_PROCESSING_THRESHOLD) {
			return batchTask.apply(new ArrayList<>(items));
		}

		List<T> itemList = new ArrayList<>(items);
		int parallelism = Math.max(1, Runtime.getRuntime().availableProcessors());
		int batchSize = (itemList.size() + parallelism - 1) / parallelism;
		List<CompletableFuture<List<R>>> futures = new ArrayList<>(parallelism);

		for (int i = 0; i < parallelism; i++) {
			int from = i * batchSize;
			if (from >= itemList.size()) break;
			int to = Math.min(from + batchSize, itemList.size());
			List<T> slice = itemList.subList(from, to);
			futures.add(CompletableFuture.supplyAsync(() -> batchTask.apply(slice), executor));
		}

		return futures.stream()
				.map(CompletableFuture::join)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	/**
	 * 将SessionObject转换为SessionInfo，并设置更新时间
	 * @param sessionObject 缓存中的会话对象
	 * @return 填充了更新时间的SessionInfo（转换失败返回null）
	 */
	private SessionInfo convertToSessionInfoWithUpdateTime(SessionObject sessionObject) {
		try {
			Object sessionData = sessionObject.get(SessionObject.KEY_SESSION_DATA);
			if (!(sessionData instanceof SessionInfo)) {
				return null;
			}
			SessionInfo sessionInfo = (SessionInfo) sessionData;
			long lastAccessTime = sessionObject.getLastAccess_at();
			Date updateTime = Date.from(Instant.ofEpochMilli(lastAccessTime));
			sessionInfo.setUpdateTime(updateTime);
			return sessionInfo;
		} catch (Exception e) {
			logger.warn("Failed to convert SessionObject to SessionInfo for session id: {}. Error: {}",
					sessionObject != null ? sessionObject.getId() : "unknown", e.getMessage(), e);
			return null;
		}
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
