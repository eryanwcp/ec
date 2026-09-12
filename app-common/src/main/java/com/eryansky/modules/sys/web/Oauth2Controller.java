package com.eryansky.modules.sys.web;

import com.eryansky.common.model.R;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.security.annotation.PrepareOauth2;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.core.security.jwt.JWTUtils;
import com.eryansky.j2cache.lock.DefaultLockCallback;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.modules.sys.vo.CodeChallengeInfo;
import com.eryansky.modules.sys.vo.OAuth2Client;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.CacheUtils;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 符合 OAuth 2.1 规范与 RFC 7636 PKCE 标准的认证 Controller
 */
@RequiresUser(required = false)
@PrepareOauth2(enable = false)
@RestController
@RequestMapping("${adminPath}/sys/oauth")
public class Oauth2Controller {

    private static final Logger log = LoggerFactory.getLogger(Oauth2Controller.class);
    private static final long DEFAULT_EXPIRE_SECONDS = 7200L;
    private static final long DEFAULT_EXPIRE_SSO_SECONDS = 600L;
    private static final String CACHE_PKCE_CODE_STORE = "cache_sso_pkce_code_store";

    /**
     * OAuth 2.1 Authorization Code 流程第一步：获取 authorization_code
     */
    @GetMapping("authorize")
    public ResponseEntity<Map<String, Object>> authorize(
            @RequestParam("response_type") String responseType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code_challenge") String codeChallenge,
            @RequestParam(value = "code_challenge_method", defaultValue = "S256") String codeChallengeMethod,
            @RequestParam(value = "state", required = false) String state) {

        // 1. 响应类型必须为 code
        if (!"code".equals(responseType)) {
            return buildOAuthError(HttpStatus.BAD_REQUEST, "unsupported_response_type", "response_type 必须为 code");
        }

        // 2. 检查客户端合法性
        OAuth2Client oAuth2Client = findClient(clientId);
        if (oAuth2Client == null) {
            return buildOAuthError(HttpStatus.UNAUTHORIZED, "unauthorized_client", "未授权或不存在的客户端：" + clientId);
        }

        // 4. OAuth 2.1：精确匹配校验 Redirect URI
        if (!validateRedirectUri(oAuth2Client, redirectUri)) {
            return buildOAuthError(HttpStatus.BAD_REQUEST, "invalid_request", "redirect_uri 校验失败或未匹配注册地址");
        }

        // 5. OAuth 2.1 强校验 PKCE 参数（必须提供 code_challenge，且推荐 S256）
        if (StringUtils.isBlank(codeChallenge)) {
            return buildOAuthError(HttpStatus.BAD_REQUEST, "invalid_request", "OAuth 2.1 规范强制要求提供 code_challenge！");
        }
        if (!"S256".equalsIgnoreCase(codeChallengeMethod)) {
            return buildOAuthError(HttpStatus.BAD_REQUEST, "invalid_request", "仅支持 S256 算法的 code_challenge_method");
        }

        // 6. 生成授权码（有效期 5 分钟）并存入缓存
        String code = Identities.uuid7();
        CodeChallengeInfo info = new CodeChallengeInfo(clientId, codeChallenge, codeChallengeMethod, redirectUri, 300);
        CacheUtils.put(CACHE_PKCE_CODE_STORE, code, info);

        Map<String, Object> result = new HashMap<>(4);
        result.put("code", code);
        if (StringUtils.isNotBlank(state)) {
            result.put("state", state);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * OAuth 2.1 Authorization Code 流程第二步：用 Code 换取 Access Token
     */
    @PostMapping("accessToken")
    public ResponseEntity<Map<String, Object>> accessToken(
            @RequestParam(value = "grant_type", defaultValue = "authorization_code") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("code") String code,
            @RequestParam("code_verifier") String codeVerifier,
            @RequestParam("redirect_uri") String redirectUri) {

        // 1. 校验 grant_type
        if (!"authorization_code".equals(grantType)) {
            return buildOAuthError(HttpStatus.BAD_REQUEST, "unsupported_grant_type", "不支持的 grant_type，仅支持 authorization_code");
        }

        // 2. 检查客户端合法性
        OAuth2Client oAuth2Client = findClient(clientId);
        if (oAuth2Client == null) {
            return buildOAuthError(HttpStatus.UNAUTHORIZED, "unauthorized_client", "未授权或不存在的客户端：" + clientId);
        }


        // 5. 安全原子提取授权码（一次性兑换，防止重放攻击）
        CodeChallengeInfo challengeInfo = CacheUtils.getCacheChannel().lock(
                CACHE_PKCE_CODE_STORE + ":" + code, 5, 10,
                new DefaultLockCallback<CodeChallengeInfo>(null, null) {
                    @Override
                    public CodeChallengeInfo handleObtainLock() {
                        CodeChallengeInfo info = CacheUtils.get(CACHE_PKCE_CODE_STORE, code);
                        if (info != null) {
                            CacheUtils.remove(CACHE_PKCE_CODE_STORE, code);
                        }
                        return info;
                    }
                });

        if (challengeInfo == null || challengeInfo.isExpired() || !StringUtils.isEquals(challengeInfo.getClientId(), clientId)) {
            return buildOAuthError(HttpStatus.BAD_REQUEST, "invalid_grant", "无效或已过期的 authorization_code！");
        }

        // 6. OAuth 2.1 严格比对 redirect_uri
        if (!StringUtils.isEquals(challengeInfo.getRedirectUri(), redirectUri)) {
            return buildOAuthError(HttpStatus.BAD_REQUEST, "invalid_grant", "redirect_uri 与授权阶段不一致！");
        }

        // 7. PKCE code_verifier 校验
        if (StringUtils.isBlank(codeVerifier)) {
            return buildOAuthError(HttpStatus.BAD_REQUEST, "invalid_request", "必须提供 code_verifier！");
        }
        if (!verifyCodeVerifier(codeVerifier, challengeInfo.getCodeChallenge(), challengeInfo.getCodeChallengeMethod())) {
            return buildOAuthError(HttpStatus.BAD_REQUEST, "invalid_grant", "PKCE 校验失败：code_verifier 验证未通过！");
        }

        // 8. 签发标准 JWT 令牌
        try {
            String token = JWTUtils.sign(clientId, oAuth2Client.getClientSecret(), DEFAULT_EXPIRE_SECONDS * 1000);
            Map<String, Object> tokenResponse = new HashMap<>(4);
            tokenResponse.put("access_token", token);
            tokenResponse.put("token_type", "Bearer");
            tokenResponse.put("expires_in", DEFAULT_EXPIRE_SECONDS);

            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            log.error("生成 OAuth2 Token 失败, clientId: {}, error: {}", clientId, e.getMessage(), e);
            return buildOAuthError(HttpStatus.INTERNAL_SERVER_ERROR, "server_error", "Token 生成失败！");
        }
    }

    /**
     * 单点登录 SSO Token 发放（扩展业务接口）
     */
    @PostMapping("ssoToken")
    public R<Map<String, Object>> ssoToken(@RequestParam("access_token") String token, @RequestParam("user_code") String userCode) {
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
     * PKCE SHA-256 (S256) 摘要匹配计算
     */
    private boolean verifyCodeVerifier(String codeVerifier, String codeChallenge, String method) {
        if ("S256".equalsIgnoreCase(method)) {
            try {
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

    /**
     * 校验 Redirect URI（精确全匹配）
     */
    private boolean validateRedirectUri(OAuth2Client client, String redirectUri) {
        if (StringUtils.isBlank(redirectUri) || client == null) {
            return false;
        }
        // 假定 client.getRedirectUris() 返回客户端注册的所有精确 URI 列表
        Collection<String> registeredUris = client.getRedirectUris();
        if (CollectionUtils.isEmpty(registeredUris)) {
            return false;
        }
        return registeredUris.contains(redirectUri);
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
     * 构建符合 RFC 6749 / OAuth 2.1 规范的标准错误 HTTP 响应
     */
    private ResponseEntity<Map<String, Object>> buildOAuthError(HttpStatus status, String error, String errorDescription) {
        Map<String, Object> errorMap = new LinkedHashMap<>(2);
        errorMap.put("error", error);
        errorMap.put("error_description", errorDescription);
        return ResponseEntity.status(status).body(errorMap);
    }
}