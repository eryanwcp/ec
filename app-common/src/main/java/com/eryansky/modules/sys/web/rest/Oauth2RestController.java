package com.eryansky.modules.sys.web.rest;

import com.eryansky.common.model.R;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.core.security.annotation.RestApi;
import com.eryansky.core.security.jwt.JWTUtils;
import com.eryansky.j2cache.lock.DefaultLockCallback;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.modules.sys.vo.OAuth2Client;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.CacheUtils;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 系统内置 OAuth2 认证 Controller（符合 RFC 7636 PKCE 标准与规范化响应）
 */
@RequiresUser(required = false)
@RestApi()
@RestController
@RequestMapping("/rest/oauth")
public class Oauth2RestController {

    private static final Logger log = LoggerFactory.getLogger(Oauth2RestController.class);

    private static final long DEFAULT_EXPIRE_SECONDS = 7200L;
    private static final long DEFAULT_EXPIRE_SSO_SECONDS = 600L;
    private static final String CACHE_PKCE_CODE_STORE = "cache_sso_pkce_code_store";

    /**
     * PKCE 授权码元数据 DTO（实现 Serializable，保障多级缓存/Redis 序列化兼容）
     */
    public static class CodeChallengeInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String clientId;
        private String codeChallenge;
        private String codeChallengeMethod;
        private String redirectUri;
        private long expireTime;

        // 无参构造函数保障 JSON 反序列化
        public CodeChallengeInfo() {}

        public CodeChallengeInfo(String clientId, String codeChallenge, String codeChallengeMethod, String redirectUri, long ttlSeconds) {
            this.clientId = clientId;
            this.codeChallenge = codeChallenge;
            this.codeChallengeMethod = StringUtils.defaultIfBlank(codeChallengeMethod, "S256");
            this.redirectUri = redirectUri;
            this.expireTime = System.currentTimeMillis() + (ttlSeconds * 1000L);
        }

        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public String getCodeChallenge() { return codeChallenge; }
        public void setCodeChallenge(String codeChallenge) { this.codeChallenge = codeChallenge; }
        public String getCodeChallengeMethod() { return codeChallengeMethod; }
        public void setCodeChallengeMethod(String codeChallengeMethod) { this.codeChallengeMethod = codeChallengeMethod; }
        public String getRedirectUri() { return redirectUri; }
        public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
        public long getExpireTime() { return expireTime; }
        public void setExpireTime(long expireTime) { this.expireTime = expireTime; }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * PKCE 流程第一步：获取 authorization_code
     */
    @GetMapping("authorize")
    public R<Map<String, Object>> authorize(
            @RequestParam("client_id") String clientId,
            @RequestParam("code_challenge") String codeChallenge,
            @RequestParam(value = "code_challenge_method", defaultValue = "S256") String codeChallengeMethod,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri) {

        OAuth2Client oAuth2Client = findClient(clientId);
        if (oAuth2Client == null) {
            return buildOAuthError("unauthorized_client", "未授权或不存在的客户端：" + clientId);
        }

        // IP 白名单安全检验
        String ip = SpringMVCHolder.getIp();
        R<Boolean> checkIpR = checkIP(oAuth2Client, ip);
        if (!checkIpR.isSuccess()) {
            return buildOAuthError("unauthorized_client", "未授权访问终端：" + clientId + "，IP:" + ip);
        }

        if (StringUtils.isBlank(codeChallenge)) {
            return buildOAuthError("invalid_request", "PKCE 模式下 code_challenge 不能为空！");
        }
        if (!"S256".equalsIgnoreCase(codeChallengeMethod) && !"plain".equalsIgnoreCase(codeChallengeMethod)) {
            return buildOAuthError("invalid_request", "不支持的 code_challenge_method，仅支持 S256 或 plain");
        }

        // 生成授权码（有效期 5 分钟）
        String code = Identities.uuid7();
        CodeChallengeInfo info = new CodeChallengeInfo(clientId, codeChallenge, codeChallengeMethod, redirectUri, 300);
        CacheUtils.put(CACHE_PKCE_CODE_STORE, code, info);

        Map<String, Object> result = new HashMap<>(2);
        result.put("code", code);
        return R.ok(result);
    }

