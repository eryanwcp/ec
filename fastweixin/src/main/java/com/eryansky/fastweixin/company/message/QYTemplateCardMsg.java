package com.eryansky.fastweixin.company.message;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 *
 * @author Eryan
 * @date 2021-09-08
 */
public class QYTemplateCardMsg extends QYBaseMsg {

    @JSONField(name = "template_card")
    private TemplateCard templateCard;

    public QYTemplateCardMsg() {
        this.setMsgType("template_card");
    }

    public TemplateCard getTemplateCard() {
        return templateCard;
    }

    public QYTemplateCardMsg setTemplateCard(TemplateCard templateCard) {
        this.templateCard = templateCard;
        return this;
    }

    public static class TemplateCard{
        /**
         * 模板卡片类型，图文展示型卡片此处填写 “news_notice”,文本通知型卡片填写 “text_notice”
         */
        @JSONField(name = "card_type")
        private String cardType;
        /**
         * 卡片来源样式信息，不需要来源样式可不填写
         */
        @JSONField(name = "source")
        private Source source;
        /**
         * 一级标题
         */
        @JSONField(name = "main_title")
        private MainTitle mainTitle;
        /**
         * 卡片二级垂直内容，该字段可为空数组，但有数据的话需确认对应字段是否必填，列表长度不超过4
         */
        @JSONField(name = "vertical_content_list")
        private List<VerticalContent> verticalContentList;
        /**
         * 二级标题+文本列表，该字段可为空数组，但有数据的话需确认对应字段是否必填，列表长度不超过6
         */
        @JSONField(name = "horizontal_content_list")
        private List<HorizontalContent> horizontalContentList;
        /**
         * 跳转指引样式的列表，该字段可为空数组，但有数据的话需确认对应字段是否必填，列表长度不超过3
         */
        @JSONField(name = "jump_list")
        private List<Jump> jumpList;
        /**
         * 整体卡片的点击跳转事件，news_notice必填本字段
         */
        @JSONField(name = "card_action")
        private CardAction cardAction;

        public TemplateCard() {
        }

        public TemplateCard(String cardType, Source source, MainTitle mainTitle, List<VerticalContent> verticalContentList, List<HorizontalContent> horizontal_content_list, List<Jump> jumpList, CardAction cardAction) {
            this.cardType = cardType;
            this.source = source;
            this.mainTitle = mainTitle;
            this.verticalContentList = verticalContentList;
            this.horizontalContentList = horizontal_content_list;
            this.jumpList = jumpList;
            this.cardAction = cardAction;
        }

        public String getCardType() {
            return cardType;
        }

        public TemplateCard setCardType(String cardType) {
            this.cardType = cardType;
            return this;
        }

        public Source getSource() {
            return source;
        }

        public TemplateCard setSource(Source source) {
            this.source = source;
            return this;
        }

        public MainTitle getMainTitle() {
            return mainTitle;
        }

        public TemplateCard setMainTitle(MainTitle mainTitle) {
            this.mainTitle = mainTitle;
            return this;
        }

        public List<VerticalContent> getVerticalContentList() {
            return verticalContentList;
        }

        public TemplateCard setVerticalContentList(List<VerticalContent> verticalContentList) {
            this.verticalContentList = verticalContentList;
            return this;
        }

        public List<HorizontalContent> getHorizontalContentList() {
            return horizontalContentList;
        }

        public TemplateCard setHorizontalContentList(List<HorizontalContent> horizontalContentList) {
            this.horizontalContentList = horizontalContentList;
            return this;
        }

        public List<Jump> getJumpList() {
            return jumpList;
        }

        public TemplateCard setJumpList(List<Jump> jumpList) {
            this.jumpList = jumpList;
            return this;
        }

        public CardAction getCardAction() {
            return cardAction;
        }

        public TemplateCard setCardAction(CardAction cardAction) {
            this.cardAction = cardAction;
            return this;
        }

