package com.eryansky.fastweixin.api;

import com.eryansky.fastweixin.api.config.ApiConfig;
import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.api.response.OauthJscode2sessionResponse;
import com.eryansky.fastweixin.util.BeanUtil;
import com.eryansky.fastweixin.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 小程序相关API
 *
 * @author Eryan
 * @date 2024-01-04
 */
public class MiniProgramAPI extends BaseAPI {

    private static final Logger LOG = LoggerFactory.getLogger(MiniProgramAPI.class);

    public MiniProgramAPI(ApiConfig config) {
        super(config);
    }


    /**
     * 小程序登录
     *
     * @param code 授权后得到的code
     * @return token对象
     */
    public OauthJscode2sessionResponse getToken(String code) {
        BeanUtil.requireNonNull(code, "code is null");
        OauthJscode2sessionResponse response = null;
        String url = BASE_API_URL + "sns/jscode2session?appid=" + this.config.getAppid() + "&secret=" + this.config.getSecret() + "&js_code=" + code + "&grant_type=authorization_code";
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        response = JSONUtil.toBean(resultJson, OauthJscode2sessionResponse.class);
        return response;
    }

}
