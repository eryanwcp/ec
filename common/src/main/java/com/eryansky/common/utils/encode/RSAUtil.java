package com.eryansky.common.utils.encode;

import com.eryansky.common.utils.Identities;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.pqc.legacy.math.linearalgebra.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

public class RSAUtil {

    private static Logger logger = LoggerFactory.getLogger(RSAUtil.class);

    /**
     * 默认初始公钥
     */
    private static String DEFAULT_PUBLIC_KEY = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJHNPhjl//gGiYWUiTGBnygiJUotaNJ+GfuFQkzF0em2WWuQtSI7K/pFHzqlmFeJJjF6a0ouXfXxorup5rN9BdkCAwEAAQ==";
    /**
     * 默认初始私钥
     */
    private static String DEFAULT_PRIVATE_KEY = "MIIBVwIBADANBgkqhkiG9w0BAQEFAASCAUEwggE9AgEAAkEAkc0+GOX/+AaJhZSJMYGfKCIlSi1o0n4Z+4VCTMXR6bZZa5C1Ijsr+kUfOqWYV4kmMXprSi5d9fGiu6nms30F2QIDAQABAkEAjuVv8ekhmQ2XJPNyDrIysZvdnjipHnv2rRtq4mGTHRFfExmLpYw08GvupxR7GmJDGx9IHRW6O1F8qmud2yTEvQIhAMNWwzFiA8fcAP9obtVJ16EEYoLJHxAOLKBGddnPgD5TAiEAvxRTdtVF4HmQqess33WiAJTJG5IC4Dum5PToxNsEbaMCIQC4mAklZaaE+9bFhf8W+A0ZUHd3eHAuT/bED1HXX0ulmQIhALLICtvp6sqAE6nYgBDImH5gt9YTBJvXVG1u9QeTQQ5vAiEAjVpHbGAMFbHRK9odd76BZH09H26X8QvZIj3ySbW21qA=";
    //非对称密钥算法
    public static final String KEY_ALGORITHM = "RSA";
    /**
     * 密钥长度，DH算法的默认密钥长度是1024
     * 密钥长度必须是64的倍数，在512到65536位之间
     */
    private static final int KEY_SIZE = 512;
    /**
     * 公钥
     */
    private static final String PUBLIC_KEY = "RSAPublicKey";
    /**
     * 私钥
     */
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 初始化密钥对
     *
     * @return Map 甲方密钥的Map
     */
    public static Map<String, Object> initKey() throws Exception {
        //实例化密钥生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥生成器
        keyPairGenerator.initialize(KEY_SIZE);
        //生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //甲方公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        //甲方私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        //将密钥存储在map中
        Map<String, Object> keyMap = Maps.newHashMap();
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * 取得私钥
     *
     * @param keyMap 密钥map
     * @return byte[] 私钥
     */
    public static byte[] getPrivateKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return key.getEncoded();
    }

    /**
     * 取得公钥
     *
     * @param keyMap 密钥map
     * @return byte[] 公钥
     */
    public static byte[] getPublicKey(Map<String, Object> keyMap) throws Exception {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return key.getEncoded();
    }

    /**
     * 获取默认公钥
     *
     * @return
     */
    public static String getDefaultBase64PublicKey() {
        return DEFAULT_PUBLIC_KEY;
    }


    /**
     * 获取默认私钥
     *
     * @return
     */
    public static String getDefaultBase64PrivateKey() {
        return DEFAULT_PRIVATE_KEY;
    }

    public static PublicKey getPublicKey(String base64PublicKey) {
        PublicKey publicKey = null;
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(base64PublicKey.getBytes()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return publicKey;
    }

    public static PrivateKey getPrivateKey(String base64PrivateKey) {
        PrivateKey privateKey = null;
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(base64PrivateKey.getBytes()));
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }
        try {
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            logger.error(e.getMessage(), e);
        }
        return privateKey;
    }


    public static String encodeBase64String(String data) {
        return encodeBase64String(data, DEFAULT_PUBLIC_KEY);
    }

    public static String encodeBase64String(String data, String publicKey) {
        return Base64.encodeBase64String(encrypt(data, publicKey));
    }

    public static byte[] encrypt(String data) {
        return encrypt(data, DEFAULT_PUBLIC_KEY);
    }

    public static byte[] encrypt(String data, String publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKey));
            return cipher.doFinal(data.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decryptBase64(String data) {
        return decryptBase64(data, DEFAULT_PRIVATE_KEY);
    }

    public static String decryptBase64(String data, String base64PrivateKey) {
        return decrypt(Base64.decodeBase64(data.getBytes()), getPrivateKey(base64PrivateKey));
    }

    public static String decrypt(String data, String base64PrivateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(base64PrivateKey));
            return new String(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(byte[] data, String base64PrivateKey) {
        return decrypt(data, getPrivateKey(base64PrivateKey));
    }

    public static String decrypt(byte[] data, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            //生成密钥对
            Map<String, Object> keyMap = initKey();
            //公钥
            byte[] publicKey = getPublicKey(keyMap);
            //私钥
            byte[] privateKey = getPrivateKey(keyMap);
            System.out.println("公钥：" + Base64.encodeBase64String(publicKey));
            System.out.println("私钥：" + Base64.encodeBase64String(privateKey));

            System.out.println("默认公钥：" + RSAUtil.getDefaultBase64PublicKey());

            String encryptedString = RSAUtil.encodeBase64String("123456");


            System.out.println(encryptedString);
            String decryptedString = RSAUtil.decryptBase64(encryptedString);
            System.out.println(decryptedString);

            String key = Identities.uuid2().substring(0, 16);
            System.out.println(key);
            //常規方法
            String encryptKey = RSAUtil.encodeBase64String(key,DEFAULT_PUBLIC_KEY);
            String decryptKey = RSAUtil.decryptBase64(encryptKey,DEFAULT_PRIVATE_KEY);
            System.out.println(encryptKey);
            System.out.println(decryptKey);
            //接口
            System.out.println(Base64.encodeBase64String(key.getBytes()));
            byte[] encryptKeys = RSAUtil.encrypt(Base64.encodeBase64String(key.getBytes()), DEFAULT_PUBLIC_KEY);
            String encrypt = Base64.encodeBase64String(encryptKeys);
            System.out.println(encrypt);
            System.out.println(RSAUtil.decryptBase64(encrypt, DEFAULT_PRIVATE_KEY));
            System.out.println(new String(Base64.decodeBase64(RSAUtil.decryptBase64(encrypt, DEFAULT_PRIVATE_KEY))));
            System.out.println(new String(Base64.decodeBase64(RSAUtil.decrypt(encryptKeys, DEFAULT_PRIVATE_KEY))));


            String data = "123456";
            System.out.println(data);
            //AES
            String encryptData = Cryptos.aesECBEncryptBase64String(data, key);
            System.out.println(encryptData);
            String decryptData = Cryptos.aesECBDecryptBase64String(encryptData, key);
            System.out.println(decryptData);
            //SM4
            String encryptData2 = Sm4Utils.encryptEcb(ByteUtils.toHexString(key.getBytes()), data);
            System.out.println(encryptData2);
            String decryptData2 = Sm4Utils.decryptEcb(ByteUtils.toHexString(key.getBytes()), encryptData2);
            System.out.println(decryptData2);


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
