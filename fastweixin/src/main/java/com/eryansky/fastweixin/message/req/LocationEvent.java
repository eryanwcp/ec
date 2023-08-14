package com.eryansky.fastweixin.message.req;

public final class LocationEvent extends BaseEvent {

    private final double latitude;
    private final double longitude;
    private final double precision;

    public LocationEvent(double latitude, double longitude, double precision) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.precision = precision;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getPrecision() {
        return precision;
    }

    @Override
    public String toString() {
        return "LocationEvent [latitude=" + latitude + ", longitude="
                + longitude + ", precision=" + precision + ", toUserName="
                + toUserName + ", fromUserName=" + fromUserName
                + ", createTime=" + createTime + ", msgType=" + msgType + "]";
    }

}
