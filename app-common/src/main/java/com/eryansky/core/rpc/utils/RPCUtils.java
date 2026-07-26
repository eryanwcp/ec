package com.eryansky.core.rpc.utils;

import com.eryansky.client.common.rpc.RPCExchange;
import com.eryansky.client.common.rpc.RPCMethodConfig;
import com.eryansky.client.common.rpc.RPCPermissions;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.core.rpc.consumer.ConsumerExecutor;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security._enum.Logical;
import com.eryansky.encrypt.config.EncryptProvider;
import com.eryansky.encrypt.enums.CipherMode;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

public class RPCUtils {

    private static final Logger log = LoggerFactory.getLogger(RPCUtils.class);
    public static final String AUTH_TYPE = "apiKey";
    public static final String HEADER_API_SERVICE_NAME = "Api-Service-Name";
    public static final String HEADER_API_SERVICE_METHOD = "Api-Service-method";
    public static final String HEADER_AUTH_TYPE = "Auth-Type";
    public static final String HEADER_X_API_KEY = "X-Api-Key";

    public static final String  HEADER_ENCRYPT = "Encrypt";
    public static final String  HEADER_ENCRYPT_KEY = "Encrypt-Key";

    public static final String HEADER_RPC_SERIALIZER = "X-RPC-Serializer";
    public static final String HEADER_APPLICATION_ID = "X-APPLICATION-ID";

    /**
     * 每个方法对应的静态元信息（URL 与请求头），在首次调用时构建并缓存，避免每次 RPC 调用都重复解析注解、占位符与重建 Map。
     */
    private static final class MethodMeta {
        final String url;
        final Map<String, String> headers;

        MethodMeta(String url, Map<String, String> headers) {
            this.url = url;
            this.headers = headers;
        }
    }

    public static <T> T createProxyObj(String serverUrl, Class clazz) {
        if (!clazz.isInterface()) { // 接口才可以进行代理
            throw new IllegalArgumentException(clazz + " is not a interface!");
        }
        RPCExchange classAnnotation = (RPCExchange) clazz.getAnnotation(RPCExchange.class);
        if (classAnnotation == null) {
            throw new IllegalArgumentException(clazz + " 缺少 @RPCExchange 注解！");
        }
        String appName = classAnnotation.name();
        // serverUrl + urlPrefix + "/" + appName + "/" 为各方法共用前缀，方法名(或别名)在首次调用时拼接
        String baseUrl = serverUrl + classAnnotation.urlPrefix() + "/" + appName + "/";

        // 方法元信息缓存：键为接口方法，值为预解析出的 URL 与请求头（首次访问时惰性构建）
        Map<Method, MethodMeta> metaCache = Maps.newConcurrentMap();

        return (T) Enhancer.create(clazz, (MethodInterceptor) (o, method, objects, methodProxy) -> {
            MethodMeta meta = metaCache.computeIfAbsent(method,
                    m -> buildMethodMeta(baseUrl, classAnnotation, appName, m));
            Type returnType = method.getGenericReturnType();
            ParameterizedTypeReference<T> reference = ParameterizedTypeReference.forType(returnType);

            // ConsumerExecutor 会向 headers 写入 ENCRYPT_KEY，因此每次调用需传入独立副本，避免污染缓存
            return ConsumerExecutor.execute(meta.url, Maps.newHashMap(meta.headers), objects, reference);
        });

    }

    /**
     * 预解析单个方法的 URL 与请求头（方法级优先于类级）。
     */
    private static MethodMeta buildMethodMeta(String baseUrl, RPCExchange classAnnotation, String appName, Method method) {
        RPCMethodConfig methodAnnotation = method.getAnnotation(RPCMethodConfig.class);
        String requestMethodName = method.getName();
        if (methodAnnotation != null && StringUtils.hasLength(methodAnnotation.alias())) {
            requestMethodName = methodAnnotation.alias();
        }
        String url = baseUrl + requestMethodName;

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HEADER_API_SERVICE_NAME, appName);
        headers.put(HEADER_API_SERVICE_METHOD, requestMethodName);
        headers.put(HEADER_AUTH_TYPE, AUTH_TYPE);
        headers.put(HEADER_X_API_KEY, StringUtils.hasLength(classAnnotation.apiKey())
                ? resolve(null, classAnnotation.apiKey()) : AppConstants.getRPCClientApiKey());
        headers.put(HEADER_RPC_SERIALIZER, AppConstants.getRPCClientSerializer());
        headers.put(HEADER_APPLICATION_ID, SpringContextHolder.getApplicationContext().getId());

