/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.orm._enum;

/**
 *
 * 枚举接口.
 *
 * @author Eryan
 * @version 2020-02-12
 *
 */
public interface IGenericEnum<T extends Enum<T>>{

	/**
	 * 获取值
	 * @return value
	 */
	String getValue();

	/**
	 * 获取描述信息
	 * @return description
	 */
	String getDescription();

}