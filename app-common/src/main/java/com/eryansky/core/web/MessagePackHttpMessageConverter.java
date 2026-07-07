package com.eryansky.core.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import java.io.IOException;

/**
 * 支持 application/x-msgpack 媒體類型的 HTTP 消息轉換器
 */
public class MessagePackHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    private final ObjectMapper objectMapper;

    public MessagePackHttpMessageConverter(ObjectMapper objectMapper) {
        // 🌟 核心閉環：顯式聲明此轉換器支持的自定義媒體類型：application/x-msgpack
        super(new MediaType("application", "x-msgpack"));
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        // 支持所有 Object 類型的序列化與反序列化
        return true;
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) 
            throws IOException, HttpMessageNotReadableException {
        // 從 HTTP 輸入流中讀取二進制字節，並通過 Jackson MsgPack 反序列化為 DTO
        byte[] bytes = StreamUtils.copyToByteArray(inputMessage.getBody());
        return objectMapper.readValue(bytes, clazz);
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage) 
            throws IOException, HttpMessageNotWritableException {
        // 将 Java 業務對象序列化為 MessagePack 二進制字節，直接寫入 HTTP 響應體
        byte[] bytes = objectMapper.writeValueAsBytes(o);
        outputMessage.getBody().write(bytes);
    }
}