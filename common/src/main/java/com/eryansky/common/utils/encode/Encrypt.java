/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.encode;

import com.eryansky.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密工具类.
 * <br>md5加密 32位
 * <br>sha加密 40位
 * @author Eryan
 * @date   2012-12-11 上午10:32:05
 */
public class Encrypt {

	private static final Logger logger = LoggerFactory.getLogger(Encrypt.class);

	/**
	 * MD5加密随机值
	 */
	public static final String SALT = "{1#2$3%4(5)6@7!poeeww$3%4(5)djjkkldss}";

	/**
	 * 测试
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// md5加密测试
		String md5_1 = md5("password");
		System.out.println(md5_1 );
		// sha加密测试
		String sha_1 = sha("123456");
		System.out.println(sha_1);
		System.out.println(e("1"));
		System.out.println(md5("1"));

	}

	/**
	 * 加密
	 * 
	 * @param inputText
	 * @return
	 */
	public static String e(String inputText) {
		return e(inputText,SALT);
	}

	/**
	 * 加密
	 *
	 * @param inputText
	 * @param salt
	 * @return
	 */
	public static String e(String inputText,String salt) {
		return md5(inputText + (salt != null ? salt: StringUtils.EMPTY));
	}

	/**
	 * 二次加密，应该破解不了了吧？
	 * 
	 * @param inputText
	 * @return
	 */
	public static String md5AndSha(String inputText) {
		return sha(md5(inputText));
	}

	/**
	 * md5加密
	 * 
	 * @param inputText
	 * @return
	 */
	public static String md5(String inputText) {
		return encrypt(inputText, "md5");
	}

	/**
	 * sha加密
	 * 
	 * @param inputText
	 * @return
	 */
	public static String sha(String inputText) {
		return encrypt(inputText, "sha-1");
	}

	/**
	 * md5或者sha-1加密
	 * 
	 * @param inputText
	 *            要加密的内容
	 * @param algorithmName
	 *            加密算法名称：md5或者sha-1，不区分大小写
	 * @return
	 */
	private static String encrypt(String inputText, String algorithmName) {
		if (inputText == null || "".equals(inputText.trim())) {
//			throw new IllegalArgumentException("请输入要加密的内容");
		    return "";
		}
		if (algorithmName == null || "".equals(algorithmName.trim())) {
			algorithmName = "md5";
		}
		String encryptText = null;
		try {
			MessageDigest m = MessageDigest.getInstance(algorithmName);
			m.update(inputText.getBytes(StandardCharsets.UTF_8));
			byte[] s = m.digest();
			// m.digest(inputText.getBytes("UTF-8"));
			return hex(s);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(),e);
		}
        return encryptText;
	}

	/**
	 * 返回十六进制字符串
	 * 
	 * @param arr
	 * @return
	 */
	private static String hex(byte[] arr) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arr.length; ++i) {
			sb.append(Integer.toHexString((arr[i] & 0xFF) | 0x100), 1, 3);
		}
		return sb.toString();
	}

}
