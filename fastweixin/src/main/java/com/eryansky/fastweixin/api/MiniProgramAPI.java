package com.eryansky.fastweixin.api;

import com.eryansky.fastweixin.api.config.ApiConfig;
import com.eryansky.fastweixin.api.entity.*;
import com.eryansky.fastweixin.api.response.*;
import com.eryansky.fastweixin.util.BeanUtil;
import com.eryansky.fastweixin.util.JSONUtil;
import com.eryansky.fastweixin.util.NetWorkCenter;
import com.eryansky.fastweixin.util.StreamUtil;
import com.eryansky.fastweixin.util.StrUtil;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小程序相关API
 * 包含小程序登录、小程序码生成、手机号验证、内容安全等接口
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class MiniProgramAPI extends BaseAPI {

    private static final Logger LOG = LoggerFactory.getLogger(MiniProgramAPI.class);

    public MiniProgramAPI(ApiConfig config) {
        super(config);
    }

    // ========================= 登录相关 =========================

    /**
     * 小程序登录 - code换取session_key和openid
     *
     * @param code 授权后得到的code
     * @return token对象
     */
    public OauthJscode2sessionResponse jscodeToSession(String code) {
        BeanUtil.requireNonNull(code, "code is null");
        OauthJscode2sessionResponse response = null;
        String url = BASE_API_URL + "sns/jscode2session?appid=" + this.config.getAppid() + "&secret=" + this.config.getSecret() + "&js_code=" + code + "&grant_type=authorization_code";
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        response = JSONUtil.toBean(resultJson, OauthJscode2sessionResponse.class);
        return response;
    }

    // ========================= 小程序码相关 =========================

    /**
     * 获取小程序码（适用于需要的码数量较少的业务场景）
     * 接口地址: POST /wxa/getwxacode
     * 注意：通过该接口生成的小程序码，永久有效，有数量限制，详见获取二维码文档。
     *
     * @param param 小程序码参数
     * @return 小程序码响应（成功时返回图片二进制数据）
     */
    public GetWXACodeResponse getWXACode(WxaCodeParam param) {
        BeanUtil.requireNonNull(param, "param is null");
        BeanUtil.requireNonNull(param.getPath(), "path is null");
        LOG.debug("获取小程序码......");
        String url = BASE_API_URL + "wxa/getwxacode?access_token=#";
        return executePostForImage(url, param.toJsonString());
    }

    /**
     * 获取小程序码（适用于需要的码数量极多的业务场景）
     * 接口地址: POST /wxa/getwxacodeunlimit
     * 注意：通过该接口生成的小程序码，永久有效，数量暂无限制。
     *
     * @param param 小程序码参数（不限数量版本）
     * @return 小程序码响应（成功时返回图片二进制数据）
     */
    public GetWXACodeResponse getWXACodeUnlimit(WxaCodeUnlimitParam param) {
        BeanUtil.requireNonNull(param, "param is null");
        BeanUtil.requireNonNull(param.getScene(), "scene is null");
        LOG.debug("获取小程序码(不限数量)......");
        String url = BASE_API_URL + "wxa/getwxacodeunlimit?access_token=#";
        return executePostForImage(url, param.toJsonString());
    }

    /**
     * 获取小程序二维码（适用于需要的码数量较少的业务场景）
     * 接口地址: POST /cgi-bin/wxaapp/createwxaqrcode
     * 注意：通过该接口，仅能生成已发布的小程序的二维码。
     *
     * @param path  扫码进入的小程序页面路径，最大长度 128 字节
     * @param width 二维码的宽度，默认430，最小280，最大1280
     * @return 小程序码响应（成功时返回图片二进制数据）
     */
    public GetWXACodeResponse createQRCode(String path, Integer width) {
        BeanUtil.requireNonNull(path, "path is null");
        LOG.debug("创建小程序二维码......");
        String url = BASE_API_URL + "cgi-bin/wxaapp/createwxaqrcode?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        if (width != null) {
            params.put("width", width);
        }
        return executePostForImage(url, JSONUtil.toJson(params));
    }

    /**
     * 获取小程序二维码（默认宽度）
     */
    public GetWXACodeResponse createQRCode(String path) {
        return createQRCode(path, 430);
    }

    // ========================= 手机号相关 =========================

    /**
     * 手机号快速验证组件
     * 接口地址: POST /wxa/business/getuserphonenumber
     *
     * @param code 手机号获取凭证
     * @return 手机号响应
     */
    public GetPhoneNumberResponse getPhoneNumber(String code) {
        BeanUtil.requireNonNull(code, "code is null");
        LOG.debug("获取手机号......");
        String url = BASE_API_URL + "wxa/business/getuserphonenumber?access_token=#";
        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, GetPhoneNumberResponse.class);
    }

    // ========================= 内容安全 =========================

    /**
     * 文本内容安全审核（适用于UGC文本内容的安全检测）
     * 接口地址: POST /wxa/msg_sec_check
     *
     * @param version 接口版本号，固定传2
     * @param openid  用户的openid
     * @param scene   场景值，1-资料, 2-评论, 3-论坛, 4-社交日志
     * @param content 要检测的文本内容
     * @return 内容安全检查响应
     */
    public MsgSecCheckResponse msgSecCheck(Integer version, String openid, Integer scene, String content) {
        BeanUtil.requireNonNull(openid, "openid is null");
        BeanUtil.requireNonNull(content, "content is null");
        LOG.debug("文本内容安全审核......");
        String url = BASE_API_URL + "wxa/msg_sec_check?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("version", version != null ? version : 2);
        params.put("openid", openid);
        params.put("scene", scene != null ? scene : 2);
        params.put("content", content);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, MsgSecCheckResponse.class);
    }

    /**
     * 文本内容安全审核（简化版，使用默认参数）
     */
    public MsgSecCheckResponse msgSecCheck(String openid, String content) {
        return msgSecCheck(2, openid, 2, content);
    }

    /**
     * 异步校验图片/音频是否含有违法违规内容
     * 接口地址: POST /wxa/media_check_async
     *
     * @param mediaUrl 要检测的多媒体url
     * @param mediaType 1-音频; 2-图片
     * @param version   接口版本号，固定传2
     * @param openid    用户的openid
     * @param scene     场景值
     * @return 异步审核响应
     */
    public MediaCheckAsyncResponse mediaCheckAsync(String mediaUrl, Integer mediaType, Integer version, String openid, Integer scene) {
        BeanUtil.requireNonNull(mediaUrl, "mediaUrl is null");
        BeanUtil.requireNonNull(openid, "openid is null");
        LOG.debug("异步内容安全审核......");
        String url = BASE_API_URL + "wxa/media_check_async?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("media_url", mediaUrl);
        params.put("media_type", mediaType);
        params.put("version", version != null ? version : 2);
        params.put("openid", openid);
        params.put("scene", scene != null ? scene : 2);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, MediaCheckAsyncResponse.class);
    }

    // ========================= URL Scheme / URL Link / Short Link =========================

    /**
     * 获取小程序Scheme码
     * 接口地址: POST /wxa/generatescheme
     *
     * @param param Scheme生成参数
     * @return Scheme生成响应
     */
    public GenerateSchemeResponse generateScheme(SchemeParam param) {
        BeanUtil.requireNonNull(param, "param is null");
        LOG.debug("生成小程序Scheme......");
        String url = BASE_API_URL + "wxa/generatescheme?access_token=#";
        BaseResponse r = executePost(url, param.toJsonString());
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, GenerateSchemeResponse.class);
    }

    /**
     * 获取小程序URL Link
     * 接口地址: POST /wxa/generate_urllink
     *
     * @param param URL Link生成参数
     * @return URL Link生成响应
     */
    public GenerateUrlLinkResponse generateUrlLink(UrlLinkParam param) {
        BeanUtil.requireNonNull(param, "param is null");
        LOG.debug("生成小程序URL Link......");
        String url = BASE_API_URL + "wxa/generate_urllink?access_token=#";
        BaseResponse r = executePost(url, param.toJsonString());
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, GenerateUrlLinkResponse.class);
    }

    /**
     * 获取小程序Short Link
     * 接口地址: POST /wxa/genwxashortlink
     *
     * @param pageUrl      通过 Short Link 进入的小程序页面路径
     * @param pageTitle    页面标题
     * @param isPermanent  默认值false。生成的 Short Link 类型，短期有效：false，永久有效：true
     * @return Short Link生成响应
     */
    public GenerateShortLinkResponse generateShortLink(String pageUrl, String pageTitle, Boolean isPermanent) {
        BeanUtil.requireNonNull(pageUrl, "pageUrl is null");
        LOG.debug("生成小程序Short Link......");
        String url = BASE_API_URL + "wxa/genwxashortlink?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("page_url", pageUrl);
        if (StrUtil.isNotBlank(pageTitle)) {
            params.put("page_title", pageTitle);
        }
        if (isPermanent != null) {
            params.put("is_permanent", isPermanent);
        }
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, GenerateShortLinkResponse.class);
    }

    // ========================= 支付相关 =========================

    /**
     * 支付后获取用户UnionID
     * 接口地址: GET /wxa/getpaidunionid
     *
     * @param openid        用户openid
     * @param transactionId 微信支付订单号
     * @return 包含unionid的响应
     */
    public OauthJscode2sessionResponse getPaidUnionId(String openid, String transactionId) {
        BeanUtil.requireNonNull(openid, "openid is null");
        BeanUtil.requireNonNull(transactionId, "transactionId is null");
        LOG.debug("支付后获取UnionID(transactionId)......");
        String url = BASE_API_URL + "wxa/getpaidunionid?access_token=#&openid=" + openid + "&transaction_id=" + transactionId;
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, OauthJscode2sessionResponse.class);
    }

    /**
     * 支付后获取用户UnionID（通过商户订单号）
     *
     * @param openid     用户openid
     * @param mchId      微信支付分配的商户号
     * @param outTradeNo 微信支付商户订单号
     * @return 包含unionid的响应
     */
    public OauthJscode2sessionResponse getPaidUnionIdByOutTradeNo(String openid, String mchId, String outTradeNo) {
        BeanUtil.requireNonNull(openid, "openid is null");
        BeanUtil.requireNonNull(mchId, "mchId is null");
        BeanUtil.requireNonNull(outTradeNo, "outTradeNo is null");
        LOG.debug("支付后获取UnionID(outTradeNo)......");
        String url = BASE_API_URL + "wxa/getpaidunionid?access_token=#&openid=" + openid + "&mch_id=" + mchId + "&out_trade_no=" + outTradeNo;
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, OauthJscode2sessionResponse.class);
    }

    // ========================= 代码审核/版本管理 =========================

    /**
     * 提交代码审核
     * 接口地址: POST /wxa/submit_audit
     *
     * @param item 提交审核项（包含 address, tag, title, item_id, item_name 等）
     * @return 审核编号 (auditid)
     */
    public BaseResponse submitAudit(Map<String, Object> item) {
        BeanUtil.requireNonNull(item, "item is null");
        LOG.debug("提交代码审核......");
        String url = BASE_API_URL + "wxa/submit_audit?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("item_list", List.of(item));
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 查询指定版本的审核状态
     * 接口地址: POST /wxa/get_auditstatus
     *
     * @param auditId 审核编号
     * @return 审核状态
     */
    public BaseResponse getAuditStatus(Long auditId) {
        BeanUtil.requireNonNull(auditId, "auditId is null");
        LOG.debug("查询审核状态......");
        String url = BASE_API_URL + "wxa/get_auditstatus?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("auditid", auditId);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 查询最新一次审核状态
     * 接口地址: GET /wxa/get_latest_auditstatus
     *
     * @return 最新审核状态
     */
    public BaseResponse getLatestAuditStatus() {
        LOG.debug("查询最新审核状态......");
        String url = BASE_API_URL + "wxa/get_latest_auditstatus?access_token=#";
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 撤回审核
     * 接口地址: GET /wxa/undocodeaudit
     *
     * @return 操作结果
     */
    public BaseResponse undoAudit() {
        LOG.debug("撤回审核......");
        String url = BASE_API_URL + "wxa/undocodeaudit?access_token=#";
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 发布已通过审核的小程序版本
     * 接口地址: POST /wxa/release
     *
     * @return 操作结果
     */
    public BaseResponse release() {
        LOG.debug("发布小程序......");
        String url = BASE_API_URL + "wxa/release?access_token=#";
        BaseResponse r = executePost(url, "{}");
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 分阶段发布小程序版本（灰度发布）
     * 接口地址: POST /wxa/release
     *
     * @param grayPercentage 灰度百分比 1-100
     * @return 操作结果
     */
    public BaseResponse releaseGray(Integer grayPercentage) {
        BeanUtil.requireNonNull(grayPercentage, "grayPercentage is null");
        LOG.debug("灰度发布小程序......");
        String url = BASE_API_URL + "wxa/release?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("gray_percentage", grayPercentage);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 版本回退
     * 接口地址: GET /wxa/revertcoderelease
     *
     * @param action       删除指定模板，0 表示下架线上版本，1 表示下架审核版本，2 表示回退线上版本
     * @param appVersion   小程序版本，0表示正式版本，1表示体验版本，2表示开发版本
     * @return 操作结果
     */
    public BaseResponse revertCodeRelease(Integer action, Integer appVersion) {
        LOG.debug("版本回退......");
        String url = BASE_API_URL + "wxa/revertcoderelease?access_token=#";
        if (action != null) {
            url += "&action=" + action;
        }
        if (appVersion != null) {
            url += "&app_version=" + appVersion;
        }
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 获取可回退的小程序版本
     * 接口地址: GET /wxa/gettemplist
     *
     * @return 可回退的版本列表
     */
    public BaseResponse getRevertReleaseList() {
        LOG.debug("获取可回退版本......");
        String url = BASE_API_URL + "wxa/gettemplist?access_token=#";
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 获取体验版二维码信息
     * 接口地址: GET /wxa/get_qrcode
     *
     * @param path 扫码进入小程序的页面路径
     * @return 二维码信息
     */
    public GetWXACodeResponse getQrcode(String path) {
        LOG.debug("获取体验版二维码......");
        String url = BASE_API_URL + "wxa/get_qrcode?access_token=#";
        if (StrUtil.isNotBlank(path)) {
            url += "&path=" + path;
        }
        return executeGetForImage(url);
    }

    // ========================= 直播相关 =========================

    /**
     * 获取直播间列表
     * 接口地址: POST /wxa/business/getliveinfo
     *
     * @param start 起始房间，0表示从第一个房间开始拉取
     * @param limit 拉取的房间数量，0表示拉取全部房间，最多拉取1000间
     * @return 直播间列表
     */
    public GetLiveInfoResponse getLiveInfo(int start, int limit) {
        LOG.debug("获取直播间列表......");
        String url = BASE_API_URL + "wxa/business/getliveinfo?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("start", start);
        params.put("limit", limit);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, GetLiveInfoResponse.class);
    }

    /**
     * 获取直播间回放
     * 接口地址: POST /wxa/business/getlivereplay
     *
     * @param action 获取回放，0表示获取直播间回放列表，1表示获取单个直播间回放列表
     * @param roomId 直播间ID（当action=1时必填）
     * @param start  起始回放，0表示从第一个回放开始拉取
     * @param limit  拉取的回放数量，0表示拉取全部回放
     * @return 直播回放列表
     */
    public GetLiveReplayResponse getLiveReplay(int action, Long roomId, int start, int limit) {
        LOG.debug("获取直播回放......");
        String url = BASE_API_URL + "wxa/business/getlivereplay?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("action", action);
        if (roomId != null) {
            params.put("room_id", roomId);
        }
        params.put("start", start);
        params.put("limit", limit);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, GetLiveReplayResponse.class);
    }

    // ========================= 扫普通链接二维码打开小程序 =========================

    /**
     * 获取已设置的二维码规则
     * 接口地址: POST /cgi-bin/wxopen/qrcodejumpget
     *
     * @return 二维码跳转规则列表
     */
    public QrcodeJumpResponse getQrcodeJump() {
        LOG.debug("获取二维码跳转规则......");
        String url = BASE_API_URL + "cgi-bin/wxopen/qrcodejumpget?access_token=#";
        BaseResponse r = executePost(url, "{}");
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, QrcodeJumpResponse.class);
    }

    /**
     * 增加或修改二维码规则
     * 接口地址: POST /cgi-bin/wxopen/qrcodejumpadd
     *
     * @param prefix        二维码规则
     * @param permitSubRule 是否占用符合规则的所有子规则，1-占用所有子规则，2-只占用指定路径
     * @param path          小程序功能页面
     * @param open          测试范围：1-开发版（配置只对开发者生效），2-体验版（配置对体验版小程序生效），3-正式版（配置对正式版小程序生效）
     * @param debugUrl      测试链接（选填）
     * @return 操作结果
     */
    public BaseResponse addQrcodeJump(String prefix, Integer permitSubRule, String path, Integer open, List<String> debugUrl) {
        BeanUtil.requireNonNull(prefix, "prefix is null");
        BeanUtil.requireNonNull(path, "path is null");
        LOG.debug("添加二维码跳转规则......");
        String url = BASE_API_URL + "cgi-bin/wxopen/qrcodejumpadd?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("prefix", prefix);
        params.put("permit_sub_rule", permitSubRule);
        params.put("path", path);
        params.put("open", open);
        if (debugUrl != null && !debugUrl.isEmpty()) {
            params.put("debug_url", debugUrl);
        }
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 发布已设置的二维码规则
     * 接口地址: POST /cgi-bin/wxopen/qrcodejumppublish
     *
     * @param prefix 二维码规则
     * @return 操作结果
     */
    public BaseResponse publishQrcodeJump(String prefix) {
        BeanUtil.requireNonNull(prefix, "prefix is null");
        LOG.debug("发布二维码跳转规则......");
        String url = BASE_API_URL + "cgi-bin/wxopen/qrcodejumppublish?access_token=#";
        Map<String, String> params = new HashMap<>();
        params.put("prefix", prefix);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 删除已设置的二维码规则
     * 接口地址: POST /cgi-bin/wxopen/qrcodejumpdelete
     *
     * @param prefix 二维码规则
     * @return 操作结果
     */
    public BaseResponse deleteQrcodeJump(String prefix) {
        BeanUtil.requireNonNull(prefix, "prefix is null");
        LOG.debug("删除二维码跳转规则......");
        String url = BASE_API_URL + "cgi-bin/wxopen/qrcodejumpdelete?access_token=#";
        Map<String, String> params = new HashMap<>();
        params.put("prefix", prefix);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 设置所有二维码规则是否开启
     * 接口地址: POST /cgi-bin/wxopen/qrcodejumpset
     *
     * @param open 是否开启，1-开启，2-关闭
     * @return 操作结果
     */
    public BaseResponse setQrcodeJump(Integer open) {
        BeanUtil.requireNonNull(open, "open is null");
        LOG.debug("设置二维码跳转开关......");
        String url = BASE_API_URL + "cgi-bin/wxopen/qrcodejumpset?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("open", open);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    // ========================= 小程序域名管理 =========================

    /**
     * 设置服务器域名
     * 接口地址: POST /wxa/modify_domain
     *
     * @param requestDomain    request合法域名列表
     * @param wsRequestDomain  socket合法域名列表
     * @param uploadDomain     uploadFile合法域名列表
     * @param downloadDomain   downloadFile合法域名列表
     * @param action           add添加, delete删除, set覆盖, get获取
     * @return 操作结果
     */
    public BaseResponse modifyDomain(String action, List<String> requestDomain, List<String> wsRequestDomain,
                                     List<String> uploadDomain, List<String> downloadDomain) {
        LOG.debug("设置服务器域名......");
        String url = BASE_API_URL + "wxa/modify_domain?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("action", action);
        if (requestDomain != null) params.put("requestdomain", requestDomain);
        if (wsRequestDomain != null) params.put("wsrequestdomain", wsRequestDomain);
        if (uploadDomain != null) params.put("uploaddomain", uploadDomain);
        if (downloadDomain != null) params.put("downloaddomain", downloadDomain);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    /**
     * 设置业务域名
     * 接口地址: POST /wxa/setwebviewdomain
     *
     * @param action  add添加, delete删除, set覆盖, get获取
     * @param domains 业务域名列表
     * @return 操作结果
     */
    public BaseResponse setWebViewDomain(String action, List<String> domains) {
        LOG.debug("设置业务域名......");
        String url = BASE_API_URL + "wxa/setwebviewdomain?access_token=#";
        Map<String, Object> params = new HashMap<>();
        params.put("action", action);
        if (domains != null) params.put("webviewdomain", domains);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        return JSONUtil.toBean(resultJson, BaseResponse.class);
    }

    // ========================= 内部方法 =========================

    /**
     * 发送GET请求并返回图片响应
     * 用于获取体验版二维码等返回图片的GET接口
     */
    private GetWXACodeResponse executeGetForImage(String url) {
        GetWXACodeResponse response = new GetWXACodeResponse();
        String getUrl = url.replace("#", config.getAccessToken());

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(NetWorkCenter.CONNECT_TIMEOUT))
                .setConnectTimeout(Timeout.ofMilliseconds(NetWorkCenter.CONNECT_TIMEOUT))
                .setResponseTimeout(Timeout.ofMilliseconds(NetWorkCenter.CONNECT_TIMEOUT))
                .build();

        HttpGet httpGet = new HttpGet(getUrl);

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
             org.apache.hc.client5.http.impl.classic.CloseableHttpResponse httpResponse = client.execute(httpGet)) {

            int statusCode = httpResponse.getCode();
            HttpEntity responseEntity = httpResponse.getEntity();

            if (HttpStatus.SC_OK == statusCode) {
                String contentType = responseEntity.getContentType();
                if (contentType != null && (contentType.contains("image") || contentType.contains("octet-stream"))) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    InputStream inputStream = responseEntity.getContent();
                    StreamUtil.copy(inputStream, baos);
                    response.setBuffer(baos.toByteArray());
                    response.setContentType(contentType);
                    response.setErrcode("0");
                    response.setErrmsg("ok");
                } else {
                    String resultJson = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                    LOG.error("获取体验版二维码失败: {}", resultJson);
                    BaseResponse errorResp = JSONUtil.toBean(resultJson, BaseResponse.class);
                    response.setErrcode(errorResp.getErrcode());
                    response.setErrmsg(errorResp.getErrmsg());
                }
            } else {
                response.setErrcode(String.valueOf(statusCode));
                response.setErrmsg("请求失败");
            }
        } catch (Exception e) {
            LOG.error("获取体验版二维码IO异常", e);
            response.setErrcode("-1");
            response.setErrmsg("IO异常: " + e.getMessage());
        }

        return response;
    }

    /**
     * 发送POST请求并返回图片响应
     * 用于小程序码等返回图片的接口
     */
    private GetWXACodeResponse executePostForImage(String url, String json) {
        GetWXACodeResponse response = new GetWXACodeResponse();
        String postUrl = url.replace("#", config.getAccessToken());

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(NetWorkCenter.CONNECT_TIMEOUT))
                .setConnectTimeout(Timeout.ofMilliseconds(NetWorkCenter.CONNECT_TIMEOUT))
                .setResponseTimeout(Timeout.ofMilliseconds(NetWorkCenter.CONNECT_TIMEOUT))
                .build();

        HttpPost httpPost = new HttpPost(postUrl);
        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
             org.apache.hc.client5.http.impl.classic.CloseableHttpResponse httpResponse = client.execute(httpPost)) {

            int statusCode = httpResponse.getCode();
            HttpEntity responseEntity = httpResponse.getEntity();

            if (HttpStatus.SC_OK == statusCode) {
                String contentType = responseEntity.getContentType();
                // 判断返回的是图片还是JSON错误信息
                if (contentType != null && (contentType.contains("image") || contentType.contains("octet-stream"))) {
                    // 成功：返回图片
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    InputStream inputStream = responseEntity.getContent();
                    StreamUtil.copy(inputStream, baos);
                    response.setBuffer(baos.toByteArray());
                    response.setContentType(contentType);
                    response.setErrcode("0");
                    response.setErrmsg("ok");
                } else {
                    // 失败：返回JSON错误信息
                    String resultJson = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                    LOG.error("获取小程序码失败: {}", resultJson);
                    BaseResponse errorResp = JSONUtil.toBean(resultJson, BaseResponse.class);
                    response.setErrcode(errorResp.getErrcode());
                    response.setErrmsg(errorResp.getErrmsg());
                }
            } else {
                response.setErrcode(String.valueOf(statusCode));
                response.setErrmsg("请求失败");
            }
        } catch (Exception e) {
            LOG.error("获取小程序码IO异常", e);
            response.setErrcode("-1");
            response.setErrmsg("IO异常: " + e.getMessage());
        }

        return response;
    }
}
