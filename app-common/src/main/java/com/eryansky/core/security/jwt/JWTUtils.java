package com.eryansky.core.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

public class JWTUtils {

    private static final Logger log = LoggerFactory.getLogger(JWTUtils.class);

    /**
     * 过期时间 默认30分钟
     */
    private static final long EXPIRE_TIME = 1800 * 1000;
    /**
     * 密钥
     */
    private static final String DEFAULT_SECRET = "ec_secret";

    /**
     * 生成签名,30min后过期
     *
     * @param username 用户名
     * @return 加密的token
     */
    public static String sign(String username) {
        return sign(username,DEFAULT_SECRET,EXPIRE_TIME);
    }

    /**
     * 生成签名,30min后过期
     *
     * @param username 用户名
     * @param secret   用户的密码
     * @return 加密的token
     */
    public static String sign(String username, String secret) {
        return sign(username,secret,EXPIRE_TIME);
    }

    /**
     * 生成签名
     *
     * @param username 用户名
     * @param secret   用户的密码
     * @param expireTime   超时时间 毫秒
     * @return 加密的token
     */
    public static String sign(String username, String secret,long expireTime) {
        return sign(username, secret, expireTime,null);
    }

    /**
     * 生成签名
     *
     * @param username 用户名
     * @param secret   用户的密码
     * @param expireTime   超时时间 毫秒
     * @param claims 自定义参数
     * @return 加密的token
     */
    public static String sign(String username, String secret, long expireTime, Map<String,String> claims) {
        Date now = new Date(System.currentTimeMillis());
        Date date = new Date(now.getTime() + expireTime);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTCreator.Builder builder = JWT.create()
                .withSubject(username)
                .withIssuedAt(now)
                .withExpiresAt(date);
        if (claims != null) {
            claims.forEach(builder::withClaim);
        }
        return builder.sign(algorithm);
    }

    /**
     * 校验token是否正确
     *
     * @param token  密钥
     * @param secret 用户的密码
     * @return 是否正确
     */
    public static boolean verify(String token, String username, String secret) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withSubject(username)
                    .build();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            log.warn("Token verification failed for username: {}", username, e);
            return false;
        }
    }

    /**
     * 获得token中的信息无需secret解密也能获得
     *
     * @return token中包含的用户名
     */
    public static String getUsername(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getSubject();
    }

}