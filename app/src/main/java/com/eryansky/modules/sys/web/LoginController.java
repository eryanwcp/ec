/**
 * Copyright (c) 2012-2026 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.eryansky.common.exception.SystemException;
import com.eryansky.common.model.*;
import com.eryansky.common.orm._enum.StatusState;
import com.eryansky.common.spring.SpringContextHolder;
import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.UserAgentUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.servlet.ValidateCodeServlet;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.security.SecurityType;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.PrepareOauth2;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.encrypt.config.EncryptProvider;
import com.eryansky.encrypt.util.RequestEncryptUtils;
import com.eryansky.j2cache.lock.DefaultLockCallback;
import com.eryansky.modules.sys.mapper.Resource;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.ResourceService;
import com.eryansky.modules.sys.service.UserPasswordService;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.modules.sys.task.SystemSecurityTask;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.modules.sys.vo.PasswordTip;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import com.eryansky.utils.CacheUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 用户登录/注销等前端交互入口
 *
 * @author Eryan
 * @date : 2014-05-02 19:50
 */
@Controller
@RequestMapping(value = {"${adminPath}/login", "${mobilePath}/login"})
public class LoginController extends SimpleController {

    @javax.annotation.Resource
    private UserService userService;
    @javax.annotation.Resource
    private UserPasswordService userPasswordService;
    @javax.annotation.Resource
    private ResourceService resourceService;
    @javax.annotation.Resource
    private SystemSecurityTask systemSecurityTask;

    private static final int RESULT_CODE_APP_VERSION_ERROR = 5; // APP版本禁止登录
    private static final int RESULT_CODE_DEVICE_ERROR = 4;      // 移动设备校验错误码
    private static final int RESULT_CODE_PASSWORD_ERROR = 6;    // 密码过期等

    /**
     * 欢迎页面
     *
     * @return
     */
    @PrepareOauth2(enable = false)
    @RequiresUser(required = false)
    @GetMapping(value = {"welcome", ""})
    public ModelAndView welcome(HttpServletRequest request,
                                @RequestParam(value = "client_id", required = false) String clientId,
                                @RequestParam(value = "redirect_uri", required = false) String redirectUri) {
        ModelAndView modelAndView = new ModelAndView("login.html");
        String ip = SpringMVCHolder.getIp();

        // 校验客户端 IP 是否满足强制开启验证码的条件
        boolean isValidateCodeLogin = isValidateCodeLogin(ip, false, false);

        modelAndView.addObject("isValidateCodeLogin", isValidateCodeLogin);
        modelAndView.addObject("isMobile", UserAgentUtils.isMobile(request));

        String randomSecurityToken = Identities.randomBase62(64);
        WebUtils.setSessionAttribute(request, "securityToken", randomSecurityToken);
        modelAndView.addObject("securityToken", randomSecurityToken);
        modelAndView.addObject("redirectUri", redirectUri);
        modelAndView.addObject("clientId", clientId);
        return modelAndView;
    }

