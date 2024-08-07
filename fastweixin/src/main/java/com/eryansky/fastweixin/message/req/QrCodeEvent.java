package com.eryansky.fastweixin.message.req;

public final class QrCodeEvent extends BaseEvent {

    private final String eventKey;
    private final String ticket;

    public QrCodeEvent(String eventKey, String ticket) {
        super();
        this.eventKey = eventKey;
        this.ticket = ticket;
    }

    public String getEventKey() {
        return eventKey;
    }

    public String getTicket() {
        return ticket;
    }

    @Override
    public String toString() {
        return "QrCodeEvent [eventKey=" + eventKey + ", ticket=" + ticket
                + ", toUserName=" + toUserName + ", fromUserName="
                + fromUserName + ", createTime=" + createTime + ", msgType="
                + msgType + "]";
    }

}
