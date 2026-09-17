package com.eryansky.fastweixin.api.response;

import java.util.List;
import java.util.Map;

/**
 * 获取黑名单用户列表响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class GetBlackListResponse extends BaseResponse {

    private Integer total;
    private Integer count;
    private Map<String, List<String>> data;
    private String next_openid;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Map<String, List<String>> getData() {
        return data;
    }

    public void setData(Map<String, List<String>> data) {
        this.data = data;
    }

    public String getNext_openid() {
        return next_openid;
    }

    public void setNext_openid(String next_openid) {
        this.next_openid = next_openid;
    }
}
