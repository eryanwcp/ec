/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.orm._enum;

/**
 *
 * 状态标识 枚举类型.
 *
 * @author Eryan
 * @date 2012-8-11 下午10:48:23
 *
 */
public enum StatusState implements IGenericEnum<StatusState> {

	/** 正常(0) */
	NORMAL("0", "正常"),
	/** 已删除(1) */
	DELETE("1", "已删除"),
	/** 待审核(2) */
	AUDIT("2", "待审核"),
	/** 锁定(3) */
	LOCK("3", "锁定"),
	/** 草稿(4) */
	DRAFT("4", "草稿");

	/**
	 * 值 String型
	 */
	private final String value;
	/**
	 * 描述 String型
	 */
	private final String description;

	StatusState(String value, String description) {
		this.value = value;
		this.description = description;
	}

	/**
	 * 获取值
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 获取描述信息
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	public static StatusState getByValue(String value) {
		return GenericEnumUtils.getByValue(StatusState.class,value);
	}

	public static StatusState getByDescription(String description) {
		return GenericEnumUtils.getByDescription(StatusState.class,description);
	}

}