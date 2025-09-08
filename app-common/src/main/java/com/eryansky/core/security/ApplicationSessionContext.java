package com.eryansky.core.security;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.j2cache.session.CacheFacade;
import com.eryansky.j2cache.session.J2CacheSessionFilter;
import com.eryansky.j2cache.session.SessionObject;
import com.eryansky.utils.CacheUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 应用Session上下文
 */
public class ApplicationSessionContext {

	private CacheFacade cacheFacade;

	/**
	 * 静态内部类，延迟加载，懒汉式，线程安全的单例模式
	 */
	public static final class Static {
		private static final FilterRegistrationBean<J2CacheSessionFilter> filterRegistrationBean = SpringContextHolder.getBean("j2CacheSessionFilter", FilterRegistrationBean.class);
		private static final ApplicationSessionContext instance = new ApplicationSessionContext(filterRegistrationBean.getFilter().getCache());
	}

	private ApplicationSessionContext() {
	}

	public ApplicationSessionContext(CacheFacade cacheFacade) {
		this.cacheFacade = cacheFacade;
	}

	public static ApplicationSessionContext getInstance() {
		return Static.instance;
	}

	public void addSession(SessionInfo sessionInfo) {
		addSession(null,sessionInfo);
	}

	public void addSession(String sessionId,SessionInfo sessionInfo) {
		if (sessionInfo != null) {
			SessionObject sessionObject = cacheFacade.getSession(StringUtils.isNotBlank(sessionId) ? sessionId:sessionInfo.getId());
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
		if (sessionId == null) return null;
		SessionObject sessionObject = cacheFacade.getSession(sessionId);
		return (SessionInfo) sessionObject.get(SessionObject.KEY_SESSION_DATA);
	}

	public List<SessionInfo> findSessionInfoDataRemoveDuplicate() {
		List<SessionInfo> list = findSessionInfoData();
		return list.parallelStream().collect(Collectors.toSet()).parallelStream().collect(Collectors.toList());
	}

	public List<SessionInfo> findSessionInfoData() {
//		Collection<String> keys = findSessionInfoKeys();
		Collection<String> keys = cacheFacade.keys();
		return findSessionInfoData(keys);
	}

	public List<SessionInfo> findSessionInfoData(Collection<String> keys) {
		return keys.parallelStream().map(key -> {
			SessionObject sessionObject = cacheFacade.getSession(key);
			return null != sessionObject ? (SessionInfo) sessionObject.get(SessionObject.KEY_SESSION_DATA) : null;
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public Collection<String> findSessionInfoKeys() {
		Collection<String> keys = cacheFacade.keys();
		return keys.parallelStream().filter(key -> {
			SessionObject sessionObject = cacheFacade.getSession(key);
			return  null != sessionObject && null != sessionObject.get(SessionObject.KEY_SESSION_DATA);
		}).collect(Collectors.toList());
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

	public int findSessionKeySize() {
		Collection<String> keys = cacheFacade.keys();
		return keys.size();
	}

	public Collection<String> findSessionKeys() {
		Collection<String> keys = cacheFacade.keys();
		return keys;
	}


	/**
	 * APP与Webview session同步兼容 添加关联已有sessionId
	 * @param sessionId
	 * @return
	 */
	public void addExtendSessionInfo(String sessionId, String sessionInfoId) {
		SessionObject sessionObject = cacheFacade.getSession(sessionId);
		sessionObject.put(SessionObject.KEY_SESSION_EXTEND,sessionInfoId);
		cacheFacade.setSessionAttribute(sessionObject,SessionObject.KEY_SESSION_EXTEND);
	}

	/**
	 * APP与Webview session同步兼容 添加关联已有sessionId
	 * @param sessionId
	 * @return
	 */
	public void removeExtendSessionInfo(String sessionId) {
		SessionObject sessionObject = cacheFacade.getSession(sessionId);
		if(null != sessionObject){
			sessionObject.remove(SessionObject.KEY_SESSION_EXTEND);
			cacheFacade.removeSessionAttribute(sessionObject,SessionObject.KEY_SESSION_EXTEND);
		}

	}

	/**
	 * APP与Webview session同步兼容 查找关联已有sessionId
	 * @param sessionId
	 * @return
	 */
	public String getExtendSession(String sessionId) {
		SessionObject sessionObject = cacheFacade.getSession(sessionId);
		return null == sessionObject ? null:(String)sessionObject.get(SessionObject.KEY_SESSION_EXTEND);
	}

	/**
	 * APP与Webview session同步兼容 查找所有关联sessionId
	 * @return
	 */
	public Collection<String> findSessionExtendKeys() {
		Collection<String> keys = cacheFacade.keys();
		return keys.parallelStream().filter(key->{
			SessionObject sessionObject = cacheFacade.getSession(key);
			return  null != sessionObject && null != sessionObject.get(SessionObject.KEY_SESSION_EXTEND);
		}).collect(Collectors.toList());
	}



}