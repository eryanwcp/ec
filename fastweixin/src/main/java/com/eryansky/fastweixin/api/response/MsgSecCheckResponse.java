package com.eryansky.fastweixin.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 内容安全检查响应
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class MsgSecCheckResponse extends BaseResponse {

    /**
     * 结果详情列表
     */
    private List<Detail> detail;

    /**
     * 唯一请求标识
     */
    @JsonProperty("trace_id")
    private String traceId;

    /**
     * 综合结果 1: 正常; 2: 违规
     */
    private Result result;

    public List<Detail> getDetail() {
        return detail;
    }

    public void setDetail(List<Detail> detail) {
        this.detail = detail;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public static class Detail {
        /**
         * 策略类型
         */
        private String strategy;
        /**
         * 错误码，0为正常
         */
        private Integer errcode;
        /**
         * 建议，有risky、pass、review三种
         */
        private String suggest;
        /**
         * 标签标准版标签
         */
        private Integer label;
        /**
         * 置信度，0-100
         */
        private Integer prob;
        /**
         * 命中的自定义关键词
         */
        private String keyword;

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public Integer getErrcode() {
            return errcode;
        }

        public void setErrcode(Integer errcode) {
            this.errcode = errcode;
        }

        public String getSuggest() {
            return suggest;
        }

        public void setSuggest(String suggest) {
            this.suggest = suggest;
        }

        public Integer getLabel() {
            return label;
        }

        public void setLabel(Integer label) {
            this.label = label;
        }

        public Integer getProb() {
            return prob;
        }

        public void setProb(Integer prob) {
            this.prob = prob;
        }

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }
    }

    public static class Result {
        /**
         * 建议，有risky、pass、review三种
         */
        private String suggest;
        /**
         * 命中标签枚举值，100 正常；10001 广告；20001 时政；20002 色情；20003 辱骂；20006 违法犯罪；20008 欺诈；20012 低俗；20013 版权；21000 其他
         */
        private Integer label;

        public String getSuggest() {
            return suggest;
        }

        public void setSuggest(String suggest) {
            this.suggest = suggest;
        }

        public Integer getLabel() {
            return label;
        }

        public void setLabel(Integer label) {
            this.label = label;
        }
    }
}
