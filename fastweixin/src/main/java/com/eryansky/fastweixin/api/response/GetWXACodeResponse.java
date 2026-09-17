package com.eryansky.fastweixin.api.response;

/**
 * 小程序码生成响应
 * 注意：成功时返回的是图片二进制数据，失败时返回JSON错误信息
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class GetWXACodeResponse extends BaseResponse {

    /**
     * 小程序码图片的二进制数据（base64编码存储）
     */
    private byte[] buffer;

    /**
     * 图片内容类型
     */
    private String contentType;

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
