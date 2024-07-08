package com.eryansky.common.orm.mybatis.sensitive.encrypt;

import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveType;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveTypeHandler;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveTypeRegisty;
import com.eryansky.common.orm.mybatis.sensitive.utils.Hex;
import com.eryansky.common.orm.mybatis.sensitive.IEncrypt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据脱敏用到的AES加解密类
 *
 * @author Eryan
 * @version 2019-12-13
 */
public class AesSupport implements IEncrypt {

    private static final Logger log = LoggerFactory.getLogger(AesSupport.class);

    private static final String KEY_ALGORITHM = "AES";
    /**
     * 加密解密算法/加密模式/填充方式
     */
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/GCM/NoPadding";

    private static final String DEFAULT_CIPHER_ECB = "AES/ECB/PKCS5Padding";
    /**
     * 默认密钥, 256位32个字节
     */
    public static final String DEFAULT_KEY = "ecececececececececececececececec";
    /**
     * 密钥, 256位32个字节
     */
    private String key;
    private final SensitiveTypeHandler sensitiveTypeHandler = SensitiveTypeRegisty.get(SensitiveType.DEFAULT);

    private SecretKeySpec secretKeySpec;
    /**
     * 初始向量IV
     */
    private static GCMParameterSpec gcMParameterSpec;
    /**
     * 默认初始向量IV 长度为128位16个字节
     */
    public static final String DEFAULT_IV = "ecececececececec";


    private static class AesSupportHolder {
        private static final AesSupport aesSupport = new AesSupport();
    }
    /**
     * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
     */
    public static AesSupport getInstance() {
        return AesSupport.AesSupportHolder.aesSupport;
    }

    static {
        gcMParameterSpec = new GCMParameterSpec(128, DEFAULT_IV.getBytes());
        java.security.Security.setProperty("crypto.policy", "unlimited");
    }
    public AesSupport() {
        try {
            this.key = DEFAULT_KEY;
            this.secretKeySpec = getSecretKey(key);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public AesSupport(String key) throws NoSuchAlgorithmException {

        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("password should not be null!");
        }

        this.key = key;
        this.secretKeySpec = getSecretKey(key);
    }

    @Override
    public String defaultType() {
        return "AES";
    }

    @Override
    public String encrypt(String data, String type) {
        return encrypt(data);
    }

    @Override
    public List<String> batchEncrypt(List<String> datas, String type) {
        return batchEncrypt(datas);
    }

    public String encrypt(String value) {
        if (null == value) {
            return value;
        }
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
            log.error("AES加密时出现问题，密钥为：{}",sensitiveTypeHandler.handle(key));
            throw new IllegalStateException("AES加密时出现问题" + e.getMessage(), e);
        }
    }

    public String encryptECB(String value) {
        if (org.springframework.util.StringUtils.isEmpty(value)) {
            return "";
        }
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ECB);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            byte[] content = value.getBytes(StandardCharsets.UTF_8);
            byte[] encryptData = cipher.doFinal(content);

            return Hex.bytesToHexString(encryptData);
        } catch (Exception e) {
            log.error("AES加密时出现问题，密钥为：{}",key);
            throw new IllegalStateException("AES加密时出现问题" + e.getMessage(), e);
        }
    }

    public List<String> batchEncrypt(List<String> datas) {
        return datas.stream().map(this::encrypt).collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> batchEncrypt(Map<String, List<String>> mapData) {
        return mapData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,e->batchEncrypt(e.getValue())));
    }

    @Override
    public String decrypt(String data, String type) {
        return decrypt(data);
    }

    @Override
    public List<String> batchDecrypt(List<String> datas, String type) {
        return batchDecrypt(datas);
    }

    public String decrypt(String value) {
        if (null == value) {
            return value;
        }
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
            log.error("AES解密时出现问题，密钥为：{}，密文为：{}", sensitiveTypeHandler.handle(key), value);
            throw new IllegalStateException("AES解密时出现问题" + e.getMessage(), e);
        }
    }

    public String decryptECB(String value) {
        if (org.springframework.util.StringUtils.isEmpty(value)) {
            return "";
        }
        try {
            byte[] encryptData = Hex.hexStringToBytes(value);
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ECB);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] content = cipher.doFinal(encryptData);
            return new String(content, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密时出现问题，密钥为：{}，密文为：{}", key, value);
            throw new IllegalStateException("AES解密时出现问题" + e.getMessage(), e);
        }
    }

    public List<String> batchDecrypt(List<String> datas) {
        return datas.stream().map(this::decrypt).collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> batchDecrypt(Map<String, List<String>> mapData) {
        return mapData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,e->batchDecrypt(e.getValue())));
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
