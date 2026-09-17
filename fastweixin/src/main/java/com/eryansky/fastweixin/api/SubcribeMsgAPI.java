package com.eryansky.fastweixin.api;

import com.eryansky.fastweixin.api.config.ApiConfig;
import com.eryansky.fastweixin.api.entity.SubcribeMsg;
import com.eryansky.fastweixin.api.enums.ResultType;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.util.BeanUtil;
import com.eryansky.fastweixin.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 小程序订阅消息 API
 * 包含发送订阅消息、模板管理等功能
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class SubcribeMsgAPI extends BaseAPI {
    private static final Logger LOG = LoggerFactory.getLogger(SubcribeMsgAPI.class);

    public SubcribeMsgAPI(ApiConfig config) {
        super(config);
    }

    /**
     * 发送订阅消息
     * 接口地址: POST /cgi-bin/message/subscribe/send
     *
     * @param msg 消息
     * @return 发送结果
     */
    public BaseResponse send(SubcribeMsg msg) {
        LOG.debug("发送订阅消息......");
        BeanUtil.requireNonNull(msg.getTouser(), "openid is null");
        BeanUtil.requireNonNull(msg.getTemplateId(), "template_id is null");
        BeanUtil.requireNonNull(msg.getData(), "data is null");
        BeanUtil.requireNonNull(msg.getMiniprogramState(), "miniprogram_state is null");
        BeanUtil.requireNonNull(msg.getLang(), "lang is null");
        String url = BASE_API_URL + "cgi-bin/message/subscribe/send?access_token=#";
        BaseResponse r = executePost(url, msg.toJsonString());
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        BaseResponse result = JSONUtil.toBean(resultJson, BaseResponse.class);
        return result;
    }

    // ========================= 模板管理 =========================

    /**
     * 组合模板并添加至帐号下的个人模板库
     * 接口地址: POST /wxaapi/newtmpl/addtemplate
     *
     * @param tid         模板标题id，可通过接口获取，也可登录小程序后台查看获取
     * @param kidList     开发者自行组合好的模板关键词列表，关键词顺序可以自由搭配
     * @param sceneDesc   服务场景描述，15个字以内
     * @return 操作结果（包含priTmplId）
     */
    public BaseResponse addTemplate(String tid, int[] kidList, String sceneDesc) {
        LOG.debug("添加订阅消息模板......");
        BeanUtil.requireNonNull(tid, "tid is null");
        BeanUtil.requireNonNull(kidList, "kidList is null");
        String url = BASE_API_URL + "wxaapi/newtmpl/addtemplate?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("tid", tid);
        params.put("kidList", kidList);
        if (sceneDesc != null) {
            params.put("sceneDesc", sceneDesc);
        }
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 删除帐号下的某个模板
     * 接口地址: POST /cgi-bin/wxaapi/newtmpl/deltemplate
     *
     * @param priTmplId 模板ID
     * @return 删除结果
     */
    public BaseResponse delTemplate(String priTmplId) {
        LOG.debug("删除订阅消息模板......");
        BeanUtil.requireNonNull(priTmplId, "priTmplId is null");
        String url = BASE_API_URL + "cgi-bin/wxaapi/newtmpl/deltemplate?access_token=#";
        Map<String, String> map = new HashMap<String, String>();
        map.put("priTmplId", priTmplId);
        BaseResponse r = executePost(url, JSONUtil.toJson(map));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 获取帐号所属类目下的公共模板标题
     * 接口地址: GET /wxaapi/newtmpl/getpubtemplatetitles
     *
     * @param ids   类目id，多个用逗号隔开
     * @param limit 用于分页，表示拉取limit条记录。最大为30
     * @param start 用于分页，表示从start开始。从0开始计数
     * @return 公共模板标题列表
     */
    public BaseResponse getPubTemplateTitleList(String ids, int limit, int start) {
        LOG.debug("获取公共模板标题列表......");
        BeanUtil.requireNonNull(ids, "ids is null");
        String url = BASE_API_URL + "wxaapi/newtmpl/getpubtemplatetitles?access_token=#&ids=" + ids + "&limit=" + limit + "&start=" + start;
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 获取模板标题下的关键词列表
     * 接口地址: GET /wxaapi/newtmpl/getpubtemplatekeywords
     *
     * @param tid 模板标题id
     * @return 关键词列表
     */
    public BaseResponse getPubTemplateKeyWordsById(String tid) {
        LOG.debug("获取公共模板关键词列表......");
        BeanUtil.requireNonNull(tid, "tid is null");
        String url = BASE_API_URL + "wxaapi/newtmpl/getpubtemplatekeywords?access_token=#&tid=" + tid;
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 获取当前帐号下的个人模板列表
     * 接口地址: GET /wxaapi/newtmpl/gettemplate
     *
     * @return 个人模板列表
     */
    public BaseResponse getTemplateList() {
        LOG.debug("获取个人模板列表......");
        String url = BASE_API_URL + "wxaapi/newtmpl/gettemplate?access_token=#";
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 获取小程序账号的类目
     * 接口地址: GET /wxaapi/newtmpl/getcategory
     *
     * @return 类目列表
     */
    public BaseResponse getCategory() {
        LOG.debug("获取小程序账号的类目......");
        String url = BASE_API_URL + "wxaapi/newtmpl/getcategory?access_token=#";
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }
}
