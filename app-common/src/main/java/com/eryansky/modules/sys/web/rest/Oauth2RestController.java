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
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.modules.sys.vo.OAuth2Client;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.CacheUtils;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerJwtAutoConfiguration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 系统内置 OAuth2 认证 Controller（支持传统客户端凭证 & PKCE 扩展方案）
 */
@RequiresUser(required = false)
@RestApi()
@RestController
@RequestMapping("/rest/oauth")
public class Oauth2RestController {

    private static final Logger log = LoggerFactory.getLogger(Oauth2RestController.class);

    /**
     * 默认 Access Token 有效期 (单位：秒)
     */
    private static final long DEFAULT_EXPIRE_SECONDS = 7200L;
    /**
     * 默认 单点登录Token 有效期 (单位：秒)
     */
    private static final long DEFAULT_EXPIRE_SSO_SECONDS = 600L;

    /**
     * 存储 PKCE 授权码信息缓存key
     */
    private static final String CACHE_PKCE_CODE_STORE = "cache_sso_pkce_code_store";

    public static class CodeChallengeInfo {
        private final String clientId;
        private final String codeChallenge;
        private final String codeChallengeMethod;
        private final long expireTime;

        public CodeChallengeInfo(String clientId, String codeChallenge, String codeChallengeMethod, long ttlSeconds) {
            this.clientId = clientId;
            this.codeChallenge = codeChallenge;
            this.codeChallengeMethod = codeChallengeMethod;
            this.expireTime = System.currentTimeMillis() + (ttlSeconds * 1000L);
        }

        public String getClientId() {
            return clientId;
        }

        public String getCodeChallenge() {
            return codeChallenge;
        }

