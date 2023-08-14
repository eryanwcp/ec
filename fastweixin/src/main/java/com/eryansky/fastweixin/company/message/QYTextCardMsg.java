package com.eryansky.fastweixin.company.message;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class QYTextCardMsg extends QYBaseMsg {

    @JSONField(name = "textcard")
    private TextCard textCard;

    public QYTextCardMsg() {
        this.setMsgType("textcard");
    }

    public QYTextCardMsg(String title, String description, String url, String btnTxt) {
        this.textCard = new TextCard(title, description, url, btnTxt);
    }

    public TextCard getTextCard() {
        return textCard;
    }

    public void setTextCard(TextCard textCard) {
        this.textCard = textCard;
    }

    public static class TextCard {

        @JSONField(name = "title")
        private String title;
        @JSONField(name = "description")
        private String description;
        @JSONField(name = "url")
        private String url;
        @JSONField(name = "btntxt")
        private String btnTxt;

        public TextCard(String title, String description, String url, String btnTxt) {
            this.title = title;
            this.description = description;
            this.url = url;
            this.btnTxt = btnTxt;
        }

        public String getTitle() {
            return title;
        }

        public TextCard setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public TextCard setDescription(String description) {
            this.description = description;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public TextCard setUrl(String url) {
            this.url = url;
            return this;
        }

        public String getBtnTxt() {
            return btnTxt;
        }

        public TextCard setBtnTxt(String btnTxt) {
            this.btnTxt = btnTxt;
            return this;
        }
    }
}
