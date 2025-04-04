/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.encode;

import com.eryansky.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * MD5工具类.
 * @author Eryan
 * @date   2012-1-9下午3:15:25
 */
public class MD5Util {

    private static final Logger logger = LoggerFactory.getLogger(MD5Util.class);

     /**
     * 默认的密码字符串组合，用来将字节转换成 16 进制表示的字符,apache校验下载的文件的正确性用的就是默认的这个组合  
     */    
    protected static final char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6','7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    protected static MessageDigest messagedigest = null;    
    static {    
        try {    
            messagedigest = MessageDigest.getInstance("MD5");    
        } catch (NoSuchAlgorithmException e) {    
            logger.error(e.getMessage(),e);
        }    
    }    
    /**
     * 获取文件MD5值
     * @param file
     * @return
     * @throws java.io.IOException
     * @date   2012-1-9下午3:15:43
     */
    public static String getFileMD5String(File file) throws IOException {    
        try(InputStream fis = new FileInputStream(file)){
            byte[] buffer = new byte[1024];
            int numRead = 0;
            while ((numRead = fis.read(buffer)) > 0) {
                messagedigest.update(buffer, 0, numRead);
            }
        }
        return bufferToHex(messagedigest.digest());
    }    
      
    /**
     * 密码字符串MD5加密 32位小写
     * @param str
     * @return
     * @date   2012-1-9下午3:16:04
     */
    public static String getStringMD5(String str){  
    	if(StringUtils.isEmpty(str)){
    		return "";
    	}
         byte[] buffer=str.getBytes();  
         messagedigest.update(buffer);  
        return bufferToHex(messagedigest.digest());  
    }  
    
    public static String bufferToHex(byte[] bytes) {
        return bufferToHex(bytes, 0, bytes.length);    
    }    
    
    private static String bufferToHex(byte[] bytes, int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);    
        int k = m + n;    
        for (int l = m; l < k; l++) {    
            appendHexPair(bytes[l], stringbuffer);    
        }    
        return stringbuffer.toString();    
    }    
    
    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {    
        char c0 = hexDigits[(bt & 0xf0) >> 4];// 取字节中高 4 位的数字转换    
        // 为逻辑右移，将符号位一起右移,此处未发现两种符号有何不同    
        char c1 = hexDigits[bt & 0xf];// 取字节中低 4 位的数字转换    
        stringbuffer.append(c0);    
        stringbuffer.append(c1);    
    }

    private static final String toHex(byte[] hash) {
        if (hash == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer(hash.length * 2);
        int i;

        for (i = 0; i < hash.length; i++) {
            if ((hash[i] & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString(hash[i] & 0xff, 16));
        }
        return buf.toString();
    }

    public static String hash(String s) {
        try {
            return new String(toHex(getStringMD5(s).getBytes(StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    //测试
    public static void main(String[] args) {
		System.out.println(MD5Util.getStringMD5("123456"));//21232f297a57a5a743894a0e4a801fc3
		System.out.println(MD5Util.hash("123456"));//21232f297a57a5a743894a0e4a801fc3
	}
}    