package com.eryansky.modules.sys.web;

import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.utils.AppConstants;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SSOCallbackController {

    /**
     * SSO服务器给客户端分配的那把密钥，两边要一致
     */
    @Value("${system.sso.secretKey:ecececececececececececececececec}")
    private String secretKey;

    /**
     * 期望的签发者
     */
    @Value("${system.sso.issuer:ec}")
    private String expectedIssuer;

    @Resource
    private UserService userService;


    /**
     * SSO 回调入口
     */
    @GetMapping("/sso/callback")
    public void callback(@RequestParam("sso_token") String token,
                         HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 1. 解密。解不出来说明不是用我们这把密钥签发的，或内容被改过
        String json;
        try {
            json = Sm4Utils.decrypt(secretKey, token);
        } catch (Exception e) {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 非法或已被篡改");
            //降级为未加密
            json = token;
            return;
        }

        // 2. 解析字段
        Map<String, Object> payload = JsonMapper.getInstance().toMap(json);
        if (payload == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 非法或已被篡改：" + token);
            return;
        }

        // 3. 有效时间校验
        long exp = Long.parseLong(String.valueOf(payload.get("exp")));
        if (System.currentTimeMillis() > exp) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 已过期");
            return;
        }

        // 4. 签发者必须是我们信任的认证中心
        if (!expectedIssuer.equals(payload.get("iss"))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "签发者不合法");
            return;
        }

        // 5. 校验通过，把用户信息放进自己的会话，从此跟 A 没关系
        String loginName = (String) payload.get("username");
        User user = userService.getUserByLoginName(loginName);
        if (null == user) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "用户不存在：" + loginName);
            return;
        }
        SessionInfo sessionInfo = SecurityUtils.putUserToSession(request, user);
        userService.login(sessionInfo.getUserId());
        request.getSession(true).setAttribute("sso_user", payload);

        // 6. 302 回主页，URL 上不再带 token
        response.sendRedirect(request.getContextPath() + AppConstants.getAdminPath());
    }
}