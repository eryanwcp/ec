package com.eryansky.fastweixin.api.response;

import com.eryansky.fastweixin.api.entity.UserCumulate;

import java.util.List;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class GetUserCumulateResponse extends BaseResponse {

    private List<UserCumulate> list;

    public List<UserCumulate> getList() {
        return list;
    }

    public void setList(List<UserCumulate> list) {
        this.list = list;
    }
}
