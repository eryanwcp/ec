package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 小程序码（不限数量）生成参数
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class WxaCodeUnlimitParam extends BaseModel {

    /**
     * 最大32个可见字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~，其它字符请自行编码为合法字符
     */
    private String scene;

    /**
     * 默认是主页，页面 page，例如 pages/index/index，根路径前不要斜杠 /
     */
    private String page;

    /**
     * 二维码的宽度，默认430，最小280，最大1280
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
     * 线条颜色
     */
    @JsonProperty("line_color")
    private WxaCodeParam.LineColor lineColor;

    /**
     * 默认值"release"。要打开的小程序版本。
     */
    @JsonProperty("env_version")
    private String envVersion;

    /**
     * 检查 page 是否存在，为 true 时 page 必须是已经发布的小程序存在的页面（否则报错）
     */
    @JsonProperty("check_path")
    private Boolean checkPath;

    public WxaCodeUnlimitParam() {
        this.width = 430;
        this.autoColor = false;
        this.isHyaline = false;
        this.envVersion = "release";
        this.checkPath = true;
    }

    public String getScene() {
        return scene;
    }

    public WxaCodeUnlimitParam setScene(String scene) {
        this.scene = scene;
        return this;
    }

    public String getPage() {
        return page;
    }

    public WxaCodeUnlimitParam setPage(String page) {
        this.page = page;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    public WxaCodeUnlimitParam setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public Boolean getAutoColor() {
        return autoColor;
    }

    public WxaCodeUnlimitParam setAutoColor(Boolean autoColor) {
        this.autoColor = autoColor;
        return this;
    }

    public Boolean getHyaline() {
        return isHyaline;
    }

    public WxaCodeUnlimitParam setHyaline(Boolean hyaline) {
        isHyaline = hyaline;
        return this;
    }

    public WxaCodeParam.LineColor getLineColor() {
        return lineColor;
    }

    public WxaCodeUnlimitParam setLineColor(WxaCodeParam.LineColor lineColor) {
        this.lineColor = lineColor;
        return this;
    }

    public String getEnvVersion() {
        return envVersion;
    }

    public WxaCodeUnlimitParam setEnvVersion(String envVersion) {
        this.envVersion = envVersion;
        return this;
    }

    public Boolean getCheckPath() {
        return checkPath;
    }

    public WxaCodeUnlimitParam setCheckPath(Boolean checkPath) {
        this.checkPath = checkPath;
        return this;
    }
}
