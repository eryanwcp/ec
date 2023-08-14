package com.eryansky.fastweixin.company.message;

import com.eryansky.fastweixin.api.entity.BaseModel;
import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class QYBaseMsg extends BaseModel implements Serializable {

    public static final class Safe {
        public static final String YES = "1";// 保密消息
        public static final String NO  = "0";// 非保密消息
    }

    @JSONField(name = "touser")
    private String toUser;
    @JSONField(name = "toparty")
    private String toParty;
    @JSONField(name = "totag")
    private String toTag;
    @JSONField(name = "msgtype")
    private String msgType;
    @JSONField(name = "agentid")
    private String agentId;
    @JSONField(name = "safe")
    private String safe = Safe.NO;
    /**
     * 表示是否开启id转译，0表示否，1表示是，默认0。仅第三方应用需要用到，企业自建应用可以忽略。
     */
    @JSONField(name = "enable_id_trans")
    private Integer enableIdTrans;
    /**
     * 表示是否开启重复消息检查，0表示否，1表示是，默认0
     */
    @JSONField(name = "enable_duplicate_check")
    private Integer enableDuplicateCheck;
    /**
     * 表示是否重复消息检查的时间间隔，默认1800s，最大不超过4小时
     */
    @JSONField(name = "duplicate_check_interval")
    private Integer duplicateCheckInterval;

    public String getToUser() {
        return toUser;
    }

    public QYBaseMsg setToUser(String toUser) {
        this.toUser = toUser;
        return this;
    }

    public String getToParty() {
        return toParty;
    }

    public QYBaseMsg setToParty(String toParty) {
        this.toParty = toParty;
        return this;
    }

    public String getToTag() {
        return toTag;
    }

    public QYBaseMsg setToTag(String toTag) {
        this.toTag = toTag;
        return this;
    }

    public String getMsgType() {
        return msgType;
    }

    public QYBaseMsg setMsgType(String msgType) {
        this.msgType = msgType;
        return this;
    }

    public String getAgentId() {
        return agentId;
    }

    public QYBaseMsg setAgentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    public String getSafe() {
        return safe;
    }

    public QYBaseMsg setSafe(String safe) {
        this.safe = safe;
        return this;
    }

    public Integer getEnableIdTrans() {
        return enableIdTrans;
    }

    public QYBaseMsg setEnableIdTrans(Integer enableIdTrans) {
        this.enableIdTrans = enableIdTrans;
        return this;
    }

    public Integer getEnableDuplicateCheck() {
        return enableDuplicateCheck;
    }

    public QYBaseMsg setEnableDuplicateCheck(Integer enableDuplicateCheck) {
        this.enableDuplicateCheck = enableDuplicateCheck;
        return this;
    }

    public Integer getDuplicateCheckInterval() {
        return duplicateCheckInterval;
    }

    public QYBaseMsg setDuplicateCheckInterval(Integer duplicateCheckInterval) {
        this.duplicateCheckInterval = duplicateCheckInterval;
        return this;
    }
}
