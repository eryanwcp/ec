package com.eryansky.modules.sys.web;

import com.eryansky.common.model.R;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.core.security.jwt.JWTUtils;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.modules.sys.vo.OAuth2Client;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 系统内置 OAuth2 认证 Controller
 */
@RestController
@RequestMapping("${adminPath}/sys/oauth")
public class Oauth2Controller {

    private static final Logger log = LoggerFactory.getLogger(Oauth2Controller.class);

    /** 默认 Access Token 有效期 (单位：秒) */
    private static final long DEFAULT_EXPIRE_SECONDS = 7200L;
    /** 默认 单点登录Token 有效期 (单位：秒) */
    private static final long DEFAULT_EXPIRE_SSO_SECONDS = 600L;
    /**
     * Access Token 认证授权
     */
    @PostMapping("token")
    public R<Map<String, Object>> accessToken(@RequestParam("client_id") String clientId,
                                              @RequestParam("client_secret") String clientSecret) {
        // 1. 手动校验 Client 合法性
        List<OAuth2Client> oAuth2Clients = AppConstants.getOauth2ClientList();
        if (CollectionUtils.isEmpty(oAuth2Clients)) {
            return R.fail("系统未配置授权客户端！");
        }

        OAuth2Client oAuth2Client = oAuth2Clients.stream()
                .filter(v -> StringUtils.isEquals(v.getClientId(), clientId)
                        && StringUtils.isEquals(v.getClientSecret(), clientSecret))
                .findFirst()
                .orElse(null);

        if (oAuth2Client == null) {
            return R.fail("未授权或认证未通过客户端：" + clientId);
        }

        // 2. IP 白名单校验（已修正原反向拦截逻辑）
        String ip = SpringMVCHolder.getIp();
        Collection<String> configWhiteList = oAuth2Client.getClientIps();
        if (!CollectionUtils.isEmpty(configWhiteList)) {
            boolean isAllowedIp = configWhiteList.stream()
                    .anyMatch(v -> "*".equals(v) || com.eryansky.j2cache.util.IpUtils.checkIPMatching(v, ip));
            if (!isAllowedIp) {
                return R.fail("未授权访问终端：" + clientId);
            }
        }

        try {
            // 4. 使用 HMAC256 算法签名生成 Token
            String token = JWTUtils.sign(clientId, AppConstants.getRestDefaultApiKey(),DEFAULT_EXPIRE_SECONDS * 1000);

            // 5. 返回标准 OAuth2 格式的响应 JSON
            Map<String, Object> map = new HashMap<>();
            map.put("access_token", token);
            map.put("token_type", "Bearer");
            map.put("expires_in", DEFAULT_EXPIRE_SECONDS);

            return R.ok(map);

        } catch (Exception e) {
            log.error("生成 OAuth2 Token 失败, clientId: {}, error: {}", clientId, e.getMessage(), e);
            return R.fail("Token 生成失败！");
        }
    }


    /**
     * 用户单点登录 Token 发放
     */
    @PostMapping("ssoToken")
    public R<Map<String, Object>> ssoToken(@RequestParam("access_token") String token,
                                              @RequestParam(value = "user_code") String userCode) {
        String clientId = JWTUtils.getUsername(token);
        boolean verify = JWTUtils.verify(token,clientId,AppConstants.getRestDefaultApiKey());
        if(!verify){
            return R.fail("访问凭证失效：" + token);
        }

        Map<String, Object> payload = Maps.newHashMap();
        User user = UserUtils.getUserByLoginNameOrMobile(userCode);
        if (user == null) {
            return R.fail("用户不存在：" + userCode);
        }
        payload.put("userId", user.getId());
        payload.put("username", user.getLoginName());//必选字段
        payload.put("mobile", user.getMobile());
        payload.put("iss", SpringContextHolder.getApplicationContext().getId());//必选字段
        payload.put("clientId", clientId);//必选字段
        payload.put("iat", System.currentTimeMillis());
        payload.put("exp", System.currentTimeMillis() + DEFAULT_EXPIRE_SSO_SECONDS * 1000L);//必选字段
        String ssoToken = JsonMapper.toJsonString(payload);

        String encryptSsoToken = null;
        try {
            List<OAuth2Client> oAuth2Clients = AppConstants.getOauth2ClientList();
            OAuth2Client oAuth2Client = oAuth2Clients.stream().filter(v -> v.getClientId().equals(clientId)).findFirst().orElse(null);
            if(oAuth2Client == null || StringUtils.isBlank(oAuth2Client.getClientSecret())){
                return R.fail("未配置授权终端："+clientId);
            }
            encryptSsoToken = Sm4Utils.encrypt(oAuth2Client.getClientSecret(), ssoToken);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return R.fail("服务期内部异常！");
        }

        Map<String, Object> map = Maps.newHashMap();
        payload.put("sso_token", encryptSsoToken);
        payload.put("expire", DEFAULT_EXPIRE_SSO_SECONDS);
        return R.ok(map);
    }
}