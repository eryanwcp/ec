package com.eryansky.core.rpc.advice;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.core.rpc.utils.SerializerFactory;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import com.eryansky.encrypt.config.EncryptProvider;
import com.eryansky.encrypt.enums.CipherMode;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 默认加密策略 返回值为
 */
@RestControllerAdvice
public class EncryptRPCResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger log = LoggerFactory.getLogger(EncryptRPCResponseBodyAdvice.class);

    public static final String ENCRYPT = "Encrypt";
    public static final String ENCRYPT_KEY = "Encrypt-Key";
    public static final String HANDLE = "RPC";
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        EncryptResponseBody encrypt = returnType.getMethodAnnotation(EncryptResponseBody.class);
        //如果带有注解且标记为验签，则进行验签操作
        return null != encrypt && !encrypt.defaultHandle() && HANDLE.equals(encrypt.handle());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        HttpHeaders headers = request.getHeaders();
        String requestEncrypt = Collections3.getFirst(headers.get(ENCRYPT));
        String requestEncryptKey = Collections3.getFirst(headers.get(ENCRYPT_KEY));
        String requestSerializer = Collections3.getFirst(headers.get(RPCUtils.HEADER_RPC_SERIALIZER));

        // No encryption requested
        if (StringUtils.isBlank(requestEncrypt)) {
            return body;
        }

        // Serialize once
        byte[] payload = serializeBody(body, requestSerializer);

        // Process encryption according to requested mode
        byte[] out;
        try {
            out = processEncryption(requestEncrypt, requestEncryptKey, payload);
        } catch (Exception e) {
            log.error("Failed to process encryption for mode {}", requestEncrypt, e);
            throw new RuntimeException(e);
        }

        // Write encrypted payload to response
        try {
            response.getHeaders().setContentType(selectedContentType);
            response.getHeaders().setContentLength(out.length);

            OutputStream os = response.getBody();
            if (os == null) {
                throw new IOException("Response OutputStream is null");
            }
            os.write(out);
            os.flush();
            return null;
        } catch (IOException e) {
            log.error("IO error while writing encrypted response", e);
            throw new RuntimeException(e);
        }
    }

    private byte[] serializeBody(Object body,String requestSerializer) {
        try {
            return SerializerFactory.getSerializer(requestSerializer).serialize(body);
        } catch (IOException e) {
            log.error("Failed to serialize response body", e);
            throw new RuntimeException(e);
        }
    }

    private byte[] processEncryption(String mode, String encryptKeyHeader, byte[] input) throws Exception {
        if (CipherMode.SM4.name().equalsIgnoreCase(mode) && StringUtils.isNotBlank(encryptKeyHeader)) {
            String key = tryDecryptKeyHex(encryptKeyHeader);
            return Sm4Utils.encrypt(key, input);
        }

        if (CipherMode.AES.name().equalsIgnoreCase(mode) && StringUtils.isNotBlank(encryptKeyHeader)) {
            String key = tryDecryptKeyBase64(encryptKeyHeader);
            return Cryptos.aesECBEncrypt(input, key);
        }

        if (CipherMode.BASE64.name().equalsIgnoreCase(mode)) {
            return Base64.encodeBase64(input);
        }

        // Unknown/unsupported mode — return original payload
        return input;
    }

    private String tryDecryptKeyHex(String encryptedKey) {
        try {
            return RSAUtils.decryptHexString(encryptedKey, EncryptProvider.privateKeyBase64());
        } catch (Exception e) {
            return encryptedKey;
        }
    }

    private String tryDecryptKeyBase64(String encryptedKey) {
        try {
            return RSAUtils.decryptBase64String(encryptedKey, EncryptProvider.privateKeyBase64());
        } catch (Exception e) {
            return encryptedKey;
        }
    }

}