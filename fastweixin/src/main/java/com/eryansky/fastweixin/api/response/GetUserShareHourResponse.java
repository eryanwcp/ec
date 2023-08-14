package com.eryansky.fastweixin.api.response;

import com.eryansky.fastweixin.api.entity.UserShareHour;

import java.util.List;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class GetUserShareHourResponse extends BaseResponse {

    private List<UserShareHour> list;

    public List<UserShareHour> getList() {
        return list;
    }

    public void setList(List<UserShareHour> list) {
        this.list = list;
    }
}