        // 加密方式：方法级优先，未配置时回退到类级（ENCRYPT_NONE 表示显式不加密）
        String encrypt = methodAnnotation != null ? resolve(null, methodAnnotation.encrypt()) : null;
        if (!StringUtils.hasLength(encrypt)) {
            encrypt = resolve(null, classAnnotation.encrypt());
        }
        if (StringUtils.hasLength(encrypt) && !RPCExchange.ENCRYPT_NONE.equals(encrypt)) {
            headers.put(HEADER_ENCRYPT, encrypt);
            log.debug("RPC服务传输数据加密：{} {}", url, encrypt);
        }
        return new MethodMeta(url, headers);
    }


    public static String resolve(ConfigurableBeanFactory beanFactory, String value) {
        if (StringUtils.hasText(value)) {
            if (beanFactory == null) {
                return SpringContextHolder.getApplicationContext().getEnvironment().resolvePlaceholders(value);
            }
            BeanExpressionResolver resolver = beanFactory.getBeanExpressionResolver();
            String resolved = beanFactory.resolveEmbeddedValue(value);
            if (resolver == null) {
                return resolved;
            }
            Object evaluateValue = resolver.evaluate(resolved, new BeanExpressionContext(beanFactory, null));
            if (evaluateValue != null) {
                return String.valueOf(evaluateValue);
            }
            return null;
        }
        return value;
    }



    private static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationType) {
        T result = clazz.getAnnotation(annotationType);
        if (result == null) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                return getAnnotation(superclass, annotationType);
            } else {
                return null;
            }
        } else {
            return result;
        }
    }

    public static Boolean isPermitted(Class clazz, Method method){
        //资源/权限注解
        RPCPermissions requiresPermissions = method.getAnnotation(RPCPermissions.class);
        if(requiresPermissions == null){
            requiresPermissions = getAnnotation(clazz,RPCPermissions.class);
        }
        if (requiresPermissions != null) {//方法注解处理
            String[] permissions = requiresPermissions.value();
            boolean permittedResource = false;
            for (String permission : permissions) {
                permittedResource = SecurityUtils.isPermitted(permission);
                if (Logical.AND.equals(requiresPermissions.logical())) {
                    if (!permittedResource) {
                        return false;
                    }
                } else {
                    if (permittedResource) {
                        break;
                    }
                }
            }
            if(!permittedResource){
                return false;
            }
        }
        return null;
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



    public static final class EncryptRequestKey {
        final String encrypt;
        final String key;
        final String encryptKey;

        EncryptRequestKey(String encrypt,String key, String encryptKey) {
            this.encrypt = encrypt;
            this.key = key;
            this.encryptKey = encryptKey;
        }

        public String getEncrypt() {
            return encrypt;
        }

        public String getKey() {
            return key;
        }

        public String getEncryptKey() {
            return encryptKey;
        }
    }

    /**
     * 生成加密密钥
     * @param encrypt
     * @return
     */
    public static EncryptRequestKey generateEncryptKey(String encrypt) {
        if (com.eryansky.common.utils.StringUtils.isBlank(encrypt) || CipherMode.BASE64.name().equals(encrypt)) {
            return new EncryptRequestKey(encrypt,null,null);
        }

        try {
            if(CipherMode.SM4.name().equals(encrypt)){
                String key = Sm4Utils.generateHexKeyString();
                String encryptKey =  RSAUtils.encryptHexString(key,EncryptProvider.publicKeyBase64());
                return new EncryptRequestKey(encrypt,key,encryptKey);
            }else if(CipherMode.AES.name().equals(encrypt)){
                String key = Cryptos.getBase64EncodeKey();
                String encryptKey =  RSAUtils.encryptBase64String(key,EncryptProvider.publicKeyBase64());
                return new EncryptRequestKey(encrypt,key,encryptKey);
            }
        } catch (Exception e) {
            log.error("Failed to generateEncryptKey cipher mode: {}", encrypt, e);
        }
        return new EncryptRequestKey(encrypt,null,null);
    }


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
}
