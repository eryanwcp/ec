/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.eryansky.common.exception.SystemException;
import com.eryansky.common.model.*;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm._enum.StatusState;
import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.UserAgentUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.utils.encode.RSAUtils;
import com.eryansky.common.web.servlet.ValidateCodeServlet;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.CookieUtils;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.security.annotation.PrepareOauth2;
import com.eryansky.core.web.annotation.MobileValue;
import com.eryansky.modules.sys.service.ResourceService;
import com.eryansky.modules.sys.service.UserPasswordService;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.modules.sys.vo.PasswordTip;
import com.eryansky.utils.AppUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.eryansky.core.security.SecurityConstants;
import com.eryansky.core.security.SecurityType;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.utils.CacheUtils;
import com.eryansky.modules.sys.mapper.Resource;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 用户登录/注销等前端交互入口
 *
 * @author Eryan
 * @date : 2014-05-02 19:50
 */
@Controller
@RequestMapping(value = {"${adminPath}/login","${mobilePath}/login"})
public class LoginController extends SimpleController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserPasswordService userPasswordService;
    @Autowired
    private ResourceService resourceService;
    private static final int RESULT_CODE_APP_VERSION_ERROR = 5;//APP版本禁止登录
    private static final int RESULT_CODE_DEVICE_ERROR = 4;//移动设备校验错误码
    private static final int RESULT_CODE_PASSWORD_ERROR = 6;//密码过期等
    /**
     * 欢迎页面
     *
     * @return
     */
    @PrepareOauth2(enable = false)
    @Mobile(value = MobileValue.ALL)
    @RequiresUser(required = false)
    @GetMapping(value = {"welcome", ""})
    public ModelAndView welcome(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("login");
        String loginName = CookieUtils.getCookie(SpringMVCHolder.getRequest(), "loginName");
        boolean isValidateCodeLogin = isValidateCodeLogin(loginName, false, false);
        modelAndView.addObject("isValidateCodeLogin", isValidateCodeLogin);
        modelAndView.addObject("isMobile", UserAgentUtils.isMobile(request));
        String randomSecurityToken = Identities.randomBase62(64);
        modelAndView.addObject("securityToken", randomSecurityToken);
        modelAndView.addObject("publicKey", RSAUtils.getDefaultBase64PublicKey());
        CacheUtils.put("securityToken:"+request.getSession().getId(),randomSecurityToken);
        return modelAndView;
    }


    /**
     * 是否是验证码登录
     *
     * @param useruame 用户名
     * @param isFail   计数加1
     * @param clean    计数清零
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean isValidateCodeLogin(String useruame, boolean isFail, boolean clean) {
        Map<String, Integer> loginFailMap = CacheUtils.get("loginFailMap");
        if (loginFailMap == null) {
            loginFailMap = Maps.newHashMap();
            CacheUtils.put("loginFailMap", loginFailMap);
        }
        Integer loginFailNum = loginFailMap.get(useruame);
        if (loginFailNum == null) {
            loginFailNum = 0;
        }
        if (isFail) {
            loginFailNum++;
            loginFailMap.put(useruame, loginFailNum);
        }
        if (clean) {
            loginFailMap.remove(useruame);
        }
        return loginFailNum >= AppConstants.getLoginAgainSize();
    }

    /**
     * 登录用户数校验
     */
    private void checkLoginLimit() {
        String loginName = SpringMVCHolder.getRequest().getParameter("loginName");
        if(StringUtils.isBlank(loginName)){
            return;
        }
        if(null != AppConstants.getLimitUserWhiteList().stream().filter(v->StringUtils.simpleWildcardMatch(v,loginName.toUpperCase())).findAny().orElse(null)){
            return;
        }
        int maxSize = AppConstants.getSessionUserMaxSize();
        if (maxSize < 0) {//系统维护
            throw new SystemException("系统正在维护，请稍后再试！");
        } else if (maxSize != 0) {//0 为不限制
            int sessionUser = SecurityUtils.getSessionInfoSize();
            if (sessionUser >= maxSize) {
                throw new SystemException("系统当前登录用户数量过多，请稍后再试！");
            }
        }
    }

    /**
     * 预登录信息获取 动态登录码等信息
     *
     * @return
     */
    @RequiresUser(required = false)
    @RequestMapping(value = {"prepareLogin"},method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Result prepareLogin(HttpServletRequest request){
        String randomSecurityToken = Identities.randomBase62(64);
        String publicKey = RSAUtils.getDefaultBase64PublicKey();
        Map<String,Object> data = Maps.newHashMap();
        data.put("securityToken:",randomSecurityToken);
        data.put("publicKey",publicKey);
        CacheUtils.put("securityToken:"+request.getSession().getId(),randomSecurityToken);
        return Result.successResult().setObj(data);
    }

    /**
     * 后端登录生成公钥方法
     * @return
     */
    @GetMapping(value = "getPublicKey")
    @ResponseBody
    public Result RSAKey(){
        String publicKey = RSAUtils.getDefaultBase64PublicKey();
        return Result.successResult().setObj(publicKey);
    }

    /**
     * 登录验证
     *
     * @param loginName    用户名
     * @param password     密码
     * @param encrypt      加密
     * @param validateCode 验证码
     * @param theme        主题
     * @param request
     * @return
     */
    @PrepareOauth2(enable = false)
    @RequiresUser(required = false)
    @ResponseBody
    @PostMapping(value = {"login"})
    public Result login(@RequestParam(required = true) String loginName,
                        @RequestParam(required = true) String password,
                        @RequestParam(defaultValue = "true") String encrypt,
                        String validateCode,
                        String theme, HttpServletRequest request, Model uiModel) {
        //登录限制
        checkLoginLimit();

        loginName = StringUtils.trim(loginName);
        String securityToken = CacheUtils.get("securityToken:"+request.getSession().getId());

        Result result = null;
        String msg = null;
        int loginAgainSize = AppConstants.getLoginAgainSize();
        final String VALIDATECODE_TIP = "密码输入错误超过"+(loginAgainSize == 0 ? 1:loginAgainSize)+"次，请输入验证码!";
        boolean isValidateCodeLogin = isValidateCodeLogin(loginName, false, false);
        if (isValidateCodeLogin && UserAgentUtils.isComputer(request)) {
            if (StringUtils.isBlank(validateCode)) {
                return Result.errorResult().setMsg("密码输入错误超过"+(loginAgainSize == 0 ? 1:loginAgainSize)+"次，请输入验证码!").setObj(isValidateCodeLogin);
            }
            if (!ValidateCodeServlet.validate(request, validateCode)) {
                msg = "验证码不正确或验证码已过期!";
                return Result.errorResult().setMsg(msg).setObj(isValidateCodeLogin);
            }
        }

        String originPassword = password;
        String _password = password;
        if ("RSA".equals(encrypt)) {
            originPassword = RSAUtils.decryptBase64String(_password);
        }
        if (!"true".equals(encrypt)) {
            _password = Encrypt.e(originPassword);
        }



        // 获取用户信息
        User user = userService.getUserByLP(loginName, _password,securityToken);
        boolean flag = null != user;
        if(null  == user && AppConstants.isdevMode()){
            user = userService.getUserByLoginName(loginName);
            flag = null != user;
        }



        if (!flag) {
            msg = "用户名或密码不正确!";
        } else if (user.getStatus().equals(StatusState.LOCK.getValue())) {
            msg = "该用户已被锁定，暂不允许登陆!";
        }
        if (msg != null) {
            isValidateCodeLogin = isValidateCodeLogin(loginName, true, false);
            if (isValidateCodeLogin && UserAgentUtils.isComputer(request)){
                msg += VALIDATECODE_TIP;
            }
            result = new Result(Result.ERROR, msg, isValidateCodeLogin);
        } else {
            boolean isSecurityOn = AppConstants.getIsSecurityOn();
            if (isSecurityOn) {
                List<SessionInfo> userSessionInfos = SecurityUtils.findSessionInfoByLoginName(loginName);
                int sessionUserLimitSize = AppConstants.getUserSessionSize();
                if(sessionUserLimitSize > 0 &&  userSessionInfos.size() >=sessionUserLimitSize ){
                    result = new Result(Result.ERROR, "已达到用户最大会话登录限制["+ sessionUserLimitSize+"，请注销其它登录信息后再试！]", sessionUserLimitSize);
                    return result;
                }
            }

            boolean isMobile = UserAgentUtils.isMobile(request);
            if (isSecurityOn && AppConstants.isCheckLoginPassword()) {
                PasswordTip passwordTip = userPasswordService.checkPassword(user.getId());
                if (passwordTip.isTip()) {
                    String token = SecurityUtils.createUserToken(user);
                    passwordTip.setUrl(AppUtils.createLocalSecurityUpdatePasswordUrl(request,token));
                    Map<String,Object> data = Maps.newHashMap();
                    data.put("url", passwordTip.getUrl());
                    data.put("passwordTip",passwordTip);
                    data.put("userId",user.getId());
                    data.put("loginName",user.getLoginName());
                    data.put("token",token);
                    return new Result().setCode(RESULT_CODE_PASSWORD_ERROR).setObj(data).setMsg(passwordTip.getMsg());
                }
            }

            //将用户信息放入session中
            SessionInfo sessionInfo = SecurityUtils.putUserToSession(request, user);
            userService.login(sessionInfo.getUserId());
            CacheUtils.remove("securityToken:"+request.getSession().getId());
            logger.info("用户登录系统：{} {}", user.getLoginName(), SpringMVCHolder.getIp());

            //设置调整URL 如果session中包含未被授权的URL 则跳转到该页面
//            String resultUrl = request.getContextPath()+ AppConstants.getAdminPath()  + "/login/index?theme=" + theme;
            String resultUrl = request.getContextPath() + AppConstants.getAdminPath();
            Object unAuthorityUrl = request.getSession().getAttribute(SecurityConstants.SESSION_UNAUTHORITY_URL);
            if (unAuthorityUrl != null) {
                resultUrl = unAuthorityUrl.toString();
                //清空未被授权的URL
                request.getSession().setAttribute(SecurityConstants.SESSION_UNAUTHORITY_URL, null);
            }
            //返回
            Map<String, Object> data = Maps.newHashMap();
            data.put("sessionInfo", sessionInfo);
            data.put("homeUrl", resultUrl);
            result = new Result(Result.SUCCESS, "用户验证通过!", data);
            isValidateCodeLogin(loginName, false, true);
        }

        return result;
    }


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PrepareOauth2(enable = false)
    @PostMapping(value = {"logout"})
    @ResponseBody
    public Result postlogout(HttpServletRequest request) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo != null) {
            //由监听器更新在线用户列表
            SecurityUtils.removeSessionInfoFromSession(sessionInfo.getId(), SecurityType.logout);
            logger.info("用户退出系统：{}", sessionInfo.getLoginName());
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
            //由监听器更新在线用户列表
            SecurityUtils.removeSessionInfoFromSession(sessionInfo.getId(), SecurityType.logout);
            logger.info("用户退出系统：{}", sessionInfo.getLoginName());
        }
        return "redirect:/";
    }


    /**
     * 自动登录
     * @param loginName
     * @param token
     * @param request
     * @param uiModel
     * @return
     */
    @PrepareOauth2(enable = false)
    @RequiresUser(required = false)
    @ResponseBody
    @PostMapping(value = {"autoLogin"})
    public Result autoLogin(@RequestParam(required = true) String loginName,
                            @RequestParam(required = true) String token,
                            HttpServletRequest request, Model uiModel) {
        //登录限制
        checkLoginLimit();

        Result result = null;

        boolean verify = false;
        User user = UserUtils.getUserByLoginName(loginName);
        if(null == user){
            logger.warn("不存在账号：{}", loginName);
            return Result.errorResult().setMsg("系统不存在账号["+loginName+"]！");
        }

        try {
            verify = SecurityUtils.verifySessionInfoToken(token,loginName,user.getPassword());
        } catch (Exception e) {
            if(!(e instanceof TokenExpiredException)){
                logger.error("Token校验失败：{}，{},{},{}",loginName, SpringMVCHolder.getIp(),  token, e.getMessage());
            }
        }
        if(!verify){
            return Result.errorResult().setMsg("Token校验失败！");
        }

        SessionInfo sessionInfo = SecurityUtils.putUserToSession(request, user);
        userService.login(sessionInfo.getUserId());
        SecurityUtils.refreshSessionInfo(sessionInfo);

        logger.info("用户自动登录：{} {}", user.getLoginName(), SpringMVCHolder.getIp());

        //设置跳转URL 如果session中包含未被授权的URL 则跳转到该页面
        String resultUrl = request.getContextPath()+ AppConstants.getAdminPath();
        Map<String,Object> data = Maps.newHashMap();
        data.put("sessionInfo",sessionInfo);
        data.put("homeUrl",resultUrl);
        result = new Result(Result.SUCCESS, "用户自动登录成功!", data);
        return result;
    }


    /**
     * 切换账号 无需密码
     * @param request
     * @param loginName
     * @return
     */
    @PostMapping(value = {"toggleLogin"})
    @ResponseBody
    public Result toggleLogin(HttpServletRequest request,@RequestParam(value = "loginName") String loginName) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (sessionInfo == null) {
            return Result.errorResult().setMsg("未登录！");
        }
        if(!sessionInfo.getLoginNames().contains(loginName)){
            return Result.errorResult().setMsg("未授权账号【"+loginName+"】！");
        }

        User user = UserUtils.getUserByLoginName(loginName);
        if(null == user){
            return Result.errorResult().setMsg("账号不存在【"+loginName+"】！");
        }

        SecurityUtils.removeSessionInfoFromSession(sessionInfo.getId(),SecurityType.logout,false);
        SessionInfo newSessionInfo = SecurityUtils.putUserToSession(request,user);
        userService.login(newSessionInfo.getUserId());

        logger.info("切换账号：{} -> {} {}", sessionInfo.getLoginName(),newSessionInfo.getLoginName(), SpringMVCHolder.getIp());
        Map<String,Object> data = Maps.newHashMap();
        data.put("sessionInfo",newSessionInfo);
        return Result.successResult().setObj(data);
    }


    @GetMapping(value = {"index"})
    public String index(String theme) {
        //根据客户端指定的参数跳转至 不同的主题 如果未指定 默认:index
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
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"navTree"})
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


    /**
     * @param resources
     * @return
     */
    private List<SiderbarMenu> resourcesToSiderbarMenu(Collection<Resource> resources) {
        List<SiderbarMenu> tempMenus = Lists.newArrayList();
        if (Collections3.isEmpty(resources)) {
            return tempMenus;
        }
        for (Resource r : resources) {
            tempMenus.add(resourceToSiderbarMenu(r));
        }

        List<SiderbarMenu> tempTreeNodes = Lists.newArrayList();
        Map<String, SiderbarMenu> tempMap = Maps.newLinkedHashMap();

        for (SiderbarMenu treeNode : tempMenus) {
            tempMap.put(treeNode.getId(), treeNode);
            tempTreeNodes.add(treeNode);
        }


        Set<String> keyIds = tempMap.keySet();
        Set<String> removeKeyIds = Sets.newHashSet();
        Iterator<String> iteratorKey = keyIds.iterator();
        while (iteratorKey.hasNext()) {
            String key = iteratorKey.next();
            SiderbarMenu treeNode = null;
            for (SiderbarMenu treeNode1 : tempTreeNodes) {
                if (treeNode1.getId().equals(key)) {
                    treeNode = treeNode1;
                    break;
                }
            }

            if (null != treeNode && StringUtils.isNotBlank(treeNode.getpId())) {
                SiderbarMenu pTreeNode = getParentSiderbarMenu(treeNode.getpId(), tempTreeNodes);
                if (pTreeNode != null) {
                    for (SiderbarMenu treeNode2 : tempTreeNodes) {
                        if (treeNode2.getId().equals(pTreeNode.getId())) {
                            treeNode2.addChild(treeNode);
                            removeKeyIds.add(treeNode.getId());
                            break;
                        }
                    }

                }
            }

        }

        //remove
        if (Collections3.isNotEmpty(removeKeyIds)) {
            keyIds.removeAll(removeKeyIds);
        }

        List<SiderbarMenu> result = Lists.newArrayList();
        keyIds = tempMap.keySet();
        iteratorKey = keyIds.iterator();
        while (iteratorKey.hasNext()) {
            String _key = iteratorKey.next();
            SiderbarMenu treeNode = null;
            for (SiderbarMenu treeNode4 : tempTreeNodes) {
                if (treeNode4.getId().equals(_key)) {
                    treeNode = treeNode4;
                    result.add(treeNode);
                    break;
                }
            }

        }
        return result;
    }


    /**
     * 资源转Menu
     *
     * @param resource 资源
     * @return
     */
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
     * 查找父级节点
     *
     * @param parentId
     * @param menus
     * @return
     */
    private SiderbarMenu getParentSiderbarMenu(String parentId, Collection<SiderbarMenu> menus) {
        SiderbarMenu t = null;
        for (SiderbarMenu menu : menus) {
            if (parentId.equals(menu.getId())) {
                t = menu;
                break;
            }
        }
        return t;
    }

    /**
     * 当前在线用户
     *
     * @throws Exception
     */
    @PostMapping(value = {"onlineDatagrid"})
    @ResponseBody
    public Datagrid<SessionInfo> onlineDatagrid(HttpServletRequest request) throws Exception {
        Page<SessionInfo> page = new Page<>(request);
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        page = SecurityUtils.findSessionInfoPage(page,sessionInfo.isSuperUser() ? null:sessionInfo.getLoginCompanyId());
        Datagrid<SessionInfo> dg = new Datagrid<>(page.getTotalCount(), page.getResult());
        return dg;
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

    /**
     * 资源转M
     *
     * @param resource 资源
     * @return
     */
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

    /**
     * 根据URL地址获取请求地址前面部分信息
     *
     * @param url
     * @return
     */
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
     * @reload 刷新Session信息
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"sessionInfo"})
    @ResponseBody
    public Result sessionInfo(Boolean reload) {
        Result result = Result.successResult();
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (null != reload && reload && null != sessionInfo) {
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