    /**
     * 是否开启验证码登录（针对用户名、IP等）
     *
     * @param key 用户名或IP等客户端标识
     * @param isFail   计数加1
     * @param clean    计数清零
     * @return
     */
    @SuppressWarnings("unchecked")
    private static boolean isValidateCodeLogin(String key, boolean isFail, boolean clean) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return CacheUtils.getCacheChannel().lock("loginFailMap:" + key, 5, 10, new DefaultLockCallback<Boolean>(false, false) {
            @Override
            public Boolean handleObtainLock() {
                if (clean) {
                    CacheUtils.remove("loginFailMap", key);
                    return false;
                }
                Integer loginFailNum = CacheUtils.get("loginFailMap", key);
                int failCount = (loginFailNum == null) ? 0 : loginFailNum;

                if (isFail) {
                    failCount++;
                    CacheUtils.put("loginFailMap", key, failCount);
                }
                return failCount >= AppConstants.getLoginAgainSize();
            }
        });
    }

    /**
     * 判断客户端标识是否已触发高阶锁定阈值（防止单 IP 爆破不同账号）
     *
     * @param key 客户端 IP或账号
     * @return 是否封禁该客户端
     */
    @SuppressWarnings("unchecked")
    private static boolean isBlocked(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        Integer failNum = CacheUtils.get("loginFailMap",key);
        // 阀值设为触发验证码限制的 3 倍（最少 15 次）
        int maxBlockNum = Math.max(15, AppConstants.getLoginAgainSize() * 3);
        return failNum != null && failNum >= maxBlockNum;
    }

    /**
     * 登录用户数校验
     */
    private void checkLoginLimit() {
        String loginName = SpringMVCHolder.getRequest().getParameter("loginName");
        if (StringUtils.isBlank(loginName)) {
            return;
        }

        boolean isWhitelisted = AppConstants.getLimitUserWhiteList().stream()
                .anyMatch(v -> StringUtils.simpleWildcardMatch(v, loginName.toUpperCase()));

        if (isWhitelisted) {
            return;
        }
        int maxSize = AppConstants.getSessionUserMaxSize();
        if (maxSize < 0) { // 系统维护
            throw new SystemException("系统正在维护，请稍后再试！");
        } else if (maxSize != 0) { // 0 为不限制
            int sessionUser = SecurityUtils.getSessionInfoSize();
            if (sessionUser >= maxSize) {
                throw new SystemException("系统当前登录用户数量过多，请稍后再试！");
            }
        }
        if(AppConstants.isUserDeviceRiskEnable()){
            HttpServletRequest request = SpringMVCHolder.getRequest();
            String deviceCode = WebUtils.getParameter(request,"deviceCode");
            String ip = IpUtils.getIpAddr0(request);
            String userAgent = UserAgentUtils.getHTTPUserAgent(request);
            systemSecurityTask.checkRiskUserDevice(loginName,deviceCode,ip,userAgent);
        }
    }

    /**
     * 预登录信息获取 动态登录码等信息
     *
     * @return
     */
    @RequiresUser(required = false)
    @RequestMapping(value = {"prepareLogin"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Result prepareLogin(HttpServletRequest request) {
        String randomSecurityToken = Identities.randomBase62(64);
        String publicKey = EncryptProvider.publicKeyBase64();
        Map<String, Object> data = Maps.newHashMap();
        data.put("securityToken", randomSecurityToken);
        data.put("publicKey", publicKey);
        return Result.successResult().setObj(data);
    }

    /**
     * 后端登录生成公钥方法
     *
     * @return
     */
    @GetMapping(value = "getPublicKey")
    @ResponseBody
    public Result RSAKey() {
        String publicKey = EncryptProvider.publicKeyBase64();
        return Result.successResult().setObj(publicKey);
    }




    /**
     * 登录验证
     *
     * @param loginName    用户名
     * @param password     密码
     * @param validateCode 验证码
     * @param request
     * @return
     */
    @PrepareOauth2(enable = false)
    @RequiresUser(required = false)
    @ResponseBody
    @PostMapping(value = {"login"})
    public Result login(@RequestParam(name = "client_id", required = false) String clientId,
                        @RequestParam(name = "redirect_uri", required = false) String redirectUri,
                        @RequestParam(name = "loginName", required = true) String loginName,
                        @RequestParam(name = "password", required = true) String password,
                        @RequestParam(name = "_csrf_token", required = true) String csrfToken,
                        @RequestParam(name = "validateCode", required = false) String validateCode,
                        HttpServletRequest request, HttpServletResponse response) {
        // 1. 登录限制校验
        checkLoginLimit();
        loginName = StringUtils.trim(loginName);
        String ip = SpringMVCHolder.getIp();

        // 2. IP 爆破防御校验
        if (isBlocked(ip)) {
            logger.warn("IP登录尝试过多，拒绝请求：IP={} username={} headers：{}", ip, loginName,JsonMapper.toJsonString(WebUtils.getHeaders(request)));
            return Result.errorResult().setMsg("当前 IP 尝试登录错误次数过多，已被暂时锁定，请稍后再试！");
        }

        // 3. CSRF校验
        String securityToken = (String) WebUtils.getSessionAttribute(request, "securityToken");
        if (!StringUtils.equals(csrfToken, securityToken)) {
            return Result.errorResult().setMsg("非法请求！").setObj(false);
        }

        Result result = null;
        String msg = null;
        int loginAgainSize = AppConstants.getLoginAgainSize();
        final String VALIDATECODE_TIP = "密码输入错误超过" + (loginAgainSize == 0 ? 1 : loginAgainSize) + "次，请输入验证码!";

        // 4. 双重校验：账号或 IP 任一触发均需验证码
        boolean isValidateCodeLogin = isValidateCodeLogin(loginName, false, false)
                || isValidateCodeLogin(ip, false, false);

        if (isValidateCodeLogin) {
            if (StringUtils.isBlank(validateCode)) {
                return Result.errorResult().setMsg(VALIDATECODE_TIP).setObj(isValidateCodeLogin);
            }
            if (!ValidateCodeServlet.validate(request, validateCode)) {
                msg = "验证码不正确或验证码已过期!";
                // 验证码输入错误：累加账号与 IP 计数
                isValidateCodeLogin(loginName, true, false);
                isValidateCodeLogin(ip, true, false);
                return Result.errorResult().setMsg(msg).setObj(isValidateCodeLogin);
            }
        }

        // 5. 解密密码与账号校验
        String originPassword = null;
        try {
            originPassword = RequestEncryptUtils.decryptEncodeDataByRequest(request, password);
        } catch (Exception e) {
            return Result.errorResult();
        }
        String _password = Encrypt.e(originPassword);

        // 获取用户信息
        User user = userService.getUserByLMP(loginName, loginName, _password);
        boolean flag = null != user;
        if (null == user && AppConstants.isdevMode()) {
            user = userService.getUserByLoginName(loginName);
            flag = null != user;
        }

        if (!flag) {
            msg = "用户名或密码不正确!";
        } else if (user.getStatus().equals(StatusState.LOCK.getValue())) {
            msg = "该用户已被锁定，暂不允许登陆!";
        }

        // 6. 登录失败分支
        if (msg != null) {
            // 累加对应用户名和 IP 的失败次数
            isValidateCodeLogin(loginName, true, false);
            isValidateCodeLogin(ip, true, false);

            isValidateCodeLogin = isValidateCodeLogin(loginName, false, false)
                    || isValidateCodeLogin(ip, false, false);

            if (isValidateCodeLogin && UserAgentUtils.isComputer(request)) {
                msg += VALIDATECODE_TIP;
            }
            result = new Result(Result.ERROR, msg, isValidateCodeLogin);
        } else {
            // 7. 登录成功分支
            boolean isSecurityOn = AppConstants.getIsSecurityOn();
            if (isSecurityOn) {
                List<SessionInfo> userSessionInfos = SecurityUtils.findSessionInfoByLoginName(loginName);
                int sessionUserLimitSize = AppConstants.getUserSessionSize();
                if (sessionUserLimitSize > 0 && userSessionInfos.size() >= sessionUserLimitSize) {
                    result = new Result(Result.ERROR, "已达到用户最大会话登录限制[" + sessionUserLimitSize + "，请注销其它登录信息后再试！]", sessionUserLimitSize);
                    return result;
                }
            }
            boolean isMobile = UserAgentUtils.isMobile(request);
            if (isSecurityOn && AppConstants.isCheckLoginPassword()) {
                PasswordTip passwordTip = userPasswordService.checkPassword(user.getId());
                if (passwordTip.isTip()) {
                    String token = SecurityUtils.createUserToken(user);
                    passwordTip.setUrl(AppUtils.createLocalSecurityUpdatePasswordUrl(request, token));
                    Map<String, Object> data = Maps.newHashMap();
                    data.put("url", passwordTip.getUrl());
                    data.put("passwordTip", passwordTip);
                    data.put("userId", user.getId());
                    data.put("loginName", user.getLoginName());
                    data.put("token", token);
                    return new Result().setCode(RESULT_CODE_PASSWORD_ERROR).setObj(data).setMsg(passwordTip.getMsg());
                }
            }

            // 将用户信息放入 session
            SessionInfo sessionInfo = SecurityUtils.putUserToSession(request, user);
            userService.login(sessionInfo.getUserId());
            WebUtils.setSessionAttribute(request, "securityToken", null);
            logger.info("用户登录系统：{} {}", user.getLoginName(), SpringMVCHolder.getIp());

            String resultUrl = request.getContextPath() + (isMobile ? AppConstants.getMobilePath() : AppConstants.getAdminPath());
            if (StringUtils.isNotBlank(redirectUri) && StringUtils.isNotBlank(clientId)) {
                Map<String, Object> payload = Maps.newHashMap();
                payload.put("userId", user.getId());
                payload.put("username", user.getLoginName());
                payload.put("mobile", user.getMobile());
                payload.put("iss", SpringContextHolder.getApplicationContext().getId());
                payload.put("clientId", clientId);
                payload.put("iat", System.currentTimeMillis());
                payload.put("exp", System.currentTimeMillis() + 10 * 60 * 1000L);
                String ssoToken = JsonMapper.toJsonString(payload);
                try {
                    String encryptSsoToken = Sm4Utils.encrypt(Hex.toHexString(EncryptProvider.sm4Key().getBytes(StandardCharsets.UTF_8)), ssoToken);
                    resultUrl = AppUtils.appendParaToUrl(redirectUri, "sso_token", encryptSsoToken);
                    request.getSession().setAttribute("sso_user_" + clientId, ssoToken);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            // 返回成功并清理对应账户和 IP 的登录失败计数
            Map<String, Object> data = Maps.newHashMap();
            data.put("homeUrl", resultUrl);
            result = new Result(Result.SUCCESS, "用户验证通过!", data);

            isValidateCodeLogin(loginName, false, true);
            isValidateCodeLogin(ip, false, true);
        }
        return result;
    }

    /**
     * 发送登录验证码
     *
     * @return
     */
    @PrepareOauth2(enable = false)
    @RequiresUser(required = false)
    @PostMapping(value = "sendLoginSms")
    @ResponseBody
    public Result sendLoginSms(@RequestParam(value = "loginName",required = true) String loginNameOrMobile) {
        return Result.successResult().setMsg("模拟实现！");
    }

    /**
     * 登录（短信验证码）
     * @param loginNameOrMobile 账号或手机号
     * @param code 验证码
     * @param request
     * @param uiModel
     * @return
     */
    @PrepareOauth2(enable = false)
    @RequiresUser(required = false)
    @ResponseBody
    @PostMapping(value = {"smsLogin"})
    public Result smsLogin(@RequestParam(value = "client_id",required = false) String clientId,
                           @RequestParam(value = "redirect_uri",required = false) String redirectUri,
                           @RequestParam(value = "loginName",required = true) String loginNameOrMobile,
                           @RequestParam(name = "code",required = true) String code,
                           HttpServletRequest request, Model uiModel) {
        return Result.errorResult().setMsg("暂未实现！");
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @RequiresUser(required = false)
    @PrepareOauth2(enable = false)
    @PostMapping(value = {"logout"})
    @ResponseBody
    public Result postlogout(HttpServletRequest request) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo != null) {
            SecurityUtils.removeSession(sessionInfo.getSessionId(), SecurityType.logout);
            logger.info("退出系统：{} {}", sessionInfo.getSessionId(), sessionInfo.getLoginName());
        }
        return Result.successResult();
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PrepareOauth2(enable = false)
    @GetMapping(value = {"logout"})
    public String logout(HttpServletRequest request) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo != null) {
            SecurityUtils.removeSession(sessionInfo.getSessionId(), SecurityType.logout);
            logger.info("退出系统：{} {}", sessionInfo.getSessionId(), sessionInfo.getLoginName());
        }
        return "redirect:/";
    }


    /**
     * 自动登录
     *
     * @param loginName
     * @param refreshToken
     * @param request
     * @param uiModel
     * @return
     */
    @PrepareOauth2(enable = false)
    @RequiresUser(required = false)
    @ResponseBody
    @PostMapping(value = {"autoLogin"})
    public Result autoLogin(@RequestParam(required = true) String loginName,
                            @RequestParam(required = true) String refreshToken,
                            HttpServletRequest request, Model uiModel) {
        checkLoginLimit();
        Result result = null;
        boolean verify = false;
        User user = UserUtils.getUserByLoginName(loginName);
        if (null == user) {
            logger.warn("不存在账号：{}", loginName);
            return Result.errorResult().setMsg("系统不存在账号[" + loginName + "]！");
        }
        try {
            verify = SecurityUtils.verifySessionInfoToken(refreshToken, loginName, user.getPassword());
        } catch (Exception e) {
            if (!(e instanceof TokenExpiredException)) {
                logger.error("Token校验失败：{}，{},{},{}", loginName, SpringMVCHolder.getIp(), refreshToken, e.getMessage());
            }
        }
        if (!verify) {
            return Result.errorResult().setMsg("Token校验失败！");
        }
        SessionInfo sessionInfo = SecurityUtils.putUserToSession(request, user);
        userService.login(sessionInfo.getUserId());
        SecurityUtils.refreshSessionInfo(sessionInfo);
        logger.info("用户自动登录：{} {}", user.getLoginName(), SpringMVCHolder.getIp());

        String resultUrl = request.getContextPath() + AppConstants.getAdminPath();
        Map<String, Object> data = Maps.newHashMap();
        data.put("sessionInfo", sessionInfo);
        data.put("homeUrl", resultUrl);
        result = new Result(Result.SUCCESS, "用户自动登录成功!", data);
        return result;
    }

    /**
     * 切换账号 无需密码
     *
     * @param request
     * @param loginName
     * @return
     */
    @PostMapping(value = {"toggleLogin"})
    @ResponseBody
    public Result toggleLogin(HttpServletRequest request, @RequestParam(value = "loginName") String loginName) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo == null) {
            return Result.errorResult().setMsg("未登录！");
        }
        if (!sessionInfo.getLoginNames().contains(loginName)) {
            return Result.errorResult().setMsg("未授权账号【" + loginName + "】！");
        }
        User user = UserUtils.getUserByLoginName(loginName);
        if (null == user) {
            return Result.errorResult().setMsg("账号不存在【" + loginName + "】！");
        }
        SecurityUtils.removeSessionInfoFromSession(sessionInfo.getSessionId(), SecurityType.logout);
        SessionInfo newSessionInfo = SecurityUtils.putUserToSession(request, user);
        userService.login(newSessionInfo.getUserId());
        logger.info("切换账号：{} -> {} {}", sessionInfo.getLoginName(), newSessionInfo.getLoginName(), SpringMVCHolder.getIp());
        Map<String, Object> data = Maps.newHashMap();
        data.put("sessionInfo", newSessionInfo);
        return Result.successResult().setObj(data);
    }

    @GetMapping(value = {"index"})
    public String index(String theme) {
        if (StringUtils.isNotBlank(theme) && (theme.equals("app") || theme.equals("index"))) {
            return "layout/" + theme;
        } else {
            return "layout/index";
        }
    }

    /**
     * 导航菜单.
     */
    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"navTree"})
    public List<TreeNode> navTree(HttpServletResponse response) {
        WebUtils.setNoCacheHeader(response);
        List<TreeNode> treeNodes = Lists.newArrayList();
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo != null) {
            treeNodes = resourceService.findNavTreeNodeWithPermissions(sessionInfo.getUserId());
        }
        return treeNodes;
    }

    /**
     * 导航菜单.
     */
    @ResponseBody
    @PostMapping(value = {"navTree2"})
    public List<SiderbarMenu> navTree2(HttpServletResponse response) {
        WebUtils.setNoCacheHeader(response);
        List<SiderbarMenu> treeNodes = Lists.newArrayList();
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo != null) {
            treeNodes = resourcesToSiderbarMenu(resourceService.findAppAndMenuWithPermissions(sessionInfo.getUserId()));
        }
        return treeNodes;
    }

    private List<SiderbarMenu> resourcesToSiderbarMenu(Collection<Resource> resources) {
        if (Collections3.isEmpty(resources)) {
            return Collections.emptyList();
        }

        Map<String, SiderbarMenu> menuMap = new LinkedHashMap<>();
        for (Resource r : resources) {
            SiderbarMenu menu = resourceToSiderbarMenu(r);
            menuMap.put(menu.getId(), menu);
        }

        List<SiderbarMenu> rootMenus = new ArrayList<>();
        for (SiderbarMenu menu : menuMap.values()) {
            String pId = menu.getpId();
            SiderbarMenu parent = StringUtils.isNotBlank(pId) ? menuMap.get(pId) : null;

            if (parent != null) {
                parent.addChild(menu);
            } else {
                rootMenus.add(menu);
            }
        }
        return rootMenus;
    }

    private SiderbarMenu resourceToSiderbarMenu(Resource resource) {
        Assert.notNull(resource, "参数resource不能为空");
        SiderbarMenu menu = new SiderbarMenu(resource.getId(), resource.getName());
        menu.setpId(resource.getParentId());
        menu.setHeader(StringUtils.isBlank(menu.getpId()));
        menu.setTargetType("iframe-tab");
        menu.setIcon(resource.getIconCls());
        String url = resource.getUrl();
        menu.setUrl(url);
        menu.addAttribute("type", resource.getType());
        return menu;
    }


    /**
     * 桌面版 开始菜单
     */
    @PostMapping(value = {"startMenu"})
    @ResponseBody
    public List<Menu> startMenu() {
        List<Menu> menus = null;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        menus = resourceService.findNavMenuWithPermissions(sessionInfo.getUserId());
        return menus;
    }

    /**
     * 桌面版 桌面应用程序列表
     */
    @PostMapping(value = {"apps"})
    @ResponseBody
    public List<Menu> apps() {
        List<Menu> menus = Lists.newArrayList();
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo != null) {
            List<Resource> resources = resourceService.findMenuResourcesWithPermissions(sessionInfo.getUserId());
            for (Resource resource : resources) {
                if (resource != null && StringUtils.isNotBlank(resource.getUrl())) {
                    Menu menu = resourceToMenu(resource);
                    menus.add(menu);
                }
            }
        }
        return menus;
    }

    private Menu resourceToMenu(Resource resource) {
        HttpServletRequest request = SpringMVCHolder.getRequest();
        Assert.notNull(resource, "参数resource不能为空");
        String head = this.getHeadFromUrl(request.getRequestURL().toString());
        Menu menu = new Menu(resource.getId(), resource.getName());
        String url = resource.getUrl();
        if (url.startsWith("http")) {
            url = resource.getUrl();
        } else if (url.startsWith("/")) {
            url = head + request.getContextPath() + url;
        } else {
            url = head + request.getContextPath() + AppConstants.getAdminPath() + "/" + url;
        }
        menu.setHref(url);
        return menu;
    }

    private String getHeadFromUrl(String url) {
        int firSplit = url.indexOf("//");
        String proto = url.substring(0, firSplit + 2);
        int webSplit = url.indexOf("/", firSplit + 2);
        int portIndex = url.indexOf(":", firSplit);
        String webUrl = url.substring(firSplit + 2, webSplit);
        String port = "";
        if (portIndex >= 0) {
            webUrl = webUrl.substring(0, webUrl.indexOf(":"));
            port = url.substring(portIndex + 1, webSplit);
        } else {
            port = "80";
        }
        return proto + webUrl + ":" + port;
    }

    /**
     * 异步方式返回session信息
     *
     * @reload 刷新Session信息
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"sessionInfo"})
    @ResponseBody
    public Result sessionInfo(@RequestParam(name = "reload", defaultValue = "false") boolean reload) {
        Result result = Result.successResult();
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (reload && null != sessionInfo) {
            SecurityUtils.reloadSession(sessionInfo.getUserId());
            sessionInfo = SecurityUtils.getCurrentSessionInfo();
        }
        result.setObj(sessionInfo);
        if (logger.isDebugEnabled()) {
            logger.debug(result.toString());
        }
        return result;
    }
}