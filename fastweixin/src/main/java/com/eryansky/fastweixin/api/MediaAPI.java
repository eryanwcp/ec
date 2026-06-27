package com.eryansky.fastweixin.api;

import com.eryansky.fastweixin.api.config.ApiConfig;
import com.eryansky.fastweixin.api.entity.Article;
import com.eryansky.fastweixin.api.enums.MediaType;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.api.response.DownloadMediaResponse;
import com.eryansky.fastweixin.api.response.UploadImgResponse;
import com.eryansky.fastweixin.api.response.UploadMediaResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多媒体资源API
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class MediaAPI extends BaseAPI {

    private static final Logger LOG = LoggerFactory.getLogger(MediaAPI.class);

    public MediaAPI(ApiConfig config) {
        super(config);
    }

    /**
     * 上传资源，会在微信服务器上保存3天，之后会被删除
     *
     * @param type 资源类型
     * @param file 需要上传的文件
     * @return 响应对象
     */
    public UploadMediaResponse uploadMedia(MediaType type, File file) {
        UploadMediaResponse response;
        String url = "http://file.api.weixin.qq.com/cgi-bin/media/upload?access_token=#&type=" + type.toString();
        BaseResponse r = executePost(url, null, file);
        response = JSONUtil.toBean(r.getErrmsg(), UploadMediaResponse.class);
        return response;
    }

    /**
     * 上传群发文章素材。
     *
     * @param articles 上传的文章信息
     * @return 响应对象
     */
    public UploadMediaResponse uploadNews(List<Article> articles){
        UploadMediaResponse response;
        String url = BASE_API_URL + "cgi-bin/media/uploadnews?access_token=#";
        final Map<String, Object> params = new HashMap<>();
        params.put("articles", articles);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        response = JSONUtil.toBean(r.getErrmsg(), UploadMediaResponse.class);
        return response;
    }

    /**
     * 上传群发消息图片素材
     */
    public UploadImgResponse uploadImg(File file){
        UploadImgResponse response;
        String url = "https://api.weixin.qq.com/cgi-bin/media/uploadimg?access_token=#";
        BaseResponse r = executePost(url, null, file);
        response = JSONUtil.toBean(r.getErrmsg(), UploadImgResponse.class);
        return response;
    }

    /**
     * 下载资源，HttpClient5 重构版
     *
     * @param mediaId 微信提供的资源唯一标识
     * @return 响应对象
     */
    public DownloadMediaResponse downloadMedia(String mediaId) {
        DownloadMediaResponse response = new DownloadMediaResponse();
        String url = "http://file.api.weixin.qq.com/cgi-bin/media/get?access_token=" + this.config.getAccessToken() + "&media_id=" + mediaId;

        // HttpClient5 超时配置
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(NetWorkCenter.CONNECT_TIMEOUT))
                .setConnectTimeout(Timeout.ofMilliseconds(NetWorkCenter.CONNECT_TIMEOUT))
                .setResponseTimeout(Timeout.ofMilliseconds(NetWorkCenter.CONNECT_TIMEOUT))
                .build();

        HttpGet get = new HttpGet(url);

        // HttpClient5 自动关闭资源
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
             org.apache.hc.client5.http.impl.classic.CloseableHttpResponse r = client.execute(get)){

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
        } catch (IOException e) {
            LOG.error("IO处理异常", e);
        }
        return response;
    }

}
