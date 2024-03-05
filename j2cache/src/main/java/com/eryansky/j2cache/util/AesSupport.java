package com.eryansky.j2cache.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 数据脱敏用到的AES加解密类
 *
 * @author Eryan
 * @version 2019-12-13
 */
public class AesSupport {

    private static final Logger log = LoggerFactory.getLogger(AesSupport.class);

    private static final String KEY_ALGORITHM = "AES";
    /**
     * 加密解密算法/加密模式/填充方式
     */
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    /**
     * 默认密钥, 256位32个字节
     */
    public static final String DEFAULT_KEY = "ecececececececececececececececec";
    /**
     * 密钥, 256位32个字节
     */
    private String key;

    private SecretKeySpec secretKeySpec;
    /**
     * 初始向量IV
     */
    private static GCMParameterSpec gcMParameterSpec;
    /**
     * 默认初始向量IV 长度为128位16个字节
     */
    public static final String DEFAULT_IV = "ecececececececec";

    static {
        gcMParameterSpec = new GCMParameterSpec(128, DEFAULT_IV.getBytes());
        java.security.Security.setProperty("crypto.policy", "unlimited");
    }
    public AesSupport() throws NoSuchAlgorithmException {
        this.key = DEFAULT_KEY;
        this.secretKeySpec = getSecretKey(key);
    }

    public AesSupport(String key) throws NoSuchAlgorithmException {
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("password should not be null!");
        }

        this.key = key;
        this.secretKeySpec = getSecretKey(key);
    }

    public String encrypt(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec,gcMParameterSpec);

            byte[] content = value.getBytes(StandardCharsets.UTF_8);
            byte[] encryptData = cipher.doFinal(content);

            return Hex.bytesToHexString(encryptData);
        } catch (Exception e) {
            log.error("AES加密时出现问题，密钥为：{}",key);
            throw new IllegalStateException("AES加密时出现问题" + e.getMessage(), e);
        }
    }

    public String decrypt(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        try {
            byte[] encryptData = Hex.hexStringToBytes(value);
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcMParameterSpec);
            byte[] content = cipher.doFinal(encryptData);
            return new String(content, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密时出现问题，密钥为：{}，密文为：{}", key, value);
            throw new IllegalStateException("AES解密时出现问题" + e.getMessage(), e);
        }
    }


    private static SecretKeySpec getSecretKey(final String key) throws NoSuchAlgorithmException {
        //返回生成指定算法密钥生成器的 KeyGenerator 对象
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        //AES 要求密钥长度为 128
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(key.getBytes());
        kg.init(128, random);
        //生成一个密钥
        SecretKey secretKey = kg.generateKey();
        // 转换为AES专用密钥
        return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
    }
}
