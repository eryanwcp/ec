/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.encode;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.web.utils.WebUtils;
import com.google.common.collect.Lists;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 封装各种格式的编码解码工具类.
 * 1.Commons-Codec的 hex/base64 编码
 * 2.自制的base62 编码
 * 3.Commons-Lang的xml/html escape
 * 4.JDK提供的URLEncoder
 * 5、XSS、SQL、orderBy 过滤器
 * 
 * @author Eryan
 */
public class EncodeUtils {

	private static final Logger logger = LoggerFactory.getLogger(EncodeUtils.class);

	private static final String DEFAULT_URL_ENCODING = "UTF-8";
	private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

	/**
	 * Hex编码.
	 */
	public static String hexEncode(byte[] input) {
		return Hex.encodeHexString(input);
	}

	/**
	 * Hex解码.
	 */
	public static byte[] hexDecode(String input) {
		try {
			return Hex.decodeHex(input.toCharArray());
		} catch (DecoderException e) {
			throw new IllegalStateException("Hex Decoder exception", e);
		}
	}

	/**
	 * Base64编码.
	 */
	public static String base64Encode(byte[] input) {
		return new String(Base64.encodeBase64(input));
	}

	/**
	 * Base64编码, URL安全(将Base64中的URL非法字符如+,/=转为其他字符, 见RFC3548).
	 */
	public static String base64UrlSafeEncode(byte[] input) {
		return Base64.encodeBase64URLSafeString(input);
	}

	/**
	 * Base64解码.
	 */
	public static byte[] base64Decode(String input) {
		return Base64.decodeBase64(input);
	}

	/**
	 * Base64解码.
	 */
	public static byte[] base64Decode(byte[] input) {
		return Base64.decodeBase64(input);
	}

