/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.encode;

import com.eryansky.common.utils.collections.Collections3;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;

/**
 * DES对称加密/解密工具类.
 * <br>加密加密字符串采用DEFAULT_CHARSET编码（UTF-8）.
 *
 * @author Eryan
 * @date 2022-01-26
 */
public class EncryptionSafe {
    /**
     * 密钥 16位长度
     */
    public static final String DEFAULT_KEY = "0123456789~!@#$%";
    /**
     * 编码方式 默认:UTF-8
     */
    public static final String DEFAULT_CHARSET = "UTF-8";
    private static Byte[] iv;
    static {
        SecureRandom random = new SecureRandom();
        byte[] bytesIV = new byte[16];
        random.nextBytes(bytesIV);
//        iv = bytesIV;
        iv = Collections3.toObjects(DEFAULT_KEY.getBytes());//TODO
    }

    /**
     * 将byte数组转换为表示16进制值的字符串， 如：byte[]{8,18}转换为：0813， 和public static byte[]
     * hexStr2ByteArr(String strIn) 互为可逆的转换过程
     *
     * @param arrB 需要转换的byte数组
     * @return 转换后的字符串
     * @throws Exception 本方法不处理任何异常，所有异常全部抛出
     */
    public static String byteArr2HexStr(byte[] arrB) {
        int iLen = arrB.length;
        // 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍
        StringBuilder sb = new StringBuilder(iLen * 2);
        for (int i = 0; i < iLen; i++) {
            int intTmp = arrB[i];
            // 把负数转换为正数
            while (intTmp < 0) {
                intTmp = intTmp + 256;
            }
            // 小于0F的数需要在前面补0
            if (intTmp < 16) {
                sb.append("0");
            }
            sb.append(Integer.toString(intTmp, 16));
        }
        return sb.toString();
    }

    /**
     * 将表示16进制值的字符串转换为byte数组， 和public static String byteArr2HexStr(byte[] arrB)
     * 互为可逆的转换过程
     *
     * @param strIn 需要转换的字符串
     * @return 转换后的byte数组
     * @throws Exception 本方法不处理任何异常，所有异常全部抛出
     */
    public static byte[] hexStr2ByteArr(String strIn) {
        byte[] arrB = strIn.getBytes();
        int iLen = arrB.length;

        // 两个字符表示一个字节，所以字节数组长度是字符串长度除以2
        byte[] arrOut = new byte[iLen / 2];
        for (int i = 0; i < iLen; i = i + 2) {
            String strTmp = new String(arrB, i, 2);
            arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
        }
        return arrOut;
    }

    /**
     * 加密字节数组
     *
     * @param arrB 需加密的字节数组
     * @return 加密后的字节数组
     * @throws Exception
     */
    public static byte[] encrypt(byte[] arrB) throws Exception {
        return encrypt(arrB, DEFAULT_KEY);
    }

    /**
     * 加密字节数组
     *
     * @param arrB   需加密的字节数组
     * @param ketStr 密钥
     * @return 加密后的字节数组
     * @throws Exception
     */
    public static byte[] encrypt(byte[] arrB, String ketStr) throws Exception {
        return Cryptos.aesEncrypt(arrB,ketStr.getBytes(),iv);
    }

    /**
     * 加密字符串 字符串采用默认编码DEFAULT_CHARSET（UTF-8）
     *
     * @param strIn 需加密的字符串
     * @return 加密后的字符串
     * @throws Exception
     */
    public static String encrypt(String strIn) throws Exception {
        return byteArr2HexStr(encrypt(strIn.getBytes(DEFAULT_CHARSET),DEFAULT_KEY));
    }


    /**
     * 加密字符串 字符串采用默认编码DEFAULT_CHARSET（UTF-8）
     *
     * @param strIn  需加密的字符串
     * @param strKey 需加密的密钥
     * @return 加密后的字符串
     * @throws Exception
     */
    public static String encrypt(String strIn, String strKey) throws Exception {
        return byteArr2HexStr(encrypt(strIn.getBytes(DEFAULT_CHARSET), strKey));
    }

    /**
     * 解密字节数组
     *
     * @param arrB 需解密的字节数组
     * @return 解密后的字节数组
     * @throws Exception
     */
    public static byte[] decrypt(byte[] arrB) throws Exception {
        return decrypt(arrB, DEFAULT_KEY);
    }

    /**
     * 解密字节数组
     *
     * @param arrB   需解密的字节数组
     * @param keyStr 密钥
     * @return 解密后的字节数组
     * @throws Exception
     */
    public static byte[] decrypt(byte[] arrB, String keyStr) throws Exception {
        return Cryptos.aes(arrB,keyStr.getBytes(),Cipher.DECRYPT_MODE);
    }

    /**
     * 解密字符串 字符串采用默认编码DEFAULT_CHARSET（UTF-8）
     *
     * @param strIn 需解密的字符串
     * @return 解密后的字符串
     * @throws Exception
     */
    public static String decrypt(String strIn) throws Exception {
        return new String(decrypt(hexStr2ByteArr(strIn),DEFAULT_KEY), DEFAULT_CHARSET);
    }

    /**
     * 解密字符串 字符串采用默认编码DEFAULT_CHARSET（UTF-8）
     *
     * @param strIn  需解密的字符串
     * @param strKey 需解密的密钥
     * @return 解密后的字符串
     * @throws Exception
     */
    public static String decrypt(String strIn, String strKey) throws Exception {
        return new String(decrypt(hexStr2ByteArr(strIn), strKey), DEFAULT_CHARSET);
    }


    // 测试
    public static void main(String[] args) throws Exception {
        String str = "中国";
        String d = EncryptionSafe.encrypt(str);//加密字符串

        String e = EncryptionSafe.decrypt(d);//解密字符串
        String e2 = EncryptionSafe.decrypt("a3722f749058de4fcec3fae61af54122b7abb60489ac");//解密字符串

        System.out.println("Encrypt:" + d + " 字节大小：" + d.getBytes().length);
        System.out.println("Dncrypt:" + e + " 字节大小：" + e.getBytes().length);
        System.out.println("Dncrypt:" + e2 + " 字节大小：" + e2.getBytes().length);
    }
}
