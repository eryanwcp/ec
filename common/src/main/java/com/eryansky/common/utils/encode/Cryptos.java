/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.common.utils.encode;

import com.eryansky.common.utils.Exceptions;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

/**
 * 支持AES对称加密的工具类.
 *
 * @author Eryan
 */
public class Cryptos {

    private static final String AES = "AES";
    private static final String AES_CBC = "AES/GCM/NoPadding";
    private static final String AES_ECB = "AES/ECB/PKCS5Padding";
    private static final String HMACSHA1 = "HmacSHA1";

    private static final int DEFAULT_HMACSHA1_KEYSIZE = 160; //RFC2401
    private static final int DEFAULT_AES_KEYSIZE = 128;
    private static final int DEFAULT_IVSIZE = 16;

    private static final SecureRandom random;
    /**
     * 初始向量IV, 初始向量IV的长度规定为128位16个字节, 初始向量的来源为随机生成.
     */
    private static GCMParameterSpec gcMParameterSpec;
    static {
        random = new SecureRandom();
        byte[] bytesIV = new byte[16];
        random.nextBytes(bytesIV);
        gcMParameterSpec = new GCMParameterSpec(128, bytesIV);
        java.security.Security.setProperty("crypto.policy", "unlimited");
    }

    private Cryptos(){}

