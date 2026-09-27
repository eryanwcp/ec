package com.eryansky.core.rpc.utils;

import com.eryansky.client.common._enum.Logical;
import com.eryansky.client.common.rpc.RPCExchange;
import com.eryansky.client.common.rpc.RPCMethodConfig;
import com.eryansky.client.common.rpc.RPCPermissions;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.core.rpc.consumer.ConsumerExecutor;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.encrypt.config.EncryptProvider;
import com.eryansky.encrypt.enums.CipherMode;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
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
    public static final String HEADER_ENCRYPT = "Encrypt";
    public static final String HEADER_ENCRYPT_KEY = "Encrypt-Key";
    public static final String HEADER_RPC_SERIALIZER = "X-RPC-Serializer";
    public static final String HEADER_APPLICATION_ID = "X-APPLICATION-ID";

    /**
     * 每个方法对应的静态元信息（URL 与请求头），在首次调用时构建并缓存，避免每次 RPC 调用都重复解析注解与重建 Map。
     */
    private static final class MethodMeta {
        final String url;
        final Map<String, String> headers;

        MethodMeta(String url, Map<String, String> headers) {
            this.url = url;
            this.headers = headers;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProxyObj(String serverUrl, Class<T> clazz) {
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException(clazz + " is not an interface!");
        }

        RPCExchange classAnnotation = clazz.getAnnotation(RPCExchange.class);
        if (classAnnotation == null) {
            throw new IllegalArgumentException(clazz + " 缺少 @RPCExchange 注解！");
        }

        String appName = classAnnotation.name();
        String baseUrl = serverUrl + classAnnotation.urlPrefix() + "/" + appName + "/";

        // 方法元信息缓存：键为接口方法，值为预解析出的 URL 与请求头（首次访问时惰性构建）
        Map<Method, MethodMeta> metaCache = Maps.newConcurrentMap();

        return (T) Enhancer.create(clazz, (MethodInterceptor) (o, method, objects, methodProxy) -> {
            MethodMeta meta = metaCache.computeIfAbsent(method, m -> buildMethodMeta(baseUrl, classAnnotation, appName, m));
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
                ? resolve(null, classAnnotation.apiKey())
                : AppConstants.getRPCClientApiKey());
        headers.put(HEADER_RPC_SERIALIZER, AppConstants.getRPCClientSerializer());

        ApplicationContext context = SpringContextHolder.getApplicationContext();
        if (context != null) {
            headers.put(HEADER_APPLICATION_ID, context.getId());
        }

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
        if (!StringUtils.hasText(value)) {
            return value;
        }

        if (beanFactory == null) {
            ApplicationContext context = SpringContextHolder.getApplicationContext();
            if (context != null && context.getEnvironment() != null) {
                return context.getEnvironment().resolvePlaceholders(value);
            }
            return value;
        }

        BeanExpressionResolver resolver = beanFactory.getBeanExpressionResolver();
        String resolved = beanFactory.resolveEmbeddedValue(value);
        if (resolver == null) {
            return resolved;
        }

        Object evaluateValue = resolver.evaluate(resolved, new BeanExpressionContext(beanFactory, null));
        return evaluateValue != null ? String.valueOf(evaluateValue) : null;
    }

    private static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> annotationType) {
        T result = clazz.getAnnotation(annotationType);
        if (result == null) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                return getAnnotation(superclass, annotationType);
            }
            return null;
        }
        return result;
    }

    /**
     * 检查当前请求是否符合权限限制
     *
     * @param clazz  类类型
     * @param method 调用方法
     * @return true 表示无权限限制或权限校验通过；false 表示未通过权限校验
     */
    public static boolean isPermitted(Class<?> clazz, Method method) {
        RPCPermissions requiresPermissions = method.getAnnotation(RPCPermissions.class);
        if (requiresPermissions == null) {
            requiresPermissions = getAnnotation(clazz, RPCPermissions.class);
        }

        // 若无注解或配置空权限数组，默认判定为有权限/放行
        if (requiresPermissions == null || requiresPermissions.value().length == 0) {
            return true;
        }

        String[] permissions = requiresPermissions.value();
        boolean isAnd = Logical.AND.equals(requiresPermissions.logical());

        for (String permission : permissions) {
            boolean hasPermission = SecurityUtils.isPermitted(permission);
            if (isAnd && !hasPermission) {
                return false; // AND 模式：只要有一个权限不满足直接拒绝
            }
            if (!isAnd && hasPermission) {
                return true;  // OR 模式：只要有一个权限满足直接放行
            }
        }

        // AND 模式下顺利跑完循环说明全满足；OR 模式下跑完循环说明全不满足
        return isAnd;
    }

    // RPC客户端加密数据封装
    public static final class EncryptRequestKey {
        final String encrypt;
        final String key;
        final String encryptKey;

        EncryptRequestKey(String encrypt, String key, String encryptKey) {
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
     *
     * @param encrypt 加密模式名称
     * @return 秘钥对象
     */
    public static EncryptRequestKey generateEncryptKey(String encrypt) {
        if (com.eryansky.common.utils.StringUtils.isBlank(encrypt) || CipherMode.BASE64.name().equalsIgnoreCase(encrypt)) {
            return new EncryptRequestKey(encrypt, null, null);
        }

        try {
            if (CipherMode.SM4.name().equalsIgnoreCase(encrypt)) {
                String key = Sm4Utils.generateHexKeyString();
                String encryptKey = RSAUtils.encryptHexString(key, EncryptProvider.publicKeyBase64());
                return new EncryptRequestKey(encrypt, key, encryptKey);
            } else if (CipherMode.AES.name().equalsIgnoreCase(encrypt)) {
                String key = Cryptos.getBase64EncodeKey();
                String encryptKey = RSAUtils.encryptBase64String(key, EncryptProvider.publicKeyBase64());
                return new EncryptRequestKey(encrypt, key, encryptKey);
            } else {
                log.warn("Unsupported cipher mode for RPC encryption: {}", encrypt);
            }
        } catch (Exception e) {
            log.error("Failed to generateEncryptKey for cipher mode: {}", encrypt, e);
        }

        return new EncryptRequestKey(encrypt, null, null);
    }
}