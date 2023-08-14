/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *          Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.cms;

import com.ckfinder.connector.ConnectorServlet;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.utils.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * CKFinderConnectorServlet
 * @author Eryan
 * @version 2013-01-15
 */
public class CKFinderConnectorServlet extends ConnectorServlet {

	private static final Logger logger = LoggerFactory.getLogger(CKFinderConnectorServlet.class);

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
		prepareGetResponse(request, response, false);
		super.doGet(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
		prepareGetResponse(request, response, true);
		super.doPost(request, response);
	}

	private void prepareGetResponse(final HttpServletRequest request,
                                    final HttpServletResponse response, final boolean post) throws ServletException, IOException {
		String command = request.getParameter("command");
		String type = request.getParameter("type");
		SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo(request);
		// 初始化时，如果startupPath文件夹不存在，则自动创建startupPath文件夹
		if ("Init".equals(command)){
			if (sessionInfo!=null){
				String startupPath = request.getParameter("startupPath");// 当前文件夹可指定为模块名
				if (startupPath!=null){
					String[] ss = startupPath.split(":");
					if (ss.length==2){
						String baseDir = AppConstants.getAppBasePath()+"/cms"+ CKFinderConfig.CK_BASH_URL+sessionInfo.getUserId()+"/";
						File file = new File(baseDir,ss[0]+ss[1]);
						if (file.getCanonicalPath().startsWith(baseDir)) {
							if (!file.exists()) {
								boolean flag = file.mkdir();
								flag = file.mkdirs();
								logger.debug("{}:{}",command,flag);
							}
						}else{
							logger.warn("危险注入：{}",file.getAbsolutePath());
						}
					}
				}
			}
		}
		// 快捷上传，自动创建当前文件夹，并上传到该路径
		else if ("QuickUpload".equals(command) && type!=null){
			if (sessionInfo!=null){
				String currentFolder = request.getParameter("currentFolder");// 当前文件夹可指定为模块名
				String baseDir = AppConstants.getAppBasePath()+"/cms"+ CKFinderConfig.CK_BASH_URL+sessionInfo.getUserId()+"/";
				File file = new File(baseDir,type + (currentFolder!=null?currentFolder:""));
				if (file.getCanonicalPath().startsWith(baseDir)) {
					if (!file.exists()) {
						boolean flag = file.mkdir();
						flag = file.mkdirs();
						logger.debug("{}:{}",command,flag);
					}
				}else{
					logger.warn("危险注入：{}",file.getAbsolutePath());
				}

			}
		}
	}

}
