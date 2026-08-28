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
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 系统内置oauth认证
 */
@RestController
@RequestMapping("${adminPath}/sys/oauth")
public class Oauth2Controller {

    private static final Logger log = LoggerFactory.getLogger(Oauth2Controller.class);


    /**
     * 手动实现 Token 发放接口
     */
    @PostMapping("token")
    public R<Map<String, Object>> accessToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam(value = "user_code", required = false) String userCode) {

        // 1. 手动校验 Client 合法性 TODO
        List<OAuth2Client> oAuth2Clients = AppConstants.getOauth2ClientList();
        OAuth2Client oAuth2Client = oAuth2Clients.stream().filter(v -> StringUtils.isEquals(v.getClientId(), clientId) && StringUtils.isEquals(v.getClientSecret(), clientSecret)).findFirst().orElse(null);
        if (oAuth2Client == null) {
            return new R<Map<String, Object>>().setCode(R.FAIL).setMsg("未授权或认证未通过客户端！" + clientId);
        }

        String ip = SpringMVCHolder.getIp();
        Collection<String> configWhiteList = oAuth2Client.getClientIps();
        if (null != configWhiteList && null != configWhiteList.stream().filter(v -> "*".equals(v) || com.eryansky.j2cache.util.IpUtils.checkIPMatching(v, ip)).findAny().orElse(null)) {
            return new R<Map<String, Object>>().setCode(R.FAIL).setMsg("未授权访问终端！" + clientId);
        }

        try {
            // 2. 构造 JWT Claims
            long now = System.currentTimeMillis();
            Algorithm algorithm = Algorithm.HMAC256(AppConstants.getRestDefaultApiKey());
            long expire = 7200L;
            JWTCreator.Builder builder = JWT.create()
                    .withIssuer(SpringContextHolder.getApplicationContext().getId())
                    .withSubject(userCode)
                    .withIssuedAt(new Date(now))
                    .withExpiresAt(new Date(now + expire * 1000))
                    .withClaim("client_id", clientId);


            // 3. 使用 RSA 私钥签名
            String token = builder.sign(algorithm);

            // 4. 返回标准 OAuth2 格式的响应 JSON
            Map<String, Object> map = new HashMap<>();
            map.put("access_token", token);
            map.put("token_type", "Bearer");
            map.put("expires_in", expire);

            return new R<Map<String, Object>>().setCode(R.SUCCESS).setData(map);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new R<Map<String, Object>>().setCode(R.FAIL);
        }
    }
}