package com.eryansky.fastweixin.company.api.response;

import com.eryansky.fastweixin.api.response.BaseResponse;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * 用户踢下线
 * @author Eryan
 * @date 2024-02-20
 */
public class QYUserOfflineResponse extends BaseResponse {

    private List<QYUserOfflineResult> result = Lists.newArrayList();

    public QYUserOfflineResponse() {
    }

    public QYUserOfflineResponse(List<QYUserOfflineResult> result) {
        this.result = result;
    }

    public List<QYUserOfflineResult> getResult() {
        return result;
    }

    public void setResult(List<QYUserOfflineResult> result) {
        this.result = result;
    }


    public class QYUserOfflineResult {
        private String userid;
        private Integer errcode;
        private String errmsg;

        public QYUserOfflineResult() {
        }

        public QYUserOfflineResult(String userid, Integer errcode, String errmsg) {
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
