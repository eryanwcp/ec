package com.eryansky.fastweixin.company.api;

import com.eryansky.fastweixin.api.enums.MediaType;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.company.api.config.QYAPIConfig;
import com.eryansky.fastweixin.company.api.response.DownloadMediaResponse;
import com.eryansky.fastweixin.company.api.response.UploadMediaResponse;
import com.eryansky.fastweixin.util.JSONUtil;
import com.eryansky.fastweixin.util.NetWorkCenter;
import com.eryansky.fastweixin.util.StreamUtil;
import com.eryansky.fastweixin.util.StrUtil;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 *
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class QYMediaAPI extends QYBaseAPI {

    private static final Logger LOG = LoggerFactory.getLogger(QYMediaAPI.class);

    /**
     * 构造方法，设置apiConfig
     *
     * @param config 微信API配置对象
     */
    public QYMediaAPI(QYAPIConfig config) {
        super(config);
    }

    /**
     * 上传媒体文件
     * @param file 媒体文件
     * @param type 媒体文件类型
     * @return 上传结果
     */
    public UploadMediaResponse upload(MediaType type, File file){
        if(type == MediaType.NEWS){
            LOG.debug("企业号媒体素材不包含新闻列表");
            return null;
        }
        UploadMediaResponse response;
        String url = BASE_API_URL + "cgi-bin/media/upload?access_token=#&type=" + type.toString();
        BaseResponse r = executePost(url, null, file);
        response = JSONUtil.toBean(r.getErrmsg(), UploadMediaResponse.class);
        return response;
    }

    /**
     * 下载媒体文件（HttpClient5 重构版）
     * @param mediaId 媒体ID
     * @return 下载结果
     */
    public DownloadMediaResponse download(String mediaId){
        DownloadMediaResponse response = new DownloadMediaResponse();
        String url = BASE_API_URL + "cgi-bin/media/get?access_token=" + config.getAccessToken() + "&media_id=" + mediaId;

        // HttpClient5 超时配置
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(NetWorkCenter.CONNECT_TIMEOUT))
                .setConnectTimeout(Timeout.ofMilliseconds(NetWorkCenter.CONNECT_TIMEOUT))
                .setResponseTimeout(Timeout.ofMilliseconds(NetWorkCenter.CONNECT_TIMEOUT))
                .build();

        // try-with-resources 自动关闭客户端和响应，无需手动 close()
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
             org.apache.hc.client5.http.impl.classic.CloseableHttpResponse r = client.execute(new HttpGet(url))) {

            if (HttpStatus.SC_OK == r.getCode()) {
                InputStream inputStream = r.getEntity().getContent();
                org.apache.hc.core5.http.Header[] headers = r.getHeaders("Content-disposition");

                if (null != headers && headers.length > 0) {
                    // 安全获取文件长度
                    int contentLength = 0;
                    org.apache.hc.core5.http.Header lengthHeader = r.getFirstHeader("Content-Length");
                    if (lengthHeader != null) {
                        contentLength = Integer.parseInt(lengthHeader.getValue());
                    }
                    response.setContent(inputStream, contentLength);

                    DownloadMediaResponse finalResponse = response;
                    Arrays.stream(headers).filter(v->v.getName().equalsIgnoreCase("filename")).findAny().ifPresent(v->{
                        finalResponse.setFileName(v.getValue());
                    });
                } else {
                    // 返回JSON错误信息
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        StreamUtil.copy(inputStream, out);
                        String json = out.toString();
                        response = JSONUtil.toBean(json, DownloadMediaResponse.class);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("下载媒体文件异常", e);
        }

        return response;
    }

}