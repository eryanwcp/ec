package com.eryansky.fastweixin.message.req;

public final class VideoReqMsg extends BaseReqMsg {

    private final String mediaId;
    private final String thumbMediaId;

    public VideoReqMsg(String mediaId, String thumbMediaId) {
        super();
        this.mediaId = mediaId;
        this.thumbMediaId = thumbMediaId;
        setMsgType(ReqType.VIDEO);
    }

    public String getMediaId() {
        return mediaId;
    }

    public String getThumbMediaId() {
        return thumbMediaId;
    }

    @Override
    public String toString() {
        return "VideoReqMsg [mediaId=" + mediaId + ", thumbMediaId="
                + thumbMediaId + ", toUserName=" + toUserName
                + ", fromUserName=" + fromUserName + ", createTime="
                + createTime + ", msgType=" + msgType + ", msgId=" + msgId
                + "]";
    }

}