    /**
     * 第二步：换取 Access Token
     */
    @PostMapping("accessToken")
    public R<Map<String, Object>> accessToken(
            @RequestParam("client_id") String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier,
            @RequestParam(value = "redirect_uri", required = false) String redirectUri) {

        OAuth2Client oAuth2Client = findClient(clientId);
        if (oAuth2Client == null) {
            return buildOAuthError("unauthorized_client", "未授权或不存在的客户端：" + clientId);
        }

        // 路由校验机制
        if (StringUtils.isNotBlank(code)) {
            // === 模式 A：PKCE 动态验证 ===
            // 严格执行一次性兑换（取出即删除，防止授权码重放攻击）
            CodeChallengeInfo challengeInfo = CacheUtils.getCacheChannel().lock(CACHE_PKCE_CODE_STORE + ":" + code, 5, 10, new DefaultLockCallback<CodeChallengeInfo>(null, null) {
                @Override
                public CodeChallengeInfo handleObtainLock() {
                    CodeChallengeInfo challengeInfo = CacheUtils.get(CACHE_PKCE_CODE_STORE, code);
                    if (challengeInfo != null) {
                        CacheUtils.remove(CACHE_PKCE_CODE_STORE, code);
                    }
                    return challengeInfo;
                }
            });

            if (challengeInfo == null || challengeInfo.isExpired() || !StringUtils.isEquals(challengeInfo.getClientId(), clientId)) {
                return buildOAuthError("invalid_grant", "无效或已过期的 authorization_code！");
            }

            // 安全性增强：比对 redirect_uri
            if (StringUtils.isNotBlank(challengeInfo.getRedirectUri()) && !StringUtils.isEquals(challengeInfo.getRedirectUri(), redirectUri)) {
                return buildOAuthError("invalid_grant", "redirect_uri 与授权时不匹配！");
            }

            if (StringUtils.isBlank(codeVerifier)) {
                return buildOAuthError("invalid_request", "PKCE 模式下必须提供 code_verifier！");
            }

            if (!verifyCodeVerifier(codeVerifier, challengeInfo.getCodeChallenge(), challengeInfo.getCodeChallengeMethod())) {
                return buildOAuthError("invalid_grant", "PKCE 校验失败：code_verifier 不匹配！");
            }
        } else {
            // === 模式 B：传统凭证模式 ===
            if (!StringUtils.isEquals(clientSecret, oAuth2Client.getClientSecret())) {
                return buildOAuthError("invalid_client", "客户端认证未通过：client_secret 错误");
            }
        }

        // IP 白名单安全检验
        String ip = SpringMVCHolder.getIp();
        R<Boolean> checkIpR = checkIP(oAuth2Client, ip);
        if (!checkIpR.isSuccess()) {
            return buildOAuthError("unauthorized_client", "未授权访问终端：" + clientId + "，IP:" + ip);
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
            return buildOAuthError("server_error", "Token 生成失败！");
        }
    }

    /**
     * 单点登录 Token 发放
     */
    @PostMapping("ssoToken")
    public R<Map<String, Object>> ssoToken(@RequestParam("access_token") String token, @RequestParam(value = "user_code") String userCode) {
        String clientId = JWTUtils.getUsername(token);
        OAuth2Client oAuth2Client = findClient(clientId);
        if (oAuth2Client == null) {
            return R.fail("未配置授权终端：" + clientId);
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

    /**
     * 基于 Guava 的高吞吐 PKCE 摘要匹配算法
     */
    private boolean verifyCodeVerifier(String codeVerifier, String codeChallenge, String method) {
        if ("plain".equalsIgnoreCase(method)) {
            return StringUtils.isEquals(codeVerifier, codeChallenge);
        }
        if ("S256".equalsIgnoreCase(method)) {
            try {
                // 使用 Guava Hashing，避免线程安全锁开销与 MessageDigest 实例频繁创建
                byte[] hash = Hashing.sha256().hashString(codeVerifier, StandardCharsets.US_ASCII).asBytes();
                String calculatedChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
                return StringUtils.isEquals(calculatedChallenge, codeChallenge);
            } catch (Exception e) {
                log.error("计算 PKCE SHA-256 Challenge 异常", e);
                return false;
            }
        }
        return false;
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

    /**
     * 遵循 RFC 6749 的 OAuth2 标准错误响应构建器
     */
    private R<Map<String, Object>> buildOAuthError(String error, String errorDescription) {
        Map<String, Object> errorMap = new HashMap<>(2);
        errorMap.put("error", error);
        errorMap.put("error_description", errorDescription);
        return R.fail(errorMap,errorDescription);
    }
}