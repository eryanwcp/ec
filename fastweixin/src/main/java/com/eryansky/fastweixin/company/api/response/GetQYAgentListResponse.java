package com.eryansky.fastweixin.company.api.response;

import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.company.api.entity.QYAgent;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class GetQYAgentListResponse extends BaseResponse {

    @JsonProperty("agentlist")
    public List<QYAgent> agentList;

    public List<QYAgent> getAgentList() {
        return agentList;
    }

    public void setAgentList(List<QYAgent> agentList) {
        this.agentList = agentList;
    }
}
