package com.eryansky.core.security;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.j2cache.session.CacheFacade;
import com.eryansky.j2cache.session.J2CacheSessionFilter;
import com.eryansky.j2cache.session.SessionObject;
import com.google.common.collect.Sets;
import org.joda.time.Instant;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.util.*;
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
	 * @param sessionId 会话ID
	 * @param bindSessionId 关联对象ID
	 * @return
	 */
	public void bindSessionInfoId(String sessionId, String bindSessionId) {
		SessionObject sessionObject = cacheFacade.getSession(bindSessionId);
		if(null != sessionObject){
			Set<String> bindIds = (Set<String>) sessionObject.getAttribute(SessionObject.KEY_SESION_ID_BIND);
			if(null == bindIds){
				bindIds = Sets.newHashSet();
			}
			bindIds.add(sessionId);
			sessionObject.setAttribute(SessionObject.KEY_SESION_ID_BIND, bindIds);
			cacheFacade.setSessionAttribute(sessionObject,SessionObject.KEY_SESION_ID_BIND);
		}
	}

	/**
	 * 解除绑定sessionId
	 * @param sessionId
	 * @param bindSessionId
	 * @return
	 */
	public void unBindSessionInfoId(String sessionId, String bindSessionId) {
		SessionObject sessionObject = cacheFacade.getSession(bindSessionId);
		if(null != sessionObject){
			Set<String> bindIds = (Set<String>) sessionObject.getAttribute(SessionObject.KEY_SESION_ID_BIND);
			if(null != bindIds){
				boolean flag = bindIds.remove(sessionId);
				if(flag){
					sessionObject.setAttribute(SessionObject.KEY_SESION_ID_BIND, bindIds);
					cacheFacade.setSessionAttribute(sessionObject,SessionObject.KEY_SESION_ID_BIND);
				}
			}

		}
	}


	/**
	 * 获取绑定的sessionId
	 * @param sessionId
	 * @return
	 */
	public String getBindSessionId(String sessionId) {
		return findSessionKeys().parallelStream().filter(key -> {
			SessionObject sessionObject = cacheFacade.getSession(key);
			Set<String> bindIds = null != sessionObject ? (Set<String>) sessionObject.get(SessionObject.KEY_SESION_ID_BIND) : null;
            return null != bindIds && bindIds.contains(sessionId);
        }).findFirst().orElse(null);
	}


}