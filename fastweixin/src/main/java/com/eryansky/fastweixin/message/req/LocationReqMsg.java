package com.eryansky.fastweixin.message.req;

public final class LocationReqMsg extends BaseReqMsg {

    private final double locationX;
    private final double locationY;
    private final int    scale;
    private final String label;

    public LocationReqMsg(double locationX, double locationY, int scale,
                          String label) {
        super();
        this.locationX = locationX;
        this.locationY = locationY;
        this.scale = scale;
        this.label = label;
        setMsgType(ReqType.LOCATION);
    }

    public double getLocationX() {
        return locationX;
    }

    public double getLocationY() {
        return locationY;
    }

    public int getScale() {
        return scale;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "LocationReqMsg [locationX=" + locationX + ", locationY="
                + locationY + ", scale=" + scale + ", label=" + label
                + ", toUserName=" + toUserName + ", fromUserName="
                + fromUserName + ", createTime=" + createTime + ", msgType="
                + msgType + ", msgId=" + msgId + "]";
    }

}
