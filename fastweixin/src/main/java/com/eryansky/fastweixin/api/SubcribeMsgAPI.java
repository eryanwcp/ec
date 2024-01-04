package com.eryansky.fastweixin.api;

import com.eryansky.fastweixin.api.config.ApiConfig;
import com.eryansky.fastweixin.api.entity.SubcribeMsg;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.util.BeanUtil;
import com.eryansky.fastweixin.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 订阅消息 api
 */
public class SubcribeMsgAPI extends BaseAPI {
    private static final Logger LOG = LoggerFactory.getLogger(CustomAPI.class);

    public SubcribeMsgAPI(ApiConfig config) {
        super(config);
    }



    /**
     * 发送订阅消息
     *
     * @param msg 消息
     * @return 发送结果
     */
    public BaseResponse send(SubcribeMsg msg) {
        LOG.debug("发送模版消息......");
        BeanUtil.requireNonNull(msg.getTouser(), "openid is null");
        BeanUtil.requireNonNull(msg.getTemplateId(), "template_id is null");
        BeanUtil.requireNonNull(msg.getData(), "data is null");
        BeanUtil.requireNonNull(msg.getMiniprogramState(), "miniprogram_state is null");
        BeanUtil.requireNonNull(msg.getLang(), "lang is null");
//        BeanUtil.requireNonNull(msg.getTopcolor(), "top color is null");
//        BeanUtil.requireNonNull(msg.getUrl(), "url is null");
        String url = BASE_API_URL + "cgi-bin/message/subscribe/send?access_token=#";
        BaseResponse r = executePost(url, msg.toJsonString());
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        BaseResponse result = JSONUtil.toBean(resultJson, BaseResponse.class);
        return result;
    }


    /**
     * 删除模板
     *
     * @param priTmplId 模板ID
     * @return 删除结果
     */
    public BaseResponse delTemplate(String priTmplId) {
        LOG.debug("删除模板......");
        BeanUtil.requireNonNull(priTmplId, "priTmplId is null");
        String url = BASE_API_URL + "cgi-bin/wxaapi/newtmpl/deltemplate?access_token=#";
        Map<String, String> map = new HashMap<String, String>();
        map.put("priTmplId", priTmplId);
        BaseResponse r = executePost(url, JSONUtil.toJson(map));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }


}
