package com.eryansky.fastweixin.company.api.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.eryansky.fastweixin.api.entity.BaseModel;

import java.util.Map;

/**
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class QYUserExternalProfile extends BaseModel {

    /**
     * 企业对外简称，需从已认证的企业简称中选填。可在“我的企业”页中查看企业简称认证状态。
     */
    @JSONField(name = "external_corp_name")
    private String externalCorpName;
    /**
     * 视频号
     */
    private WechatChannels wechatChannels;
    /**
     * 属性列表
     */
    private ExternalAttr externalAttr;


    public QYUserExternalProfile() {
    }

    /**
     * 视频号
     */
    public static class WechatChannels {

        /**
         * 视频号名字（设置后，成员将对外展示该视频号）
         */
        @JSONField(name = "nicknam")
        private String nicknam;
        /**
         * 对外展示视频号状态。0表示企业视频号已被确认，可正常使用，1表示企业视频号待确认
         */
        @JSONField(name = "status")
        private Integer status;

        public WechatChannels() {
        }

        public WechatChannels(String nicknam, Integer status) {
            this.nicknam = nicknam;
            this.status = status;
        }

        public String getNicknam() {
            return nicknam;
        }

        public void setNicknam(String nicknam) {
            this.nicknam = nicknam;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }


    /**
     * 属性列表
     */
    public static class ExternalAttr {

        /**
         * 属性类型: 0-文本 1-网页 2-小程序
         */
        @JSONField(name = "type")
        private Integer type;
        /**
         * 属性名称： 需要先确保在管理端有创建该属性，否则会忽略
         */
        @JSONField(name = "name")
        private String name;
        /**
         * 文本类型的属性
         */
        private Text text;
        /**
         * 网页类型的属性
         */
        private Web web;
        /**
         * 小程序类型的属性
         */
        private Miniprogram miniprogram;


        public ExternalAttr() {
        }


        /**
         * 文本类型的属性
         */
        public static class Text {

            /**
             * 属性名称： 需要先确保在管理端有创建该属性，否则会忽略
             */
            @JSONField(name = "value")
            private String value;

            public Text() {
            }

            public Text(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }


        /**
         * 网页类型的属性
         */
        public static class Web {

            /**
             * 网页的url,必须包含http或者https头
             */
            @JSONField(name = "url")
            private String url;
            /**
             * 网页的展示标题,长度限制12个UTF8字符
             */
            private String title;

            public Web() {
            }

            public Web(String url, String title) {
                this.url = url;
                this.title = title;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }
        }


        /**
         * 小程序类型的属性
         */
        public static class Miniprogram {

            /**
             * 网小程序appid，必须是有在本企业安装授权的小程序，否则会被忽略
             */
            @JSONField(name = "appid")
            private String appid;
            /**
             * 小程序的展示标题,长度限制12个UTF8字符
             */
            @JSONField(name = "title")
            private String title;

            /**
             * 小程序的页面路径
             */
            @JSONField(name = "pagepath")
            private String pagepath;

            public Miniprogram() {
            }

            public Miniprogram(String appid, String title, String pagepath) {
                this.appid = appid;
                this.title = title;
                this.pagepath = pagepath;
            }

            public String getAppid() {
                return appid;
            }

            public void setAppid(String appid) {
                this.appid = appid;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getPagepath() {
                return pagepath;
            }

            public void setPagepath(String pagepath) {
                this.pagepath = pagepath;
            }
        }

    }


    public QYUserExternalProfile(String externalCorpName, WechatChannels wechatChannels, ExternalAttr externalAttr) {
        this.externalCorpName = externalCorpName;
        this.wechatChannels = wechatChannels;
        this.externalAttr = externalAttr;
    }

    public String getExternalCorpName() {
        return externalCorpName;
    }

    public void setExternalCorpName(String externalCorpName) {
        this.externalCorpName = externalCorpName;
    }

    public WechatChannels getWechatChannels() {
        return wechatChannels;
    }

    public void setWechatChannels(WechatChannels wechatChannels) {
        this.wechatChannels = wechatChannels;
    }

    public ExternalAttr getExternalAttr() {
        return externalAttr;
    }

    public void setExternalAttr(ExternalAttr externalAttr) {
        this.externalAttr = externalAttr;
    }
}