    //-- HMAC-SHA1 funciton --//
    /**
     * 使用HMAC-SHA1进行消息签名, 返回字节数组,长度为20字节.
     *
     * @param input 原始输入字符数组
     * @param key HMAC-SHA1密钥
     */
    public static byte[] hmacSha1(byte[] input, byte[] key) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, HMACSHA1);
            Mac mac = Mac.getInstance(HMACSHA1);
            mac.init(secretKey);
            return mac.doFinal(input);
        } catch (GeneralSecurityException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 校验HMAC-SHA1签名是否正确.
     *
     * @param expected 已存在的签名
     * @param input 原始输入字符串
     * @param key 密钥
     */
    public static boolean isMacValid(byte[] expected, byte[] input, byte[] key) {
        byte[] actual = hmacSha1(input, key);
        return Arrays.equals(expected, actual);
    }

    /**
     * 生成HMAC-SHA1密钥,返回字节数组,长度为160位(20字节).
     * HMAC-SHA1算法对密钥无特殊要求, RFC2401建议最少长度为160位(20字节).
     */
    public static byte[] generateHmacSha1Key() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(HMACSHA1);
            keyGenerator.init(DEFAULT_HMACSHA1_KEYSIZE);
            SecretKey secretKey = keyGenerator.generateKey();
            return secretKey.getEncoded();
        } catch (GeneralSecurityException e) {
            throw Exceptions.unchecked(e);
        }
    }

    //-- AES funciton --//
    /**
     * 初始化秘钥
     * @return: byte
     */
    public static byte[] initAESKey() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyGenerator kg = KeyGenerator.getInstance(AES);
        kg.init(128);
        SecretKey secretKey = kg.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * @description: 生成秘钥
     * @return: String
     */
    public static String getBase64EncodeKey() throws Exception {
        byte[] keys = initAESKey();
        return EncodeUtils.base64Encode(keys);
    }

    /**
     * @description: 秘钥转换
     * @return: String
     */
    public static byte[] getBase64DecodeKey(String base64) {
        byte[] returnValue = null;
        if (StringUtils.isNotEmpty(base64)) {
            returnValue = EncodeUtils.base64Decode(base64);
        }

        return returnValue;
    }

    /**
     * 使用AES加密原始字符串.
     *
     * @param input 原始输入字符数组
     * @param key 符合AES要求的密钥
     */
    public static byte[] aesEncrypt(byte[] input, byte[] key) {
        return aes(input, key, Cipher.ENCRYPT_MODE);
    }

    /**
     * 使用AES加密原始字符串.
     *
     * @param input 原始输入字符数组
     * @param key 符合AES要求的密钥
     */
    public static byte[] aesECBEncrypt(byte[] input, byte[] key) {
        return aesECB(input, key, Cipher.ENCRYPT_MODE);
    }
    /**
     * 使用AES加密原始字符串.
     *
     * @param input 原始输入字符数组
     * @param base64Key 符合AES要求的Base64密钥
     */
    public static String aesECBEncryptBase64String(String input, String base64Key) {
        return aesECBEncryptBase64String(input, getBase64DecodeKey(base64Key));
    }

    /**
     * 使用AES加密原始字符串.
     *
     * @param input 原始输入字符数组
     * @param key 符合AES要求的密钥
     */
    public static String aesECBEncryptBase64String(String input, byte[] key) {
        return EncodeUtils.base64Encode(aesECB(input.getBytes(StandardCharsets.UTF_8), key, Cipher.ENCRYPT_MODE));
    }

    /**
     * 使用AES加密原始字符串.
     *
     * @param input 原始输入字符数组
     * @param base64Key 符合AES要求的Base64密钥
     */
    public static String aesECBEncryptHexString(String input, String base64Key) {
        return aesECBEncryptHexString(input, getBase64DecodeKey(base64Key));
    }

    /**
     * 使用AES加密原始字符串.
     *
     * @param input 原始输入字符数组
     * @param key 符合AES要求的密钥
     */
    public static String aesECBEncryptHexString(String input, byte[] key) {
        return EncodeUtils.hexEncode(aesECB(input.getBytes(StandardCharsets.UTF_8), key, Cipher.ENCRYPT_MODE));
    }


    /**
     * 使用AES加密原始字符串.
     *
     * @param input 原始输入字符数组
     * @param key 符合AES要求的密钥
     * @param iv 初始向量
     */
    public static byte[] aesEncrypt(byte[] input, byte[] key, Byte[] iv) {
        return aes(input, key, iv, Cipher.ENCRYPT_MODE);
    }

    /**
     * 使用AES解密字符串, 返回原始字符串.
     *
     * @param input Hex编码的加密字符串
     * @param key 符合AES要求的密钥
     */
    public static String aesDecrypt(byte[] input, byte[] key) {
        byte[] decryptResult = aes(input, key, Cipher.DECRYPT_MODE);
        return new String(decryptResult);
    }

    /**
     * 使用AES解密字符串, 返回原始字符串.
     *
     * @param input Hex编码的加密字符串
     * @param key 符合AES要求的密钥
     * @param iv 初始向量
     */
    public static String aesDecrypt(byte[] input, byte[] key, Byte[] iv) {
        byte[] decryptResult = aes(input, key, iv, Cipher.DECRYPT_MODE);
        return new String(decryptResult);
    }

    /**
     * 使用AES解密字符串, 返回原始字符串.
     *
     * @param base64Data Base64编码的加密字符串
     * @param base64Key 符合AES要求的base64密钥
     */
    public static String aesECBDecryptBase64String(String base64Data, String base64Key) {
        return aesECBDecryptBase64String(base64Data,getBase64DecodeKey(base64Key));
    }

    /**
     * 使用AES解密字符串, 返回原始字符串.
     *
     * @param base64Data Base64编码的加密字符串
     * @param key 符合AES要求的密钥
     */
    public static String aesECBDecryptBase64String(String base64Data, byte[] key) {
        if(StringUtils.isBlank(base64Data)){
            return base64Data;
        }
        byte[] decryptResult = aesECB(EncodeUtils.base64Decode(base64Data), key, Cipher.DECRYPT_MODE);
        return new String(decryptResult);
    }

    /**
     * 使用AES解密字符串, 返回原始字符串.
     *
     * @param hexData Hex编码的加密字符串
     * @param base64Key 符合AES要求的base64密钥
     */
    public static String aesECBDecryptHexString(String hexData, String base64Key) {
        return aesECBDecryptHexString(hexData,getBase64DecodeKey(base64Key));
    }

    /**
     * 使用AES解密字符串, 返回原始字符串.
     *
     * @param hexData Hex编码的加密字符串
     * @param key 符合AES要求的密钥
     */
    public static String aesECBDecryptHexString(String hexData, byte[] key) {
        if(StringUtils.isBlank(hexData)){
            return hexData;
        }
        byte[] decryptResult = aesECB(EncodeUtils.hexDecode(hexData), key, Cipher.DECRYPT_MODE);
        return new String(decryptResult);
    }


    /**
     * 使用AES解密字符串, 返回原始字符串.
     *
     * @param input Hex编码的加密字符串
     * @param key 符合AES要求的密钥
     */
    public static String aesECBDecrypt(byte[] input, byte[] key) {
        byte[] decryptResult = aesECB(input, key, Cipher.DECRYPT_MODE);
        return new String(decryptResult);
    }


    /**
     * 使用AES加密或解密无编码的原始字节数组, 返回无编码的字节数组结果.
     *
     * @param input 原始字节数组
     * @param key 符合AES要求的密钥
     * @param mode Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
     */
    public static byte[] aes(byte[] input, byte[] key, int mode) {
        return  aes(input,key,null,mode);
    }

    /**
     * 使用AES加密或解密无编码的原始字节数组, 返回无编码的字节数组结果.
     *
     * @param input 原始字节数组
     * @param key 符合AES要求的密钥
     * @param iv 初始向量
     * @param mode Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
     */
    public static byte[] aes(byte[] input, byte[] key, Byte[] iv, int mode) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, AES);
            if(null != iv){
                gcMParameterSpec = new GCMParameterSpec(128, Collections3.toPrimitives(iv));
            }
            Cipher cipher = Cipher.getInstance(AES_CBC);
            cipher.init(mode, secretKey, gcMParameterSpec);
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            throw Exceptions.unchecked(e);
        }
    }


    /**
     * 使用AES加密或解密无编码的原始字节数组, 返回无编码的字节数组结果.
     *
     * @param input 原始字节数组
     * @param key 符合AES要求的密钥
     * @param mode Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
     */
    public static byte[] aesECB(byte[] input, byte[] key, int mode) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, AES);
            Cipher cipher = Cipher.getInstance(AES_ECB);
            cipher.init(mode, secretKey);
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 生成AES密钥,返回字节数组, 默认长度为128位(16字节).
     */
    public static byte[] generateAesKey() {
        return generateAesKey(DEFAULT_AES_KEYSIZE);
    }

    /**
     * 生成AES密钥,可选长度为128,192,256位.
     */
    public static byte[] generateAesKey(int keysize) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
            keyGenerator.init(keysize);
            SecretKey secretKey = keyGenerator.generateKey();
            return secretKey.getEncoded();
        } catch (GeneralSecurityException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 生成随机向量,默认大小为cipher.getBlockSize(), 16字节.
     */
    public static byte[] generateIV() {
        byte[] bytes = new byte[DEFAULT_IVSIZE];
        random.nextBytes(bytes);
        return bytes;
    }

}