        /**
         * 卡片来源样式信息，不需要来源样式可不填写
         */
        public static class Source{
            /**
             * 来源图片的url
             */
            @JSONField(name = "icon_url")
            private String iconUrl;
            /**
             * 源图片的描述，建议不超过20个字
             */
            @JSONField(name = "desc")
            private String desc;

            public Source() {
            }

            public Source(String icon_url, String desc) {
                this.iconUrl = icon_url;
                this.desc = desc;
            }

            public String getIconUrl() {
                return iconUrl;
            }

            public Source setIconUrl(String iconUrl) {
                this.iconUrl= iconUrl;
                return this;
            }

            public String getDesc() {
                return desc;
            }

            public Source setDesc(String desc) {
                this.desc = desc;
                return this;
            }
        }


        /**
         * 一级标题
         */
        public static class MainTitle{
            /**
             * 一级标题，建议不超过36个字
             */
            @JSONField(name = "title")
            private String title;
            /**
             * 标题辅助信息，建议不超过44个字
             */
            @JSONField(name = "desc")
            private String desc;

            public MainTitle() {
            }

            public MainTitle(String title, String desc) {
                this.title = title;
                this.desc = desc;
            }

            public String getTitle() {
                return title;
            }

            public MainTitle setTitle(String title) {
                this.title = title;
                return this;
            }

            public String getDesc() {
                return desc;
            }

            public MainTitle setDesc(String desc) {
                this.desc = desc;
                return this;
            }
        }


        /**
         * 图片样式
         */
        public static class CardImage{
            /**
             * 图片的url
             */
            @JSONField(name = "url")
            private String url;
            /**
             * 图片的宽高比，宽高比要小于2.25，大于1.3，不填该参数默认1.3
             */
            @JSONField(name = "aspect_ratio")
            private Double aspectRatio;

            public CardImage() {
            }

            public CardImage(String url, Double aspectRatio) {
                this.url = url;
                this.aspectRatio = aspectRatio;
            }

            public String getUrl() {
                return url;
            }

            public CardImage setUrl(String url) {
                this.url = url;
                return this;
            }

            public Double getAspectRatio() {
                return aspectRatio;
            }

            public CardImage setAspectRatio(Double aspectRatio) {
                this.aspectRatio = aspectRatio;
                return this;
            }
        }

        /**
         * 卡片二级垂直内容
         */
        public static class VerticalContent{
            /**
             * 卡片二级标题，建议不超过38个字
             */
            @JSONField(name = "title")
            private String title;
            /**
             * 级普通文本，建议不超过160个字
             */
            @JSONField(name = "desc")
            private String desc;

            public VerticalContent() {
            }

            public VerticalContent(String title, String desc) {
                this.title = title;
                this.desc = desc;
            }

            public String getTitle() {
                return title;
            }

            public VerticalContent setTitle(String title) {
                this.title = title;
                return this;
            }

            public String getDesc() {
                return desc;
            }

            public VerticalContent setDesc(String desc) {
                this.desc = desc;
                return this;
            }
        }

        /**
         * 二级标题+文本列表
         */
        public static class HorizontalContent{
            /**
             * 链接类型，0或不填代表不是链接，1 代表跳转url，2 代表下载附件
             */
            @JSONField(name = "type")
            private Integer type;
            /**
             * 二级标题，建议不超过5个字
             */
            private String keyname;
            /**
             * 级文本，如果horizontal_content_list.type是2，该字段代表文件名称（要包含文件类型），建议不超过30个字
             */
            private String value;
            /**
             * 链接跳转的url，horizontal_content_list.type是1时必填
             */
            private String url;
            /**
             * 附件的media_id，horizontal_content_list.type是2时必填
             */
            @JSONField(name = "media_id")
            private String mediaId;

            public HorizontalContent() {
            }

            public HorizontalContent(Integer type, String keyname, String value, String url, String mediaId) {
                this.type = type;
                this.keyname = keyname;
                this.value = value;
                this.url = url;
                this.mediaId = mediaId;
            }

            public Integer getType() {
                return type;
            }

