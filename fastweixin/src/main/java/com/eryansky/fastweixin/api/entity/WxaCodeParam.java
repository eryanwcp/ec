package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 小程序码生成参数
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class WxaCodeParam extends BaseModel {

    /**
     * 小程序码的页面路径，不能为空，最大长度 128 字节
     */
    private String path;

    /**
     * 小程序码的宽度，默认430，最小280，最大1280
     */
    private Integer width;

    /**
     * 自动配置线条颜色
     */
    @JsonProperty("auto_color")
    private Boolean autoColor;

    /**
     * 默认是false，是否是透明背景
     */
    @JsonProperty("is_hyaline")
    private Boolean isHyaline;

    /**
     * 线条颜色（auto_color为false时生效），默认 {"r":0,"g":0,"b":0}
     */
    @JsonProperty("line_color")
    private LineColor lineColor;

    /**
     * 默认值"release"。要打开的小程序版本。
     * 正式版为 "release"，体验版为 "trial"，开发版为 "developer"
     */
    @JsonProperty("env_version")
    private String envVersion;

    public WxaCodeParam() {
        this.width = 430;
        this.autoColor = false;
        this.isHyaline = false;
        this.envVersion = "release";
    }

    public String getPath() {
        return path;
    }

    public WxaCodeParam setPath(String path) {
        this.path = path;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    public WxaCodeParam setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public Boolean getAutoColor() {
        return autoColor;
    }

    public WxaCodeParam setAutoColor(Boolean autoColor) {
        this.autoColor = autoColor;
        return this;
    }

    public Boolean getHyaline() {
        return isHyaline;
    }

    public WxaCodeParam setHyaline(Boolean hyaline) {
        isHyaline = hyaline;
        return this;
    }

    public LineColor getLineColor() {
        return lineColor;
    }

    public WxaCodeParam setLineColor(LineColor lineColor) {
        this.lineColor = lineColor;
        return this;
    }

    public String getEnvVersion() {
        return envVersion;
    }

    public WxaCodeParam setEnvVersion(String envVersion) {
        this.envVersion = envVersion;
        return this;
    }

    /**
     * RGB颜色值
     */
    public static class LineColor extends BaseModel {
        private Integer r;
        private Integer g;
        private Integer b;

        public LineColor() {}

        public LineColor(Integer r, Integer g, Integer b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public Integer getR() { return r; }
        public void setR(Integer r) { this.r = r; }
        public Integer getG() { return g; }
        public void setG(Integer g) { this.g = g; }
        public Integer getB() { return b; }
        public void setB(Integer b) { this.b = b; }
    }
}
