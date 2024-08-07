package com.eryansky.fastweixin.api.response;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * @author Eryan
 * @date 2016-03-15
 */
public class GetUsersResponse extends BaseResponse {

    private long   total;
    private int    count;
    private Openid data;
    @JSONField(name = "next_openid")
    private String nextOpenid;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Openid getData() {
        return data;
    }

    public void setData(Openid data) {
        this.data = data;
    }

    public String getNextOpenid() {
        return nextOpenid;
    }

    public void setNextOpenid(String nextOpenid) {
        this.nextOpenid = nextOpenid;
    }

    public static class Openid {
        private String[] openid;

        public String[] getOpenid() {
            return openid;
        }

        public void setOpenid(String[] openid) {
            this.openid = openid;
        }
    }
}
