package com.eryansky.encrypt.util;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.EncodeUtils;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.encrypt.config.EncryptProvider;
import com.eryansky.encrypt.enums.CipherMode;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;

/**
 * 数据加解密工具类
 *
 * @author Eryan
 */
public class RequestEncryptUtils {

    private static final Logger log = LoggerFactory.getLogger(RequestEncryptUtils.class);

    public static final String  ENCRYPT = "Encrypt";
    public static final String  ENCRYPT_KEY = "Encrypt-Key";

    /**
     * 数据加密
     * @param encrypt 加密方式
     * @param key 密钥
     * @param bytes 数据
     * @return
     */
    public static byte[] encryptData(String encrypt, String key, byte[] bytes) {
        if (!com.eryansky.common.utils.StringUtils.isNotBlank(key) && !CipherMode.BASE64.name().equals(encrypt)) {
            return bytes;
        }

        try {
            if (com.eryansky.common.utils.StringUtils.isNotBlank(encrypt)){
                if(CipherMode.SM4.name().equals(encrypt)){
                    bytes = Sm4Utils.encrypt(key, bytes);
                }else if(CipherMode.AES.name().equals(encrypt)){
                    bytes = Cryptos.aesECBEncrypt(bytes, key);
                }else if(CipherMode.BASE64.name().equals(encrypt)){
                    bytes = Base64.encodeBase64(bytes);
                }
            }
        } catch (Exception e) {
            log.error("Failed to encrypt data with cipher mode: {}", encrypt, e);
        }
        return bytes;
    }

    /**
     * 数据解密
     * @param encrypt 加密方式
     * @param key 密钥
     * @param bytes 数据
     * @return
     */
    public static byte[] decryptData(String encrypt, String key, byte[] bytes) {
        if (!com.eryansky.common.utils.StringUtils.isNotBlank(key) && !CipherMode.BASE64.name().equals(encrypt)) {
            return bytes;
        }

        try {
            if (CipherMode.SM4.name().equals(encrypt)) {
                return Sm4Utils.decrypt(key, bytes);
            } else if (CipherMode.AES.name().equals(encrypt)) {
                return Cryptos.aesECBDecrypt(bytes, key);
            } else if (CipherMode.BASE64.name().equals(encrypt)) {
                return Base64.decodeBase64(bytes);
            }
        } catch (Exception e) {
            log.error("Failed to decrypt data with cipher mode: {}", encrypt, e);
        }
        return bytes;
    }

//    服务端相关代码

    /**
     * 数据解密（根据请求加密密钥）
     * @param encrypt 加密方式
     * @param encryptKey 加密密钥
     * @param bytes 数据
     * @return
     */
    public static byte[] decryptDataByRequest(String encrypt, String encryptKey, byte[] bytes) {
        if (!com.eryansky.common.utils.StringUtils.isNotBlank(encryptKey) && !CipherMode.BASE64.name().equals(encrypt)) {
            return bytes;
        }

        try {
            if (CipherMode.SM4.name().equals(encrypt)) {
                String key = tryDecryptKeyHex(encryptKey);
                return Sm4Utils.decrypt(key, bytes);
            } else if (CipherMode.AES.name().equals(encrypt)) {
                String key = tryDecryptKeyBase64(encryptKey);
                return Cryptos.aesECBDecrypt(bytes, key);
            } else if (CipherMode.BASE64.name().equals(encrypt)) {
                return Base64.decodeBase64(bytes);
            }
        } catch (Exception e) {
            log.error("Failed to decryptDataByRequest with cipher mode: {}", encrypt, e);
        }
        return bytes;
    }

    /**
     * 数据解密（根据请求加密密钥）
     * @param request 请求对象
     * @param encodeBytes 已编码数据
     * @return
     */
    public static byte[] decryptEncodeDataByRequest(HttpServletRequest request, byte[] encodeBytes) throws Exception{
        String encrypt = WebUtils.getHeaderIgnoreCaseOrParameter(request, RequestEncryptUtils.ENCRYPT);
        String encryptKey = WebUtils.getHeaderIgnoreCaseOrParameter(request, RequestEncryptUtils.ENCRYPT_KEY);

        try {
            if (CipherMode.SM4.name().equals(encrypt)) {
                String key = tryDecryptKeyHex(encryptKey);
                return Sm4Utils.decrypt(key, EncodeUtils.hexDecode(new String(encodeBytes, StandardCharsets.UTF_8)));
            } else if (CipherMode.AES.name().equals(encrypt)) {
                String key = tryDecryptKeyBase64(encryptKey);
                return Cryptos.aesECBDecrypt(EncodeUtils.base64Decode(encodeBytes), key);
            } else if (CipherMode.BASE64.name().equals(encrypt)) {
                return Base64.decodeBase64(encodeBytes);
            }
        } catch (Exception e) {
            log.error("Failed to decryptDataByRequest with cipher mode: {}", encrypt, e);
            throw e;
        }
        return encodeBytes;
    }

