/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.exec;

import com.eryansky.common.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Command
 * @author Eryan
 * @date   2018-11-11
 */
public class CommandUtils {

	public static String execute(String command) throws IOException {
		return execute(command, "UTF-8");
	}
	
	public static String execute(String command, String charsetName) throws IOException {
		return execute(command,charsetName,null);
	}

	/**
	 * 执行操作系统命令
	 * @param command
	 * @param charsetName
	 * @param dir 只运行 dir 包含特定的字符
	 * @return
	 * @throws IOException
	 */
	public static String execute(String command, String charsetName,String dir) throws IOException {
		//只运行 dir 包含特定的字符
		if(StringUtils.isNotBlank(dir) && !Pattern.matches("[0-9A-Za-z@.]+",dir)){
			throw new IllegalArgumentException("未授权执行，危险命令执行:"+command);
		}

		Process process = Runtime.getRuntime().exec(command);
		// 记录dos命令的返回信息
		StringBuilder stringBuffer = new StringBuilder();
		// 获取返回信息的流
		try (InputStream in = process.getInputStream();
			 BufferedReader bReader = new BufferedReader(new InputStreamReader(in, null == charsetName ? StandardCharsets.UTF_8.name():charsetName))){
			String res = bReader.readLine();
			while (res != null) {
				stringBuffer.append(res);
				stringBuffer.append("\n");
				res = bReader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringBuffer.toString();
	}
	
}
