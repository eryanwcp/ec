/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.model;

import com.eryansky.common.utils.mapper.JsonMapper;

import java.io.Serializable;

/**
 * 请求响应结果模型.
 * 
 * @author Eryan
 * @date 2020-07-21
 */
@SuppressWarnings("serial")
public class Result implements Serializable {

	/**
	 * 成功
	 */
	public static final int SUCCESS = 1;
	/**
	 * 警告
	 */
	public static final int WARN = 2;
	/**
	 * 失败
	 */
	public static final int ERROR = 0;

	/**
	 * API 成功
	 */
	public static final int SUCCESS_API = 200;
	/**
	 * API 失败
	 */
	public static final int ERROR_API = 500;
	/**
	 * 未授权
	 */
	public static final int NO_PERMISSION = 403;

	/**
	 * 成功消息
	 */
	public static final String SUCCESS_MSG = "操作成功！";
	/**
	 * 警告消息
	 */
	public static final String WARN_MSG = "警告信息！";
	/**
	 * 失败消息
	 */
	public static final String ERROR_MSG = "操作失败:发生未知异常！";
	/**
	 * 未授权
	 */
	public static final String NO_PERMISSION_MSG = "未授权！";

	/**
	 * 结果状态码(可自定义结果状态码) 1:操作成功 0:操作失败
	 */
	private int code;
	/**
	 * 响应结果描述
	 */
	private String msg;
	/**
	 * 数据
	 */
	@Deprecated
	private Object obj;
	/**
	 * 数据
	 */
	private Object data;

	public Result() {
		super();
	}

	/**
	 * 
	 * @param code
	 *            结果状态码(可自定义结果状态码或者使用内部静态变量 1:操作成功 0:操作失败 2:警告)
	 * @param msg
	 *            响应结果描述
	 * @param obj
	 *            其它数据信息（比如跳转地址）
	 */
	public Result(int code, String msg, Object obj) {
		super();
		this.code = code;
		this.msg = msg;
		this.obj = obj;
	}

	/**
	 * 默认操作成功结果.
	 */
	public static Result successResult() {
		return new Result(SUCCESS, SUCCESS_MSG, null);
	}

	/**
	 * 默认操作警告结果.
	 */
	public static Result warnResult() {
		return new Result(WARN, WARN_MSG, null);
	}


	/**
	 * 默认操作失败结果.
	 */
	public static Result errorResult() {
		return new Result(ERROR, ERROR_MSG, null);
	}


	/**
	 * API 默认操作成功结果.
	 */
	public static Result successApiResult() {
		return new Result(SUCCESS_API, SUCCESS_MSG, null);
	}

	/**
	 * API 默认操作失败结果.
	 */
	public static Result errorApiResult() {
		return new Result(ERROR_API, ERROR_MSG, null);
	}

	/**
	 * 未授权结果.
	 */
	public static Result noPermissionResult() {
		return new Result(NO_PERMISSION, NO_PERMISSION_MSG, null);
	}

	/**
	 * 结果状态码(可自定义结果状态码) 1:操作成功 0:操作失败
	 */
	public int getCode() {
		return code;
	}

	/**
	 * 设置结果状态码(约定 1:操作成功 0:操作失败 2:警告)
	 * 
	 * @param code
	 *            结果状态码
	 */
	public Result setCode(int code) {
		this.code = code;
        return this;
	}

	/**
	 * 响应结果描述
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * 设置响应结果描述
	 * 
	 * @param msg
	 *            响应结果描述
	 */
	public Result setMsg(String msg) {
		this.msg = msg;
        return this;
	}

	/**
	 * 其它数据信息（比如跳转地址）
	 */
	public Object getObj() {
		return obj;
	}

	/**
	 * 设置数据
	 * 
	 * @param obj
	 *            数据
	 */
	public Result setObj(Object obj) {
		this.obj = obj;
        return this;
	}

	/**
	 * 其它数据信息
	 */
	public Object getData() {
		return data;
	}

	/**
	 * 设置其它数据信息
	 *
	 * @param data 数据
	 */
	public Result setData(Object data) {
		this.data = data;
		return this;
	}

	@SuppressWarnings("static-access")
	@Override
	public String toString() {
		return new JsonMapper().nonDefaultMapper().toJson(this);
	}
}
