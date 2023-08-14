/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils;

import com.eryansky.common.utils.encode.EncodeUtils;
import com.eryansky.common.utils.id.SnowFlake;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * 封装各种生成唯一性ID算法的工具类.
 * 
 * @author Eryan
 */
public class Identities {

	private static final SecureRandom random = new SecureRandom();
	private static final SnowFlake snowFlake = SnowFlake.getInstance();

	private Identities() {
	}

	/**
	 * 封装JDK自带的UUID, 通过Random数字生成,中间有-分割
	 */
	public static String uuid() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 封装JDK自带的UUID, 通过Random数字生成,中间无-分割
	 */
	public static String uuid2() {
		return uuid().replaceAll("-", "");
	}

	/**
	 * 基于雪花算法
	 * @return
	 */
	public static Long uuid3() {
		return snowFlake.nextId();
	}

	/**
	 * 基于雪花算法
	 * @return
	 */
	public static String uuid4() {
		return String.valueOf(uuid3());
	}

	/**
	 * 使用SecureRandom随机生成Long. 
	 */
	public static long randomLong() {
		return random.nextLong();
	}

	/**
	 * 基于Base62编码的SecureRandom随机生成bytes.
	 */
	public static String randomBase62(int length) {
		byte[] randomBytes = new byte[length];
		random.nextBytes(randomBytes);
		return EncodeUtils.encodeBase62(randomBytes);
	}

	/**
	 * 使用SecureRandom随机生成指定范围的Integer.
	 */
	public static int randomInt(int min, int max) {
		return random.nextInt(max) % (max - min + 1) + min;
	}

	/**
	 * 获取新代码编号
	 */
	public static String nextCode(String code){
		if (code != null){
			String str = code.trim();
			int lastNotNumIndex = -1;
			int len = str.length() - 1;
			for (int i = len; i >= 0; i--) {
				if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
					lastNotNumIndex = i;
				}else{
					break;
				}
			}
			String prefix = str;
			String prevNum = "000";
			if (lastNotNumIndex != -1){
				prefix = str.substring(0, lastNotNumIndex);
				prevNum = str.substring(lastNotNumIndex);
			}
			String nextNum = new BigDecimal(prevNum).add(BigDecimal.ONE).toString();
			str = prefix + StringUtils.leftPad(nextNum, prevNum.length(), "0");
			return str;
		}
		return null;
	}

	/**
	 * 基于Base62编码的随机生成Long.
	 */
	/*public static String randomBase62() {
		return Encodes.encodeBase62(random.nextLong());
	}*/
	
	public static void main(String[] args) {
		for(int i=0;i<1000;i++){
			System.out.println(randomLong());
		}
		System.out.println(nextCode("A00001"));
	}
}
