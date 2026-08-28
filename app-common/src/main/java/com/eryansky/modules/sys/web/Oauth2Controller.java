package com.eryansky.modules.sys.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.eryansky.common.model.R;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.modules.sys.vo.OAuth2Client;
import com.eryansky.utils.AppConstants;
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

    /** 默认 Token 有效期 (单位：秒) */
    private static final long DEFAULT_EXPIRE_SECONDS = 7200L;

    /**
     * 手动实现 Token 发放接口
     */
    @PostMapping("token")
    public R<Map<String, Object>> accessToken(@RequestParam("grant_type") String grantType,
                                              @RequestParam("client_id") String clientId,
                                              @RequestParam("client_secret") String clientSecret,
                                              @RequestParam(value = "user_code", required = false) String userCode) {

        // 0. 校验 grant_type
        if (StringUtils.isBlank(grantType)) {
            return new R<Map<String, Object>>().setCode(R.FAIL).setMsg("请求参数 grant_type 不能为空！");
        }

        // 1. 手动校验 Client 合法性
        List<OAuth2Client> oAuth2Clients = AppConstants.getOauth2ClientList();
        if (CollectionUtils.isEmpty(oAuth2Clients)) {
            return new R<Map<String, Object>>().setCode(R.FAIL).setMsg("系统未配置授权客户端！");
        }

        OAuth2Client oAuth2Client = oAuth2Clients.stream()
                .filter(v -> StringUtils.isEquals(v.getClientId(), clientId)
                        && StringUtils.isEquals(v.getClientSecret(), clientSecret))
                .findFirst()
                .orElse(null);

        if (oAuth2Client == null) {
            return new R<Map<String, Object>>().setCode(R.FAIL).setMsg("未授权或认证未通过客户端：" + clientId);
        }

        // 2. IP 白名单校验（已修正原反向拦截逻辑）
        String ip = SpringMVCHolder.getIp();
        Collection<String> configWhiteList = oAuth2Client.getClientIps();
        if (!CollectionUtils.isEmpty(configWhiteList)) {
            boolean isAllowedIp = configWhiteList.stream()
                    .anyMatch(v -> "*".equals(v) || com.eryansky.j2cache.util.IpUtils.checkIPMatching(v, ip));
            if (!isAllowedIp) {
                return new R<Map<String, Object>>().setCode(R.FAIL).setMsg("未授权访问终端：" + clientId);
            }
        }

        try {
            // 3. 构造 JWT Claims
            long now = System.currentTimeMillis();
            Algorithm algorithm = Algorithm.HMAC256(AppConstants.getRestDefaultApiKey());
            long expire = DEFAULT_EXPIRE_SECONDS;

            JWTCreator.Builder builder = JWT.create()
                    .withIssuer(SpringContextHolder.getApplicationContext().getId())
                    .withSubject(userCode)
                    .withIssuedAt(new Date(now))
                    .withExpiresAt(new Date(now + expire * 1000))
                    .withClaim("client_id", clientId);

            // 4. 使用 HMAC256 算法签名生成 Token
            String token = builder.sign(algorithm);

            // 5. 返回标准 OAuth2 格式的响应 JSON
            Map<String, Object> map = new HashMap<>();
            map.put("access_token", token);
            map.put("token_type", "Bearer");
            map.put("expires_in", expire);

            return new R<Map<String, Object>>().setCode(R.SUCCESS).setData(map);

        } catch (Exception e) {
            log.error("生成 OAuth2 Token 失败, clientId: {}, userCode: {}, error: {}", clientId, userCode, e.getMessage(), e);
            return new R<Map<String, Object>>().setCode(R.FAIL).setMsg("Token 生成失败！");
        }
    }
}