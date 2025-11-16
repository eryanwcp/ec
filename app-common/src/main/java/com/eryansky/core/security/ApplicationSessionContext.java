package com.eryansky.core.security;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.j2cache.session.CacheFacade;
import com.eryansky.j2cache.session.J2CacheSessionFilter;
import com.eryansky.j2cache.session.SessionObject;
import com.eryansky.utils.CacheUtils;
import org.joda.time.Instant;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用Session上下文
 */
public class ApplicationSessionContext {

	private static final String CACHE_SESSION_ID_BIND = "session_id_bind";

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
		if (sessionId == null) return null;
		SessionObject sessionObject = cacheFacade.getSession(sessionId);
        SessionInfo sessionInfo = null != sessionObject ? (SessionInfo) sessionObject.get(SessionObject.KEY_SESSION_DATA) : null;
        if(null != sessionInfo){
            sessionInfo.setUpdateTime(Instant.ofEpochMilli(sessionObject.getLastAccess_at()).toDate());
        }
        return sessionInfo;
	}

	public List<SessionInfo> findSessionInfoData() {
//		Collection<String> keys = findSessionInfoKeys();
		Collection<String> keys = cacheFacade.keys();
		return findSessionInfoData(keys);
	}

	public List<SessionInfo> findSessionInfoData(Collection<String> keys) {
		return keys.parallelStream().map(key -> {
			SessionObject sessionObject = cacheFacade.getSession(key);
            SessionInfo sessionInfo = null != sessionObject ? (SessionInfo) sessionObject.get(SessionObject.KEY_SESSION_DATA) : null;
            if(null != sessionInfo){
                sessionInfo.setUpdateTime(Instant.ofEpochMilli(sessionObject.getLastAccess_at()).toDate());
            }
            return sessionInfo;
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
	 * 绑定sessionInfoId 与 sessionId
	 * @param sessionInfoId
	 * @param sessionId
	 * @return
	 */
	public void bindSessionInfoId(String sessionInfoId, String sessionId) {
		CacheUtils.put(CACHE_SESSION_ID_BIND,sessionInfoId,sessionId);
	}

	/**
	 * 解除绑定sessionInfoId 与 sessionId
	 * @param sessionInfoId
	 * @return
	 */
	public void unBindSessionInfoId(String sessionInfoId) {
		CacheUtils.remove(CACHE_SESSION_ID_BIND,sessionInfoId);
	}


	/**
	 * 获取sessionInfoId 绑定的sessionId
	 * @param sessionInfoId
	 * @return
	 */
	public String getbindSessionId(String sessionInfoId) {
		String sessionId = CacheUtils.get(CACHE_SESSION_ID_BIND,sessionInfoId);
		return null != sessionId ? sessionInfoId:sessionInfoId;
	}




}