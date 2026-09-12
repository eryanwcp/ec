package com.eryansky.modules.sys.web.rest;

import com.eryansky.common.model.R;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.core.security.annotation.PrepareOauth2;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.core.security.annotation.RestApi;
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
@RequiresUser(required = false)
@PrepareOauth2(enable = false)
@RestApi()
@RestController
@RequestMapping("/rest/oauth")
public class Oauth2RestController {

    private static final Logger log = LoggerFactory.getLogger(Oauth2RestController.class);

    private static final long DEFAULT_EXPIRE_SECONDS = 7200L;
    private static final long DEFAULT_EXPIRE_SSO_SECONDS = 600L;

    /**
     * 获取 Access Token
     */
    @PostMapping("accessToken")
    public R<Map<String, Object>> accessToken(
            @RequestParam("client_id") String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret) {

        OAuth2Client oAuth2Client = findClient(clientId);
        if (oAuth2Client == null) {
            return R.fail("未授权或不存在的客户端：" + clientId);
        }

        // === 模式 B：传统凭证模式 ===
        if (!StringUtils.isEquals(clientSecret, oAuth2Client.getClientSecret())) {
            return R.fail("客户端认证未通过：client_secret 错误");
        }

        // IP 白名单安全检验
        String ip = SpringMVCHolder.getIp();
        R<Boolean> checkIpR = checkIP(oAuth2Client, ip);
        if (!checkIpR.isSuccess()) {
            return R.fail("未授权访问终端：" + clientId + "，IP:" + ip);
        }

        // 签发标准 JWT 令牌
        try {
            String token = JWTUtils.sign(clientId, oAuth2Client.getClientSecret(), DEFAULT_EXPIRE_SECONDS * 1000);

            Map<String, Object> map = new HashMap<>(4);
            map.put("access_token", token);
            map.put("token_type", "Bearer");
            map.put("expires_in", DEFAULT_EXPIRE_SECONDS);
            return R.ok(map);
        } catch (Exception e) {
            log.error("生成 OAuth2 Token 失败, clientId: {}, error: {}", clientId, e.getMessage(), e);
            R.fail("Token 生成失败！");
        }
    }

    /**
     * 单点登录 Token 发放
     */
    @PostMapping("ssoToken")
    public R<Map<String, Object>> ssoToken(@RequestParam("access_token") String token,
                                           @RequestParam(value = "user_code") String userCode) {
        String clientId = JWTUtils.getUsername(token);
        OAuth2Client oAuth2Client = findClient(clientId);
        if (oAuth2Client == null) {
            return R.fail("未配置授权终端：" + clientId);
        }

        // IP 白名单安全检验
        String ip = SpringMVCHolder.getIp();
        R<Boolean> checkIpR = checkIP(oAuth2Client, ip);
        if (!checkIpR.isSuccess()) {
            R.fail("未授权访问终端：" + clientId + "，IP:" + ip);
        }

        boolean verify = JWTUtils.verify(token, clientId, oAuth2Client.getClientSecret());
        if (!verify) {
            return R.fail("访问凭证失效：" + token);
        }

        User user = UserUtils.getUserByLoginNameOrMobile(userCode);
        if (user == null) {
            return R.fail("用户不存在：" + userCode);
        }

        Map<String, Object> payload = Maps.newHashMap();
        payload.put("userId", user.getId());
        payload.put("username", user.getLoginName());
        payload.put("mobile", user.getMobile());
        payload.put("iss", SpringContextHolder.getApplicationContext().getId());
        payload.put("clientId", clientId);
        payload.put("iat", System.currentTimeMillis());
        payload.put("exp", System.currentTimeMillis() + DEFAULT_EXPIRE_SSO_SECONDS * 1000L);

        try {
            String ssoToken = JsonMapper.toJsonString(payload);
            String encryptSsoToken = Sm4Utils.encrypt(oAuth2Client.getClientSecret(), ssoToken);

            Map<String, Object> map = Maps.newHashMap();
            map.put("sso_token", encryptSsoToken);
            map.put("expire", DEFAULT_EXPIRE_SSO_SECONDS);
            return R.ok(map);
        } catch (Exception e) {
            log.error("生成 SSO Token 失败，userCode: {}, error: {}", userCode, e.getMessage(), e);
            return R.fail("服务器内部异常！");
        }
    }


    private R<Boolean> checkIP(OAuth2Client oAuth2Client, String ip) {
        if (oAuth2Client == null) {
            return R.rest(false);
        }
        Collection<String> configWhiteList = oAuth2Client.getClientIps();
        if (!CollectionUtils.isEmpty(configWhiteList)) {
            boolean isAllowedIp = configWhiteList.stream()
                    .anyMatch(v -> "*".equals(v) || com.eryansky.j2cache.util.IpUtils.checkIPMatching(v, ip));
            if (!isAllowedIp) {
                return R.fail(false, "未授权访问终端：" + oAuth2Client.getClientId());
            }
        }
        return R.rest(true);
    }

    private OAuth2Client findClient(String clientId) {
        if (StringUtils.isBlank(clientId)) {
            return null;
        }
        List<OAuth2Client> oAuth2Clients = AppConstants.getOauth2ClientList();
        if (CollectionUtils.isEmpty(oAuth2Clients)) {
            return null;
        }
        return oAuth2Clients.stream()
                .filter(v -> StringUtils.isEquals(v.getClientId(), clientId))
                .findFirst()
                .orElse(null);
    }

}