	/**
	 * Base62编码。
	 */
	public static String encodeBase62(byte[] input) {
		char[] chars = new char[input.length];
		for (int i = 0; i < input.length; i++) {
			chars[i] = BASE62[((input[i] & 0xFF) % BASE62.length)];
		}
		return new String(chars);
	}
	/**
	 * URL 编码, Encode默认为UTF-8. 
	 */
	public static String urlEncode(String input) {
		if(null == input){
			return null;
		}
		try {
			return URLEncoder.encode(input, DEFAULT_URL_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported Encoding Exception", e);
		}
	}

	/**
	 * URL 解码, Encode默认为UTF-8. 
	 */
	public static String urlDecode(String input) {
		if(null == input){
			return null;
		}
		try {
			return URLDecoder.decode(input, DEFAULT_URL_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported Encoding Exception", e);
		}
	}
	/**
	 * URL 解码（两次）, Encode默认为UTF-8.
	 */
	public static String urlDecode2(String part) {
		return urlDecode(urlDecode(part));
	}

	/**
	 * Html 转码.
	 */
	public static String htmlEscape(String html) {
		return StringEscapeUtils.escapeHtml4(html);
	}

	/**
	 * Html 解码.
	 */
	public static String htmlUnescape(String htmlEscaped) {
		return StringEscapeUtils.unescapeHtml4(htmlEscaped);
	}

	/**
	 * Xml 转码.
	 */
	public static String xml10Escape(String xml) {
		return StringEscapeUtils.escapeXml10(xml);
	}

	/**
	 * Xml 转码.
	 */
	public static String xml11Escape(String xml) {
		return StringEscapeUtils.escapeXml11(xml);
	}

	/**
	 * Xml 解码.
	 */
	public static String xmlUnescape(String xmlEscaped) {
		return StringEscapeUtils.unescapeXml(xmlEscaped);
	}

	// 预编译XSS过滤正则表达式
	private static final List<Pattern> xssPatterns = Lists.newArrayList(
			Pattern.compile("(<\\s*(script|link|style|iframe)([\\s\\S]*?)(>|<\\/\\s*\\1\\s*>))|(</\\s*(script|link|style|iframe)\\s*>)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("\\s*(href|src)\\s*=\\s*(\"\\s*(javascript|vbscript):[^\"]+\"|'\\s*(javascript|vbscript):[^']+'|(javascript|vbscript):[^\\s]+)\\s*(?=>)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("\\s*on[a-z]+\\s*=\\s*(\"[^\"]+\"|'[^']+'|[^\\s]+)\\s*(?=>)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("(eval\\((.*?)\\)|expression\\((.*?)\\))", Pattern.CASE_INSENSITIVE),
			Pattern.compile("^(javascript:|vbscript:)", Pattern.CASE_INSENSITIVE)
	);
	/**
	 * XSS 非法字符过滤，内容以<!--HTML-->开头的用以下规则（保留标签）
	 */
	public static String xssFilter(String text) {
		String oriValue = StringUtils.trim(text);
		if (text != null){
			String value = oriValue;
			for (Pattern pattern : xssPatterns) {
				Matcher matcher = pattern.matcher(value);
				if (matcher.find()) {
					value = matcher.replaceAll(StringUtils.EMPTY);
				}
			}
			// 如果开始不是HTML，XML，JOSN格式，则再进行HTML的 "、<、> 转码。
			if (!StringUtils.startsWithIgnoreCase(value, "<!--HTML-->")    // HTML
					&& !StringUtils.startsWithIgnoreCase(value, "<?xml ")  // XML
					&& !StringUtils.contains(value, "id=\"FormHtml\"") // JFlow
					&& !(StringUtils.startsWith(value, "{") && StringUtils.endsWith(value, "}")) // JSON Object
					&& !(StringUtils.startsWith(value, "[") && StringUtils.endsWith(value, "]")) // JSON Array
			){
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < value.length(); i++) {
					char c = value.charAt(i);
					switch (c) {
						case '>':
							sb.append("＞");
							break;
						case '<':
							sb.append("＜");
							break;
						case '\'':
							sb.append("＇");
							break;
						case '\"':
							sb.append("＂");
							break;
						default:
							sb.append(c);
							break;
					}
				}
				value = sb.toString();
			}
			if (logger.isInfoEnabled() && !value.equals(oriValue)){
				logger.info("xssFilter: {}   <=<=<=   {} ", value, text);
			}
			return value;
		}
		return null;
	}

	// 预编译SQL过滤正则表达式
	private static final Pattern sqlPattern = Pattern.compile(
			"(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|((extractvalue|updatexml|if|mid|database|rand|user)([\\s]*?)\\()"
					+ "|(\\b(select|update|and|or|delete|insert|trancate|substr|ascii|declare|exec|count|master|into"
					+ "|drop|execute|case when|sleep|union|load_file)\\b)", Pattern.CASE_INSENSITIVE);
	private static final Pattern orderByPattern = Pattern.compile("[a-z0-9_\\.\\, ]*", Pattern.CASE_INSENSITIVE);

	/**
	 * SQL过滤，防止注入，传入参数输入有select相关代码，替换空。
	 */
	public static String sqlFilter(String text){
		return sqlFilter(text, "common");
	}

	/**
	 * SQL过滤，防止注入，传入参数输入有select相关代码，替换空。
	 */
	public static String sqlFilter(String text, String source){
		if (text != null){
			String value = text;
			if ("orderBy".equals(source)) {
				Matcher matcher = orderByPattern.matcher(value);
				if (!matcher.matches()) {
					value = StringUtils.EMPTY;
				}
			} else {
				Matcher matcher = sqlPattern.matcher(value);
				if (matcher.find()) {
					value = matcher.replaceAll(StringUtils.EMPTY);
				}
			}
			if (logger.isWarnEnabled() && !value.equals(text)){
				logger.info("sqlFilter: {}   <=<=<=   {}   source: {}", value, text, source);
				return StringUtils.EMPTY;
			}
			return value;
		}
		return null;
	}

}
