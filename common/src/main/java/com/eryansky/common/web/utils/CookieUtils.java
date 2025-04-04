/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.web.utils;

import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.EncodeUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cookie工具类
 * @author Eryan
 * @version 2013-01-15
 */
public class CookieUtils {

	/**
	 * 设置 Cookie（生成时间为1天）
	 * @param name 名称
	 * @param value 值
	 */
	public static void setCookie(HttpServletResponse response, String name, String value) {
		setCookie(response, name, value, 60*60*24,false);
	}
	
	/**
	 * 设置 Cookie
	 * @param name 名称
	 * @param value 值
	 * @param maxAge 生存时间（单位秒）
	 */
	public static void setCookie(HttpServletResponse response, String name, String value, int maxAge,boolean secure) {
		Cookie cookie = new Cookie(EncodeUtils.urlEncode(name), EncodeUtils.urlEncode(value));
        if(StringUtils.isNotBlank(SpringContextHolder.getApplicationContext().getApplicationName())){
            cookie.setPath(SpringContextHolder.getApplicationContext().getApplicationName());
        }else{
            cookie.setPath("/");
        }
		cookie.setMaxAge(maxAge);
        cookie.setSecure(secure);
		response.addCookie(cookie);
	}
	/**
	 * 获得指定Cookie的值
	 * @param name 名称
	 * @return 值
	 */
	public static String getCookie(HttpServletRequest request, String name) {
		return getCookie(request, null, name, false);
	}

	/**
	 * 获得指定Cookie的值，并删除。
	 * @param name 名称
	 * @return 值
	 */
	public static String getCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		return getCookie(request, response, name, false);
	}

	/**
	 * 获得指定Cookie的值
	 * @param request 请求对象
	 * @param response 响应对象
	 * @param name 名字
	 * @param isRemove 是否移除
	 * @return 值
	 */
	public static String getCookie(HttpServletRequest request, HttpServletResponse response, String name, boolean isRemove) {
		String ctxPath =  request != null ? request.getContextPath() : StringUtils.EMPTY;
		return getCookie(request, response, name, ctxPath, isRemove);
	}

	/**
	 * 获得指定Cookie的值
	 * @param request 请求对象
	 * @param response 响应对象
	 * @param name 名字
	 * @param isRemove 是否移除
	 * @return 值
	 */
	public static String getCookie(HttpServletRequest request, HttpServletResponse response, String name, String path, boolean isRemove) {
		String value = null;
		if (StringUtils.isNotBlank(name)){
			name = EncodeUtils.urlEncode(name);
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(name)) {
						value = EncodeUtils.urlDecode(cookie.getValue());
						value = EncodeUtils.xssFilter(value);
						if (isRemove && response != null) {
							cookie.setPath(path);
							cookie.setMaxAge(0);
							response.addCookie(cookie);
						}
					}
				}
			}
		}
		return value;
	}
}
