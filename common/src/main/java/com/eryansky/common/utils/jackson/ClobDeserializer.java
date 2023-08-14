/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.jackson;

import com.eryansky.common.utils.io.ClobUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Clob;

/**
 * 自定义Jackson Clob类型转换.
 * @author Eryan
 * @date 2020-02-12
 *
 */
public class ClobDeserializer extends JsonDeserializer<Clob> {

	private static final Logger logger = LoggerFactory.getLogger(ClobDeserializer.class);

	@Override
	public Clob deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String value = p.getText();
		try {
			return value == null ? null : ClobUtil.getClob(value);
		} catch (NumberFormatException e) {
			logger.error("解析Clob错误", e);
			return null;
		}
	}
}