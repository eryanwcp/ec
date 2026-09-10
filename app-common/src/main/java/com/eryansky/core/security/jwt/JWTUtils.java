package com.eryansky.core.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class JWTUtils {

    private static final Logger log = LoggerFactory.getLogger(JWTUtils.class);

    /**
     * 默认过期时间：30分钟（毫秒）
     */
    private static final long EXPIRE_TIME = 1800 * 1000L;

    /**
     * 默认密钥（建议后续通过配置类或环境变量覆盖）
     */
    private static final String DEFAULT_SECRET = "ec_secret";

    public static final String SUBJECT = "username";

    /**
     * 生成签名，使用默认密钥和默认30min过期时间
     */
    public static String sign(String username) {
        return sign(username, DEFAULT_SECRET, EXPIRE_TIME);
    }

    /**
     * 生成签名，使用自定义密钥和默认30min过期时间
     */
    public static String sign(String username, String secret) {
        return sign(username, secret, EXPIRE_TIME);
    }

    /**
     * 生成签名
     */
    public static String sign(String username, String secret, long expireTime) {
        return sign(username, secret, expireTime, null);
    }

    /**
     * 生成签名（支持各种对象类型的 Claims）
     */
    public static String sign(String username, String secret, long expireTime, Map<String, ?> claims) {
        if (username == null || secret == null) {
            throw new IllegalArgumentException("Username and secret must not be null");
        }

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expireDate = new Date(nowMillis + expireTime);

        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTCreator.Builder builder = JWT.create()
                .withClaim(SUBJECT, username)
                .withIssuedAt(now)
                .withExpiresAt(expireDate);

        if (claims != null && !claims.isEmpty()) {
            claims.forEach((key, value) -> {
                if (value instanceof String) {
                    builder.withClaim(key, (String) value);
                } else if (value instanceof Integer) {
                    builder.withClaim(key, (Integer) value);
                } else if (value instanceof Long) {
                    builder.withClaim(key, (Long) value);
                } else if (value instanceof Boolean) {
                    builder.withClaim(key, (Boolean) value);
                } else if (value instanceof Date) {
                    builder.withClaim(key, (Date) value);
                } else {
                    builder.withClaim(key, value != null ? value.toString() : null);
                }
            });
        }

        return builder.sign(algorithm);
    }

    /**
     * 校验 token 是否合法
     */
    public static boolean verify(String token, String username, String secret) {
        if (token == null || token.trim().isEmpty() || secret == null) {
            return false;
        }

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim(SUBJECT, username)
                    .build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            if (!(e instanceof TokenExpiredException)) {
                log.warn("Token verification failed for username: {}, reason: {}", username, e.getMessage());
            }
            return false;
        }
    }

    /**
     * 从 token 中获取用户名（无需密钥解密）
     *
     * @return 用户名，若解析失败或不存在则返回 null
     */
    public static String getUsername(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            DecodedJWT jwt = JWT.decode(token);
            Claim claim = jwt.getClaims().get(SUBJECT);
            return claim != null ? claim.asString() : null;
        } catch (JWTDecodeException e) {
            log.error("Failed to decode token: {}", e.getMessage());
            return null;
        }
    }
}