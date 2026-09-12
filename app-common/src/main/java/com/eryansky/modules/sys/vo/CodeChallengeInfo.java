package com.eryansky.modules.sys.vo;

import com.eryansky.common.utils.StringUtils;

import java.io.Serializable;

/**
 * PKCE 授权码元数据 DTO（实现 Serializable，保障多级缓存/Redis 序列化兼容）
 */
public class CodeChallengeInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String clientId;
    private String codeChallenge;
    private String codeChallengeMethod;
    private String redirectUri;
    private long expireTime;

    // 无参构造函数保障 JSON 反序列化
    public CodeChallengeInfo() {
    }

    public CodeChallengeInfo(String clientId, String codeChallenge, String codeChallengeMethod, String redirectUri, long ttlSeconds) {
        this.clientId = clientId;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = StringUtils.defaultIfBlank(codeChallengeMethod, "S256");
        this.redirectUri = redirectUri;
        this.expireTime = System.currentTimeMillis() + (ttlSeconds * 1000L);
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }
}