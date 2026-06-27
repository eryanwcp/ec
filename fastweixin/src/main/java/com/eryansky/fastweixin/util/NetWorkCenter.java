package com.eryansky.fastweixin.util;

import com.eryansky.fastweixin.api.response.BaseResponse;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求客户端操作类，基于org.apache.hc.client5包 5.x版本实现
 */
public final class NetWorkCenter {

    /**
     * 默认连接超时时间(毫秒)
     */
    public static final int     CONNECT_TIMEOUT = 10 * 1000;
    /**
     * 日志输出组件
     */
    private static final Logger LOG             = LoggerFactory.getLogger(NetWorkCenter.class);

    /**
     * 私有化构造器
     * 不允许外界创建实例
     */
    private NetWorkCenter() {
        LOG.warn("Oh,my god!!!How do you call this method?!");
        LOG.warn("You shouldn't create me!!!");
        LOG.warn("Look my doc again!!!");
    }

    /**
     * 发起HTTP POST同步请求
     *
     * @param url       请求对应的URL地址
     * @param paramData 请求所带参数，目前支持JSON格式的参数
     * @param callback  请求收到响应后回调函数
     */
    public static void post(String url, String paramData, ResponseCallback callback) {
        post(url, paramData, null, callback);
    }

    public static BaseResponse post(String url, String paramData) {
        final BaseResponse[] response = new BaseResponse[]{null};
        post(url, paramData, (resultCode, resultJson) -> {
            if (200 == resultCode) {
                BaseResponse r = JSONUtil.toBean(resultJson, BaseResponse.class);
                r.setErrmsg(resultJson);
                response[0] = r;
            } else {//请求本身就失败了
                response[0] = new BaseResponse();
                response[0].setErrcode(String.valueOf(resultCode));
                response[0].setErrmsg("请求失败");
            }
        });
        return response[0];
    }

    /**
     * 发起HTTP POST同步请求
     *
     * @param url       请求对应的URL地址
     * @param paramData 请求所带参数，目前支持JSON格式的参数
     * @param fileList  需要一起发送的文件列表
     * @param callback  请求收到响应后回调函数
     */
    public static void post(String url, String paramData, List<File> fileList, ResponseCallback callback) {
        doRequest(RequestMethod.POST, url, paramData, fileList, callback);
    }

    public static BaseResponse post(String url, String paramData, List<File> fileList) {
        final BaseResponse[] response = new BaseResponse[]{null};
        post(url, paramData, fileList, (resultCode, resultJson) -> {
            if (200 == resultCode) {
                BaseResponse r = JSONUtil.toBean(resultJson, BaseResponse.class);
                if (StrUtil.isBlank(r.getErrcode())) {
                    r.setErrcode("0");
                }
                r.setErrmsg(resultJson);
                response[0] = r;
            } else {//请求本身就失败了
                response[0] = new BaseResponse();
                response[0].setErrcode(String.valueOf(resultCode));
                response[0].setErrmsg("请求失败");
            }
        });
        return response[0];
    }

    /**
     * 发起HTTP GET同步请求
     *
     * @param url      请求对应的URL地址
     * @param paramMap GET请求所带参数Map
     * @param callback 请求收到响应后回调函数
     */
    public static void get(String url, Map<String, String> paramMap, ResponseCallback callback) {
        String paramData = null;
        if (null != paramMap && !paramMap.isEmpty()) {
            StringBuilder buffer = new StringBuilder();
            for (Map.Entry<String, String> param : paramMap.entrySet()) {
                buffer.append(param.getKey()).append("=").append(param.getValue()).append("&");
            }
            paramData = buffer.substring(0, buffer.length() - 1);
        }
        doRequest(RequestMethod.GET, url, paramData, null, callback);
    }

    public static BaseResponse get(String url) {
        final BaseResponse[] response = new BaseResponse[]{null};
        doRequest(RequestMethod.GET, url, null, null, (resultCode, resultJson) -> {
            if (200 == resultCode) {
                BaseResponse r = JSONUtil.toBean(resultJson, BaseResponse.class);
                if (StrUtil.isBlank(r.getErrcode())) {
                    r.setErrcode("0");
                }
                r.setErrmsg(resultJson);
                response[0] = r;
            } else {//请求本身就失败了
                response[0] = new BaseResponse();
                response[0].setErrcode(String.valueOf(resultCode));
                response[0].setErrmsg("请求失败");
            }
        });
        return response[0];
    }

