/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *          Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.cms;

import com.ckfinder.connector.configuration.Configuration;
import com.ckfinder.connector.data.AccessControlLevel;
import com.ckfinder.connector.utils.AccessControlUtil;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.utils.AppConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

/**
 * CKFinder配置
 * @author Eryan
 * @version 2013-01-15
 */
public class CKFinderConfig extends Configuration {

	public static final String CK_BASH_URL = "/userfiles/";
//	public static final String SESSION_ID = "JSESSIONID";
	public static final String SESSION_ID = "J2CACHE_SESSION_ID";

	public CKFinderConfig(ServletConfig servletConfig) {
        super(servletConfig);  
    }
	
	@Override
    protected Configuration createConfigurationInstance() {
		SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
		if(sessionInfo == null){
//			String _sessionId = SpringMVCHolder.getRequest().getRequestedSessionId() == null ? CookieUtils.getCookie(SpringMVCHolder.getRequest(),SESSION_ID): SpringMVCHolder.getRequest().getRequestedSessionId();
			String _sessionId = CookieUtils.getCookie(SpringMVCHolder.getRequest(),SESSION_ID);
			sessionInfo = SecurityUtils.getSessionInfo(StringUtils.substringBefore(_sessionId,"."));
			//更新真实的SessionID
			if (sessionInfo != null && _sessionId != null && !sessionInfo.getSessionId().equals(_sessionId)) {
				sessionInfo.setId(_sessionId);
				sessionInfo.setSessionId(_sessionId);
				SecurityUtils.refreshSessionInfo(sessionInfo);
			}
		}

		boolean isView = null != sessionInfo && SecurityUtils.isPermitted(sessionInfo.getUserId(), "cms:ckfinder:view");
		boolean isUpload = null != sessionInfo && SecurityUtils.isPermitted(sessionInfo.getUserId(),"cms:ckfinder:upload");
		boolean isEdit = null != sessionInfo && SecurityUtils.isPermitted(sessionInfo.getUserId(),"cms:ckfinder:edit");
		AccessControlLevel alc = this.getAccessConrolLevels().get(0);
		alc.setFolderView(isView);
		alc.setFolderCreate(isEdit);
		alc.setFolderRename(isEdit);
		alc.setFolderDelete(isEdit);
		alc.setFileView(isView);
		alc.setFileUpload(isUpload);
		alc.setFileRename(isEdit);
		alc.setFileDelete(isEdit);
		AccessControlUtil.getInstance(this).loadACLConfig();

		try {
			this.baseDir = AppConstants.getAppBasePath()+"/cms"+CK_BASH_URL+
					(null == sessionInfo ? "temp":sessionInfo.getUserId())+"/";
			this.baseURL = SpringMVCHolder.getRequest().getContextPath()+ CK_BASH_URL +
					(null == sessionInfo ? "temp":sessionInfo.getUserId())+"/";
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return new CKFinderConfig(this.servletConf);
    }

    @Override
    public boolean checkAuthentication(final HttpServletRequest request) {
		SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo(request);
        return sessionInfo != null;
    }

}
