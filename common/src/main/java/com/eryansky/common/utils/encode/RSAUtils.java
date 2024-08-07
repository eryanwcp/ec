package com.eryansky.common.utils.encode;

import com.google.common.collect.Maps;
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

public class RSAUtils {

    private static Logger logger = LoggerFactory.getLogger(RSAUtils.class);

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
        return initKey(KEY_SIZE);
    }

    /**
     * 初始化密钥对
     *
     * @return Map 甲方密钥的Map
     */
    public static Map<String, Object> initKey(int keySize) throws Exception {
        //实例化密钥生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥生成器
        keyPairGenerator.initialize(keySize);
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
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(EncodeUtils.base64Decode(base64PublicKey));
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return publicKey;
    }

    public static PrivateKey getPrivateKey(String base64PrivateKey) {
        PrivateKey privateKey = null;
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(EncodeUtils.base64Decode(base64PrivateKey));
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
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


    public static String encryptBase64String(String data) {
        return encryptBase64String(data, DEFAULT_PUBLIC_KEY);
    }

    public static String encryptBase64String(String data, String base64PublicKey) {
        return EncodeUtils.base64Encode(encrypt(data, base64PublicKey));
    }

    public static String encryptHexString(String data) {
        return EncodeUtils.hexEncode(encrypt(data, DEFAULT_PUBLIC_KEY));
    }

    public static String encryptHexString(String data, String base64PublicKey) {
        return EncodeUtils.hexEncode(encrypt(data, base64PublicKey));
    }

    public static byte[] encrypt(String data) {
        return encrypt(data, DEFAULT_PUBLIC_KEY);
    }

    public static byte[] encrypt(String data, String base64PublicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(base64PublicKey));
            return cipher.doFinal(data.getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decryptBase64String(String base64Data) {
        return decryptBase64String(base64Data, DEFAULT_PRIVATE_KEY);
    }

    public static String decryptBase64String(String base64Data, String base64PrivateKey) {
        return decrypt(EncodeUtils.base64Decode(base64Data), getPrivateKey(base64PrivateKey));
    }

    public static String decryptHexString(String hexData) {
        return decryptHexString(hexData, DEFAULT_PRIVATE_KEY);
    }
    public static String decryptHexString(String hexData, String base64PrivateKey) {
        return decrypt(EncodeUtils.hexDecode(hexData), getPrivateKey(base64PrivateKey));
    }

    public static String decrypt(String data) {
        return decrypt(data,DEFAULT_PRIVATE_KEY);
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

    public static String decrypt(byte[] data) {
        return decrypt(data, getPrivateKey(DEFAULT_PRIVATE_KEY));
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
            System.out.println("公钥：" + EncodeUtils.base64Encode(publicKey));
            System.out.println("私钥：" + EncodeUtils.base64Encode(privateKey));

            System.out.println("默认公钥：" + RSAUtils.getDefaultBase64PublicKey());

            String base64EncodeKey = Cryptos.getBase64EncodeKey();
            System.out.println(base64EncodeKey);
            //常規方法
            String encryptKey = RSAUtils.encryptBase64String(base64EncodeKey,DEFAULT_PUBLIC_KEY);
            String decryptKey = RSAUtils.decryptBase64String(encryptKey,DEFAULT_PRIVATE_KEY);
            System.out.println(encryptKey);
            System.out.println(decryptKey);

            String data = "123456";
            System.out.println(data);
            //AES
            String encryptData = Cryptos.aesECBEncryptBase64String(data, base64EncodeKey);
            System.out.println(encryptData);
            String decryptData = Cryptos.aesECBDecryptBase64String(encryptData, base64EncodeKey);
            System.out.println(decryptData);



        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