    /**
     * 处理HTTP请求
     */
    private static void doRequest(final RequestMethod method, final String url,
                                  final String paramData, final List<File> fileList, final ResponseCallback callback) {
        if (null == url || url.isEmpty()) {
            LOG.warn("The url is null or empty!!You must give it to me!OK?");
            return;
        }

        boolean haveCallback = true;
        if (null == callback) {
            LOG.warn("--------------no callback block!--------------");
            haveCallback = false;
        }

        LOG.debug("-----------------请求地址:{}-----------------", url);

        // HttpClient5 超时配置
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(CONNECT_TIMEOUT))
                .setConnectTimeout(Timeout.ofMilliseconds(CONNECT_TIMEOUT))
                .setResponseTimeout(Timeout.ofMilliseconds(CONNECT_TIMEOUT))
                .build();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build()) {

            switch (method) {
                case GET:
                    handleGetRequest(client, url, paramData, callback, haveCallback);
                    break;
                case POST:
                    handlePostRequest(client, url, paramData, fileList, callback, haveCallback);
                    break;
                case PUT:
                case DELETE:
                default:
                    LOG.warn("-----------------请求类型:{} 暂不支持-----------------", method);
                    break;
            }

        } catch (Exception e) {
            LOG.error("请求异常:", e);
            LOG.warn("-----------------请求出现异常:{}-----------------", e.toString());
            if (haveCallback) {
                callback.onResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.toString());
            }
        }
    }

    /**
     * 处理GET请求
     */
    private static void handleGetRequest(CloseableHttpClient client, String url, String paramData,
                                         ResponseCallback callback, boolean haveCallback) throws IOException {
        String getUrl = url;
        if (null != paramData) {
            getUrl += "?" + paramData;
        }
        HttpGet httpGet = new HttpGet(getUrl);

        try (org.apache.hc.client5.http.impl.classic.CloseableHttpResponse response = client.execute(httpGet)) {
            long start = System.currentTimeMillis();
            long time = System.currentTimeMillis() - start;
            LOG.debug("本次请求'{}'耗时:{}ms", url.substring(url.lastIndexOf("/") + 1), time);

            int resultCode = response.getCode();
            HttpEntity entity = response.getEntity();
            String resultJson = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            if (HttpStatus.SC_OK == resultCode) {
                LOG.debug("-----------------请求成功-----------------");
                LOG.debug("响应结果:");
                LOG.debug(resultJson);
                if (haveCallback) {
                    callback.onResponse(resultCode, resultJson);
                }
            } else {
                if (haveCallback) {
                    LOG.warn("-----------------请求出现错误，错误码:{}-----------------", resultCode);
                    callback.onResponse(resultCode, resultJson);
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理POST请求（普通JSON/文件上传）
     */
    private static void handlePostRequest(CloseableHttpClient client, String url, String paramData,
                                          List<File> fileList, ResponseCallback callback, boolean haveCallback) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        LOG.debug("请求入参:");
        LOG.debug(paramData);

        // 文件上传
        if (null != fileList && !fileList.isEmpty()) {
            LOG.debug("上传文件...");
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (File file : fileList) {
                if (file.isFile()) {
                    FileBody fb = new FileBody(file, ContentType.DEFAULT_BINARY);
                    builder.addPart("media", fb);
                } else {
                    LOG.warn("The target '{}' not a file,please check and try again!", file.getPath());
                    return;
                }
            }
            if (null != paramData) {
                StringBody stringBody = new StringBody(paramData, ContentType.APPLICATION_JSON);
                builder.addPart("description", stringBody);
            }
            httpPost.setEntity(builder.build());
        } else {
            // 普通JSON请求
            if (null != paramData) {
                StringEntity jsonEntity = new StringEntity(paramData, ContentType.APPLICATION_JSON);
                httpPost.setEntity(jsonEntity);
            }
        }

        try (org.apache.hc.client5.http.impl.classic.CloseableHttpResponse response = client.execute(httpPost)) {
            long start = System.currentTimeMillis();
            long time = System.currentTimeMillis() - start;
            LOG.debug("本次请求'{}'耗时:{}ms", url.substring(url.lastIndexOf("/") + 1), time);

            int resultCode = response.getCode();
            HttpEntity entity = response.getEntity();
            String resultJson = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            if (HttpStatus.SC_OK == resultCode) {
                LOG.debug("-----------------请求成功-----------------");
                LOG.debug("响应结果:");
                LOG.debug(resultJson);
                if (haveCallback) {
                    callback.onResponse(resultCode, resultJson);
                }
            } else {
                if (haveCallback) {
                    LOG.warn("-----------------请求出现错误，错误码:{}-----------------", resultCode);
                    callback.onResponse(resultCode, resultJson);
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 标识HTTP请求类型枚举
     */
    enum RequestMethod {
        GET, POST, PUT, DELETE
    }

    /**
     * 自定义HTTP响应回调接口
     */
    public interface ResponseCallback {
        void onResponse(int resultCode, String resultJson);
    }
}