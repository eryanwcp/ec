package com.eryansky.common.utils.http;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

/**
 * 回调接口
 *
 * @author Eryan
 * @date 2022-10-13
 */
public interface HttpResponseCallback<T> {

    /**
     * 处理
     * 
     * @return
     */
    T handle(CloseableHttpResponse httpResponse);

}