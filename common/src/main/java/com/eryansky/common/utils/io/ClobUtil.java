/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.sql.Clob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialException;

/**
 * Clob工具类.
 * @author Eryan
 * @date   2012-12-11 上午10:32:35
 */
public class ClobUtil {

	private static final Logger logger = LoggerFactory.getLogger(ClobUtil.class);

	/**
	 * 获得字符串
	 * 
	 * @param c
	 *            java.sql.Clob
	 * @return 字符串
	 */
	public static String getString(Clob c) {
		StringBuffer s = new StringBuffer();
		if (c != null) {
			try (BufferedReader bufferRead = new BufferedReader(c.getCharacterStream())){
				String str;
				while ((str = bufferRead.readLine()) != null) {
					s.append(str);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		return s.toString();
	}

	/**
	 * 获得Clob
	 * 
	 * @param s
	 *            字符串
	 * @return java.sql.Clob
	 */
	public static Clob getClob(String s) {
		Clob c = null;
		try {
			if (s != null) {
				c = new SerialClob(s.toCharArray());
			}
		} catch (SerialException e) {
			logger.error(e.getMessage(),e);
		} catch (SQLException e) {
			logger.error(e.getMessage(),e);
		}
		return c;
	}

}
