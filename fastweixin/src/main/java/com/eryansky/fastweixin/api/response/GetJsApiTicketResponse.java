package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author daxiaoming
 */
public class GetJsApiTicketResponse extends BaseResponse {

    private String ticket;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }
}
