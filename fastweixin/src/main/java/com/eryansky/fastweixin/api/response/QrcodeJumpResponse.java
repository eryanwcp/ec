package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 小程序二维码跳转配置响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class QrcodeJumpResponse extends BaseResponse {

    /**
     * 已设置的二维码规则列表
     */
    @JsonProperty("qrcodejump_list")
    private List<QrcodeJumpItem> qrcodeJumpList;

    /**
     * 已设置的二维码规则总数
     */
    @JsonProperty("qrcodejump_total")
    private Integer qrcodeJumpTotal;

    /**
     * 还可以设置的二维码规则数量
     */
    @JsonProperty("qrcodejump_limit")
    private Integer qrcodeJumpLimit;

    /**
     * 是否可以编辑
     */
    @JsonProperty("qrcodejump_open")
    private Integer qrcodeJumpOpen;

    public List<QrcodeJumpItem> getQrcodeJumpList() {
        return qrcodeJumpList;
    }

    public void setQrcodeJumpList(List<QrcodeJumpItem> qrcodeJumpList) {
        this.qrcodeJumpList = qrcodeJumpList;
    }

    public Integer getQrcodeJumpTotal() {
        return qrcodeJumpTotal;
    }

    public void setQrcodeJumpTotal(Integer qrcodeJumpTotal) {
        this.qrcodeJumpTotal = qrcodeJumpTotal;
    }

    public Integer getQrcodeJumpLimit() {
        return qrcodeJumpLimit;
    }

    public void setQrcodeJumpLimit(Integer qrcodeJumpLimit) {
        this.qrcodeJumpLimit = qrcodeJumpLimit;
    }

    public Integer getQrcodeJumpOpen() {
        return qrcodeJumpOpen;
    }

    public void setQrcodeJumpOpen(Integer qrcodeJumpOpen) {
        this.qrcodeJumpOpen = qrcodeJumpOpen;
    }

    /**
     * 二维码跳转规则
     */
    public static class QrcodeJumpItem {

        /**
         * 二维码规则
         */
        private String prefix;

        /**
         * 小程序功能页面
         */
        @JsonProperty("permit_sub_rule")
        private Integer permitSubRule;

        /**
         * 小程序页面
         */
        @JsonProperty("path")
        private String path;

        /**
         * 测试链接（最多10个）
         */
        @JsonProperty("debug_url")
        private List<String> debugUrl;

        /**
         * 发布标志，1 表示已发布，2 表示未发布
         */
        @JsonProperty("add_status")
        private Integer addStatus;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public Integer getPermitSubRule() {
            return permitSubRule;
        }

        public void setPermitSubRule(Integer permitSubRule) {
            this.permitSubRule = permitSubRule;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public List<String> getDebugUrl() {
            return debugUrl;
        }

        public void setDebugUrl(List<String> debugUrl) {
            this.debugUrl = debugUrl;
        }

        public Integer getAddStatus() {
            return addStatus;
        }

        public void setAddStatus(Integer addStatus) {
            this.addStatus = addStatus;
        }
    }
}