    /**
     * 数据解密（根据请求加密密钥）
     * @param request 请求对象
     * @param bytes 数据
     * @return
     */
    public static byte[] decryptDataByRequest(HttpServletRequest request, byte[] bytes) throws Exception{
        boolean ignoreEncrypt = Boolean.parseBoolean((String.valueOf(request.getAttribute("ignoreEncrypt"))));
        if (ignoreEncrypt) {
            return bytes;
        }
        String encrypt = WebUtils.getHeaderIgnoreCaseOrParameter(request, RequestEncryptUtils.ENCRYPT);
        String encryptKey = WebUtils.getHeaderIgnoreCaseOrParameter(request, RequestEncryptUtils.ENCRYPT_KEY);

        try {
            if (CipherMode.SM4.name().equals(encrypt)) {
                String key = tryDecryptKeyHex(encryptKey);
                return Sm4Utils.decrypt(key, bytes);
            } else if (CipherMode.AES.name().equals(encrypt)) {
                String key = tryDecryptKeyBase64(encryptKey);
                return Cryptos.aesECBDecrypt(bytes, key);
            } else if (CipherMode.BASE64.name().equals(encrypt)) {
                return Base64.decodeBase64(bytes);
            }
        } catch (Exception e) {
            log.error("Failed to decryptDataByRequest with cipher mode: {}", encrypt, e);
            throw e; // 记录日志后向上抛出异常，交给上层处理
        }
        return bytes;
    }


    /**
     * 解密 Request 密码公用提取逻辑
     */
    public static String decryptEncodeDataByRequest(HttpServletRequest request, String data) throws Exception {
        boolean ignoreEncrypt = Boolean.parseBoolean(String.valueOf(request.getAttribute("ignoreEncrypt")));
        if (ignoreEncrypt || StringUtils.isBlank(data)) {
            return data;
        }

        String encrypt = WebUtils.getHeaderIgnoreCaseOrParameter(request, RequestEncryptUtils.ENCRYPT);
        if (StringUtils.isBlank(encrypt)) {
            return data;
        }

        String encryptKey = WebUtils.getHeaderIgnoreCaseOrParameter(request, RequestEncryptUtils.ENCRYPT_KEY);

        try {
            if ("AES".equals(encrypt)) {
                byte[] decrypted = RequestEncryptUtils.decryptDataByRequest(encrypt, encryptKey, EncodeUtils.base64Decode(data));
                return new String(decrypted, StandardCharsets.UTF_8);
            } else if ("SM4".equals(encrypt)) {
                byte[] decrypted = RequestEncryptUtils.decryptDataByRequest(encrypt, encryptKey, EncodeUtils.hexDecode(data));
                return new String(decrypted, StandardCharsets.UTF_8);
            } else if (CipherMode.BASE64.name().equalsIgnoreCase(encrypt)) {
                return new String(EncodeUtils.base64Decode(data), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("数据解密失败, EncryptType: {}, error: {}", encrypt, e.getMessage());
            throw e; // 记录日志后向上抛出异常，交给上层处理
        }

        return data;
    }



    /**
     * 数据加密（根据请求加密密钥）
     * @param encrypt 加密方式
     * @param encryptKey 加密密钥
     * @param bytes 数据
     * @return
     */
    public static byte[] encryptDataByRequest(String encrypt, String encryptKey, byte[] bytes) throws Exception {
        if (CipherMode.SM4.name().equalsIgnoreCase(encrypt) && com.eryansky.common.utils.StringUtils.isNotBlank(encryptKey)) {
            String key = tryDecryptKeyHex(encryptKey);
            return Sm4Utils.encrypt(key, bytes);
        }

        if (CipherMode.AES.name().equalsIgnoreCase(encrypt) && com.eryansky.common.utils.StringUtils.isNotBlank(encryptKey)) {
            String key = tryDecryptKeyBase64(encryptKey);
            return Cryptos.aesECBEncrypt(bytes, key);
        }

        if (CipherMode.BASE64.name().equalsIgnoreCase(encrypt)) {
            return Base64.encodeBase64(bytes);
        }

        // Unknown/unsupported mode — return original payload
        return bytes;
    }


    /**
     * 数据加密（根据请求加密密钥）
     * @param encrypt 加密方式
     * @param encryptKey 加密密钥
     * @param bytes 数据
     * @return
     */
    public static String encryptDataStringByRequest(String encrypt, String encryptKey, byte[] bytes) throws Exception {
        if (CipherMode.SM4.name().equalsIgnoreCase(encrypt) && com.eryansky.common.utils.StringUtils.isNotBlank(encryptKey)) {
            String key = tryDecryptKeyHex(encryptKey);
            return EncodeUtils.hexEncode(Sm4Utils.encrypt(key, bytes));
        }

        if (CipherMode.AES.name().equalsIgnoreCase(encrypt) && com.eryansky.common.utils.StringUtils.isNotBlank(encryptKey)) {
            String key = tryDecryptKeyBase64(encryptKey);
            return EncodeUtils.base64Encode(Cryptos.aesECBEncrypt(bytes, key));
        }

        if (CipherMode.BASE64.name().equalsIgnoreCase(encrypt)) {
            return EncodeUtils.base64Encode(bytes);
        }

        // Unknown/unsupported mode — return original payload
        return new String(bytes);
    }

    public static String tryDecryptKeyHex(String encryptedKey) {
        try {
            return RSAUtils.decryptHexString(encryptedKey, EncryptProvider.privateKeyBase64());
        } catch (Exception e) {
            return encryptedKey;
        }
    }

    public static String tryDecryptKeyBase64(String encryptedKey) {
        try {
            return RSAUtils.decryptBase64String(encryptedKey, EncryptProvider.privateKeyBase64());
        } catch (Exception e) {
            return encryptedKey;
        }
    }
}
