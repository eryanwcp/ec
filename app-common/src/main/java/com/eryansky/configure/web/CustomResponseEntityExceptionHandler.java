package com.eryansky.configure.web;

import com.eryansky.common.utils.mapper.JsonMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers, HttpStatus status, WebRequest request) {
        String method = ex.getMethod();
        Set<HttpMethod> supportedHttpMethods = ex.getSupportedHttpMethods();
        String body = ((ServletWebRequest) request).getRequest().getRequestURI() + " 不支持的请求类型：" + method + "，支持的请求类型：" + JsonMapper.toJsonString(supportedHttpMethods);
        logger.error(body);
        Map<String, Object> map = new HashMap<>();
        map.put("body", body);

        pageNotFoundLogger.warn(ex.getMessage());

        if (!CollectionUtils.isEmpty(supportedHttpMethods)) {
            headers.setAllow(supportedHttpMethods);
        }
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

}
