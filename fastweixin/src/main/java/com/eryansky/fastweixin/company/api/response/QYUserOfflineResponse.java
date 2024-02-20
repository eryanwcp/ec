package com.eryansky.fastweixin.company.api.response;

import com.eryansky.fastweixin.api.response.BaseResponse;
import com.google.common.collect.Lists;

import java.util.List;

/**
 *
 * @author Eryan
 * @date 2024-02-20
 */
public class QYUserOfflineResponse extends BaseResponse {

    private List<OfflineResult> result = Lists.newArrayList();

    public QYUserOfflineResponse() {
    }

    public QYUserOfflineResponse(List<OfflineResult> result) {
        this.result = result;
    }

    public List<OfflineResult> getResult() {
        return result;
    }

    public void setResult(List<OfflineResult> result) {
        this.result = result;
    }

    // 成员内部类
    public class OfflineResult {
        private String userid;
        private Integer errcode;
        private String errmsg;

        public OfflineResult() {
        }

        public OfflineResult(String userid, Integer errcode, String errmsg) {
            this.userid = userid;
            this.errcode = errcode;
            this.errmsg = errmsg;
        }

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public Integer getErrcode() {
            return errcode;
        }

        public void setErrcode(Integer errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }
    }



}
