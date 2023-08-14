package com.eryansky.fastweixin.message.req;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public final class VoiceReqMsg extends BaseReqMsg {

    private final String mediaId;
    private final String format;
    private final String recognition;

    public VoiceReqMsg(String mediaId, String format, String recognition) {
        super();
        this.mediaId = mediaId;
        this.format = format;
        this.recognition = recognition;
        setMsgType(ReqType.VOICE);
    }

    public String getMediaId() {
        return mediaId;
    }

    public String getFormat() {
        return format;
    }

    public String getRecognition() {
        return recognition;
    }

    @Override
    public String toString() {
        return "VoiceReqMsg [mediaId=" + mediaId + ", format=" + format
                + ", recognition=" + recognition + ", msgId=" + msgId
                + ", toUserName=" + toUserName + ", fromUserName="
                + fromUserName + ", createTime=" + createTime + ", msgType="
                + msgType + "]";
    }

}
