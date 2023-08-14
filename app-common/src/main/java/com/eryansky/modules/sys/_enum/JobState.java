package com.eryansky.modules.sys._enum;

import com.eryansky.common.orm._enum.GenericEnumUtils;
import com.eryansky.common.orm._enum.IGenericEnum;

/**
 *  任务状态
 */
public enum JobState implements IGenericEnum<JobState> {

	WAITING("WAITING", "等待"),
	PAUSED("PAUSED", "暂停"),
	ACQUIRED("ACQUIRED", "正常执行"),
	BLOCKED("BLOCKED", "阻塞"),
	ERROR("ERROR", "错误");
	/**
	 * 值 String型
	 */
	private final String value;

	/**
	 * 描述 String型
	 */
	private final String description;

	JobState(String value, String description) {
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


	public static JobState getByValue(String value) {
		return GenericEnumUtils.getByValue(JobState.class,value);
	}

	public static JobState getByDescription(String description) {
		return GenericEnumUtils.getByDescription(JobState.class,description);
	}

}