            public HorizontalContent setType(Integer type) {
                this.type = type;
                return this;
            }

            public String getKeyname() {
                return keyname;
            }

            public HorizontalContent setKeyname(String keyname) {
                this.keyname = keyname;
                return this;
            }

            public String getValue() {
                return value;
            }

            public HorizontalContent setValue(String value) {
                this.value = value;
                return this;
            }

            public String getUrl() {
                return url;
            }

            public HorizontalContent setUrl(String url) {
                this.url = url;
                return this;
            }

            public String getMediaId() {
                return mediaId;
            }

            public HorizontalContent setMediaId(String mediaId) {
                this.mediaId = mediaId;
                return this;
            }
        }


        /**
         * 跳转指引样式的列表，该字段可为空数组，但有数据的话需确认对应字段是否必填，列表长度不超过3
         */
        public static class Jump{
            /**
             * 跳转链接类型，0或不填代表不是链接，1 代表跳转url，2 代表跳转小程序
             */
            @JSONField(name = "type")
            private Integer type;
            /**
             * 跳转链接样式的文案内容，建议不超过18个字
             */
            private String title;
            /**
             * 跳转链接的url，jump_list.type是1时必填
             */
            private String url;
            /**
             * 跳转链接的小程序的appid，必须是与当前应用关联的小程序，jump_list.type是2时必填
             */
            @JSONField(name = "appid")
            private String appid;
            /**
             * 跳转链接的小程序的pagepath，jump_list.type是2时选填
             */
            @JSONField(name = "pagepath")
            private String pagepath;

            public Jump() {
            }

            public Jump(Integer type, String title, String url, String appid, String pagepath) {
                this.type = type;
                this.title = title;
                this.url = url;
                this.appid = appid;
                this.pagepath = pagepath;
            }

            public Integer getType() {
                return type;
            }

            public Jump setType(Integer type) {
                this.type = type;
                return this;
            }

            public String getTitle() {
                return title;
            }

            public Jump setTitle(String title) {
                this.title = title;
                return this;
            }

            public String getUrl() {
                return url;
            }

            public Jump setUrl(String url) {
                this.url = url;
                return this;
            }

            public String getAppid() {
                return appid;
            }

            public Jump setAppid(String appid) {
                this.appid = appid;
                return this;
            }

            public String getPagepath() {
                return pagepath;
            }

            public Jump setPagepath(String pagepath) {
                this.pagepath = pagepath;
                return this;
            }
        }


        /**
         * 整体卡片的点击跳转事件，news_notice必填本字段
         */
        public static class CardAction{
            /**
             * 跳转事件类型，1 代表跳转url，2 代表打开小程序。news_notice卡片模版中该字段取值范围为[1,2]
             */
            @JSONField(name = "type")
            private Integer type;
            /**
             * 跳转事件的url，card_action.type是1时必填
             */
            private String url;
            /**
             * 跳转事件的小程序的appid，必须是与当前应用关联的小程序，card_action.type是2时必填
             */
            @JSONField(name = "appid")
            private String appid;
            /**
             * 跳转事件的小程序的pagepath，card_action.type是2时选填
             */
            @JSONField(name = "pagepath")
            private String pagepath;


            public CardAction() {
            }

            public CardAction(Integer type, String url, String appid, String pagepath) {
                this.type = type;
                this.url = url;
                this.appid = appid;
                this.pagepath = pagepath;
            }

            public Integer getType() {
                return type;
            }

            public CardAction setType(Integer type) {
                this.type = type;
                return this;
            }

            public String getUrl() {
                return url;
            }

            public CardAction setUrl(String url) {
                this.url = url;
                return this;
            }

            public String getAppid() {
                return appid;
            }

            public CardAction setAppid(String appid) {
                this.appid = appid;
                return this;
            }

            public String getPagepath() {
                return pagepath;
            }

            public CardAction setPagepath(String pagepath) {
                this.pagepath = pagepath;
                return this;
            }
        }
    }
}
