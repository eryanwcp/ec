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
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 应用Session上下文
 */
public class ApplicationSessionContext {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationSessionContext.class);

	/** 并行处理阈值 */
	private static final int PARALLEL_PROCESSING_THRESHOLD = 1000;

	private static volatile ApplicationSessionContext instance;

	private final J2CacheSessionFilter sessionFilter;
	private final CacheFacade cacheFacade;
	private final ExecutorService executor;

	/**
	 * 双重检查锁（DCL）获取单例，避免类加载阶段死锁或 Spring 未就绪问题
	 */
	public static ApplicationSessionContext getInstance() {
		if (instance == null) {
			synchronized (ApplicationSessionContext.class) {
				if (instance == null) {
					FilterRegistrationBean<?> filterRegistrationBean =
							SpringContextHolder.getBean("j2CacheSessionFilter", FilterRegistrationBean.class);
					if (filterRegistrationBean == null || filterRegistrationBean.getFilter() == null) {
						throw new IllegalStateException("J2CacheSessionFilter bean is not initialized in Spring Context.");
					}
					instance = new ApplicationSessionContext((J2CacheSessionFilter) filterRegistrationBean.getFilter());
				}
			}
		}
		return instance;
	}

	public ApplicationSessionContext(J2CacheSessionFilter sessionFilter) {
		this.sessionFilter = sessionFilter;
		this.cacheFacade = sessionFilter.getCache();

		int cpus = Runtime.getRuntime().availableProcessors();
		// 使用 ThreadPoolExecutor 显式指定有界队列与拒绝策略
		this.executor = new ThreadPoolExecutor(
				Math.max(1, cpus),
				Math.max(2, cpus * 2),
				60L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(2000),
				r -> {
					Thread t = new Thread(r);
					t.setDaemon(true);
					t.setName("session-context-parallel-pool-" + t.getId());
					return t;
				},
				new ThreadPoolExecutor.CallerRunsPolicy()
		);

		// 注册 JVM 优雅关闭钩子
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownExecutor));
	}

	/**
	 * 关闭线程池资源
	 */
	private void shutdownExecutor() {
		if (executor != null && !executor.isShutdown()) {
			executor.shutdown();
			try {
				if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}

	public void addSession(SessionInfo sessionInfo) {
		if (sessionInfo == null || sessionInfo.getSessionId() == null) {
			return;
		}
		SessionObject sessionObject = cacheFacade.getSession(sessionInfo.getSessionId());
		if (sessionObject == null) {
			logger.warn("SessionObject for sessionId {} not found when adding session. This might indicate an issue or a new session.", sessionInfo.getSessionId());
			return;
		}
		sessionObject.put(SessionObject.KEY_SESSION_DATA, sessionInfo);
		cacheFacade.setSessionAttribute(sessionObject, SessionObject.KEY_SESSION_DATA);
	}

	public void removeSessionInfo(String sessionId) {
		if (sessionId == null) {
			return;
		}
		SessionObject sessionObject = cacheFacade.getSession(sessionId);
		if (sessionObject != null) {
			sessionObject.remove(SessionObject.KEY_SESSION_DATA);
			cacheFacade.removeSessionAttribute(sessionObject, SessionObject.KEY_SESSION_DATA);
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
		if (sessionObject == null) {
			return null;
		}
		SessionInfo sessionInfo = (SessionInfo) sessionObject.get(SessionObject.KEY_SESSION_DATA);
		if (sessionInfo != null) {
			sessionInfo.setUpdateTime(Date.from(Instant.ofEpochMilli(sessionObject.getLastAccess_at())));
		}
		return sessionInfo;
	}

	public List<SessionInfo> findSessionInfoData() {
		return findSessionInfoData(cacheFacade.keys());
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

	/**
	 * 根据 SessionInfo ID 查找（优化：支持找到后快速短路返回）
	 */
	public SessionInfo getSessionInfoBySessionInfoId(String sessionInfoId) {
		if (sessionInfoId == null) {
			return null;
		}
		Collection<String> keys = cacheFacade.keys();
		if (keys == null || keys.isEmpty()) {
			return null;
		}

		// 短路查找：并行任务中只要任意批次命中，直接返回并取消其他任务
		if (keys.size() > PARALLEL_PROCESSING_THRESHOLD) {
			List<String> keyList = new ArrayList<>(keys);
			int parallelism = Math.max(1, Runtime.getRuntime().availableProcessors());
			int batchSize = (keyList.size() + parallelism - 1) / parallelism;

			CompletionService<SessionInfo> completionService = new ExecutorCompletionService<>(executor);
			List<Future<SessionInfo>> futures = new ArrayList<>(parallelism);

			for (int i = 0; i < parallelism; i++) {
				int from = i * batchSize;
				if (from >= keyList.size()) break;
				int to = Math.min(from + batchSize, keyList.size());
				List<String> slice = keyList.subList(from, to);

				futures.add(completionService.submit(() -> searchSessionInfoInBatch(slice, sessionInfoId)));
			}

			try {
				for (int i = 0; i < futures.size(); i++) {
					Future<SessionInfo> completedFuture = completionService.take();
					SessionInfo result = completedFuture.get();
					if (result != null) {
						// 取消其余所有正在执行的批次任务
						for (Future<SessionInfo> f : futures) {
							f.cancel(true);
						}
						return result;
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				logger.error("Error during parallel session lookup: {}", e.getMessage(), e);
			}
			return null;
		}

		return searchSessionInfoInBatch(keys, sessionInfoId);
	}

	private SessionInfo searchSessionInfoInBatch(Collection<String> keys, String sessionInfoId) {
		for (String key : keys) {
			if (Thread.currentThread().isInterrupted()) {
				return null;
			}
			SessionObject sessionObject = cacheFacade.getSession(key);
			if (sessionObject != null) {
				Object sessionData = sessionObject.get(SessionObject.KEY_SESSION_DATA);
				if (sessionData instanceof SessionInfo) {
					SessionInfo info = (SessionInfo) sessionData;
					if (sessionInfoId.equals(info.getId())) {
						info.setUpdateTime(Date.from(Instant.ofEpochMilli(sessionObject.getLastAccess_at())));
						return info;
					}
				}
			}
		}
		return null;
	}

	/**
	 * 通用并行执行分批任务
	 */
	public <T, R> List<R> executeParallel(Collection<T> items, Function<List<T>, List<R>> batchTask) {
		if (items == null || items.isEmpty()) {
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

	private SessionInfo convertToSessionInfoWithUpdateTime(SessionObject sessionObject) {
		try {
			if (sessionObject == null) {
				return null;
			}
			Object sessionData = sessionObject.get(SessionObject.KEY_SESSION_DATA);
			if (!(sessionData instanceof SessionInfo)) {
				return null;
			}
			SessionInfo sessionInfo = (SessionInfo) sessionData;
			long lastAccessTime = sessionObject.getLastAccess_at();
			sessionInfo.setUpdateTime(Date.from(Instant.ofEpochMilli(lastAccessTime)));
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