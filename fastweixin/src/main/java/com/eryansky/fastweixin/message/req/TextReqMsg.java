package com.eryansky.fastweixin.message.req;

public final class TextReqMsg extends BaseReqMsg {

    private final String content;

    public TextReqMsg(String content) {
        super();
        this.content = content;
        setMsgType(ReqType.TEXT);
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "TextReqMsg [content=" + content + ", toUserName=" + toUserName
                + ", fromUserName=" + fromUserName + ", createTime="
                + createTime + ", msgType=" + msgType + ", msgId=" + msgId
                + "]";
    }

}
