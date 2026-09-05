package com.eryansky.core.rpc.advice;

import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.core.rpc.utils.RPCUtils;
import com.eryansky.core.rpc.utils.SerializerFactory;
import com.eryansky.encrypt.anotation.EncryptResponseBody;
import com.eryansky.encrypt.util.RequestEncryptUtils;
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

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        EncryptResponseBody annotation = returnType.getMethodAnnotation(EncryptResponseBody.class);
        if (annotation == null) {
            return false;
        }
        return Boolean.parseBoolean(annotation.enable()) && annotation.handle() == this.getClass();
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        HttpHeaders headers = request.getHeaders();
        String requestEncrypt = Collections3.getFirst(headers.get(RequestEncryptUtils.ENCRYPT));
        String requestEncryptKey = Collections3.getFirst(headers.get(RequestEncryptUtils.ENCRYPT_KEY));
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
            out = RequestEncryptUtils.encryptDataByRequest(requestEncrypt, requestEncryptKey, payload);
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

}