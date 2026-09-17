package com.eryansky.fastweixin.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 动态消息模板信息
 *
 * 支持以下字段:
 * - member_count: 参与人数（target_state=0时必填）
 * - room_limit:   上限人数（target_state=0时必填）
 * - path:         小程序路径（target_state=1时必填）
 * - version_type: 小程序版本（target_state=1时必填，developer|trial|release）
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class UpdatableMsgTemplateInfo extends BaseModel {

    /**
     * 参数列表
     */
    @JsonProperty("parameter_list")
    private List<ParameterItem> parameterList;

    public UpdatableMsgTemplateInfo() {
    }

    public UpdatableMsgTemplateInfo(List<ParameterItem> parameterList) {
        this.parameterList = parameterList;
    }

    public List<ParameterItem> getParameterList() {
        return parameterList;
    }

    public UpdatableMsgTemplateInfo setParameterList(List<ParameterItem> parameterList) {
        this.parameterList = parameterList;
        return this;
    }

    /**
     * 动态消息参数项
     */
    public static class ParameterItem extends BaseModel {

        /**
         * 参数名
         * - member_count: target_state=0时有效，表示「参与人数」
         * - room_limit:   target_state=0时有效，表示「上限人数」
         * - path:         target_state=1时有效，表示「点击消息进入小程序的页面路径」
         * - version_type: target_state=1时有效，表示「小程序版本」(developer/trial/release)
         */
        private String name;

        /**
         * 参数值
         */
        private String value;

        public ParameterItem() {
        }

        public ParameterItem(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public ParameterItem setName(String name) {
            this.name = name;
            return this;
        }

        public String getValue() {
            return value;
        }

        public ParameterItem setValue(String value) {
            this.value = value;
            return this;
        }

        // ========================= 工厂方法 =========================

        /**
         * 创建「参与人数」参数
         */
        public static ParameterItem memberCount(String count) {
            return new ParameterItem("member_count", count);
        }

        /**
         * 创建「参与人数」参数
         */
        public static ParameterItem memberCount(int count) {
            return new ParameterItem("member_count", String.valueOf(count));
        }

        /**
         * 创建「上限人数」参数
         */
        public static ParameterItem roomLimit(String limit) {
            return new ParameterItem("room_limit", limit);
        }

        /**
         * 创建「上限人数」参数
         */
        public static ParameterItem roomLimit(int limit) {
            return new ParameterItem("room_limit", String.valueOf(limit));
        }

        /**
         * 创建「点击跳转页面路径」参数
         */
        public static ParameterItem path(String pagePath) {
            return new ParameterItem("path", pagePath);
        }

        /**
         * 创建「小程序版本」参数
         *
         * @param versionType developer-开发版, trial-体验版, release-正式版
         */
        public static ParameterItem versionType(String versionType) {
            return new ParameterItem("version_type", versionType);
        }
    }
}