        public String getCodeChallengeMethod() {
            return codeChallengeMethod;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * PKCE 流程第一步：获取授权码 authorization_code
     */
    @GetMapping("authorize")
    public R<Map<String, Object>> authorize(
            @RequestParam("client_id") String clientId,
            @RequestParam("code_challenge") String codeChallenge,
            @RequestParam(value = "code_challenge_method", defaultValue = "S256") String codeChallengeMethod) {

        // 1. 校验 Client 是否注册
        OAuth2Client oAuth2Client = findClient(clientId);
        if (oAuth2Client == null) {
            return R.fail("未授权或不存在的客户端：" + clientId);
        }

        // 2. PKCE 参数合法性校验
        if (StringUtils.isBlank(codeChallenge)) {
            return R.fail("PKCE 模式下 code_challenge 不能为空！");
        }
        if (!"S256".equalsIgnoreCase(codeChallengeMethod) && !"plain".equalsIgnoreCase(codeChallengeMethod)) {
            return R.fail("不支持的 code_challenge_method，仅支持 S256 或 plain");
        }

        // 3. 生成授权码，绑定 code_challenge（缓存 5 分钟有效）
        String code = Identities.uuid7();
        CacheUtils.put(CACHE_PKCE_CODE_STORE, code, new CodeChallengeInfo(clientId, codeChallenge, codeChallengeMethod, 300));

        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        return R.ok(result);
    }

    /**
     * Access Token 认证授权（兼容传统 Client Credentials 与 PKCE Authorization Code 模式）
     */
    @PostMapping("accessToken")
    public R<Map<String, Object>> accessToken(
            @RequestParam("client_id") String clientId,
            @RequestParam(value = "client_secret", required = false) String clientSecret,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "code_verifier", required = false) String codeVerifier) {

        // 1. 获取并检查 Client 配置
        OAuth2Client oAuth2Client = findClient(clientId);

        if (oAuth2Client == null) {
            return R.fail("未授权或不存在的客户端：" + clientId);
        }

        // 2. 路由校验模式：PKCE 动态验证 或 传统静态 client_secret 校验
        if (StringUtils.isNotBlank(code)) {
            // === 模式 A：PKCE 授权码模式 ===
            // 严格执行一次性兑换（取出即删除，防止授权码重放攻击）
            CodeChallengeInfo challengeInfo = CacheUtils.get(CACHE_PKCE_CODE_STORE, code);
            CacheUtils.remove(CACHE_PKCE_CODE_STORE, code);
            if (challengeInfo == null || challengeInfo.isExpired() || !StringUtils.isEquals(challengeInfo.getClientId(), clientId)) {
                return R.fail("无效或已过期的 authorization_code！");
            }

            if (StringUtils.isBlank(codeVerifier)) {
                return R.fail("PKCE 校验必须提供 code_verifier！");
            }

            if (!verifyCodeVerifier(codeVerifier, challengeInfo.getCodeChallenge(), challengeInfo.getCodeChallengeMethod())) {
                return R.fail("PKCE 校验失败：code_verifier 不匹配！");
            }
        } else {
            // === 模式 B：传统 Client Credentials 模式（完全保留原系统校验规则） ===
            if (!StringUtils.isEquals(clientSecret, oAuth2Client.getClientSecret())) {
                return R.fail("未授权或认证未通过客户端：" + clientId);
            }
        }

        // 3. IP 白名单校验（完全保持原有防护逻辑）
        String ip = SpringMVCHolder.getIp();
        R<Boolean> checkIpR = checkIP(oAuth2Client, ip);
        if (!checkIpR.isSuccess()) {
            return R.fail("未授权访问终端：" + clientId + "，IP:" + ip);
        }

        // 4. 签发 Token 并返回 JSON
        try {
            String token = JWTUtils.sign(clientId, oAuth2Client.getClientSecret(), DEFAULT_EXPIRE_SECONDS * 1000);

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
     * 用户单点登录 Token 发放（完全保持原代码不动）
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
        Map<String, Object> payload = Maps.newHashMap();
        User user = UserUtils.getUserByLoginNameOrMobile(userCode);
        if (user == null) {
            return R.fail("用户不存在：" + userCode);
        }
        payload.put("userId", user.getId());
        payload.put("username", user.getLoginName()); // 必选字段
        payload.put("mobile", user.getMobile());
        payload.put("iss", SpringContextHolder.getApplicationContext().getId()); // 必选字段
        payload.put("clientId", clientId); // 必选字段
        payload.put("iat", System.currentTimeMillis());
        payload.put("exp", System.currentTimeMillis() + DEFAULT_EXPIRE_SSO_SECONDS * 1000L); // 必选字段
        String ssoToken = JsonMapper.toJsonString(payload);
        String encryptSsoToken = null;
        try {
            encryptSsoToken = Sm4Utils.encrypt(oAuth2Client.getClientSecret(), ssoToken);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.fail("服务器内部异常！");
        }
        Map<String, Object> map = Maps.newHashMap();
        payload.put("sso_token", encryptSsoToken);
        payload.put("expire", DEFAULT_EXPIRE_SSO_SECONDS);
        return R.ok(map);
    }

    /**
     * 检查IP是否授权
     *
     * @param oAuth2Client
     * @param ip
     * @return
     */
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

    /**
     * 内部辅助：检索 Client
     */
    private OAuth2Client findClient(String clientId) {
        if(StringUtils.isBlank(clientId)){
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
     * 内部辅助：依照 RFC 7636 规范检验 PKCE verifier
     */
    private boolean verifyCodeVerifier(String codeVerifier, String codeChallenge, String method) {
        if ("plain".equalsIgnoreCase(method)) {
            return StringUtils.isEquals(codeVerifier, codeChallenge);
        }
        if ("S256".equalsIgnoreCase(method)) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
                String calculatedChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
                return StringUtils.isEquals(calculatedChallenge, codeChallenge);
            } catch (Exception e) {
                log.error("计算 PKCE SHA-256 Challenge 异常", e);
                return false;
            }
        }
        return false;
    }
}