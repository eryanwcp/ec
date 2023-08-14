<%@ page import="com.eryansky.common.web.utils.CookieUtils" %>
<%@ page import="com.eryansky.common.utils.encode.Encrypt" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ include file="/common/taglibs.jsp" %>
<%@ include file="/common/meta_mobile.jsp" %>
<%
    String loginNameOrName = CookieUtils.getCookie(request, "loginName");
    request.setAttribute("loginNameOrName",loginNameOrName);
%>
<html>
<head>
    <title>${fns:getAppFullName()}-用户登录</title>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=0, minimal-ui">
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <META HTTP-EQUIV="Pragma" CONTENT="no-cache">
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/mobile/af/icons.css" />
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/mobile/af/af.ui.css" />

    <script type="text/javascript" charset="utf-8" src="${ctxStatic}/js/jquery/jquery-2.1.4.min.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctxStatic}/js/jquery/jquery.cookie.min.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctxStatic}/mobile/af/fastclick.min.js"></script>
    <script type="text/javascript" charset="utf-8" src="${ctxStatic}/mobile/af/appframework.ui.js"></script>

    <link rel="stylesheet" type="text/css" href="${ctxStatic}/app/mobile/login.mobile${yuicompressor}.css?_=${sysInitTime}" />
    <script src="${ctxStatic}/js/md5/md5.min.js"></script>
    <script type="text/javascript">
        var lPassword = window.localStorage.getItem("password");
        var needEncrypt = null != lPassword;
        var SALT = "<%=Encrypt.SALT%>";
        var securityToken = "${securityToken}";
    </script>
    <script type="text/javascript" charset="utf-8" src="${ctxStatic}/app/mobile/login.mobile${yuicompressor}.js?_=${sysInitTime}"></script>
</head>
<body>
<div class="view">
    <div class="pages">
        <div class="panel" id="loginpanel">
            <img src="${ctxStatic}/img/logo.png" style="position: absolute;top:10px;left:10px;"/>
            <div class="title"><h1>${fns:getAppFullName()}</h1></div>
            <form class="loginform" role="form">
                <div class="inputoutdiv"><input type="text" id="loginName" placeholder="账号"><img src="${ctxStatic}/img/mobile/user.png"/></div>
                <div class="inputoutdiv"><input type="password" id="password" placeholder="密码"><img src="${ctxStatic}/img/mobile/lock.png"/></div>
                <div class="checkboxclass"><input type="checkbox" id="rememberMe" style="border: none;"><label for="rememberMe" style="float: left;position: absolute;left:25px;color: #ffffff;border:none;">记住密码</label></div>
                <a id="btn_login" onclick="login()" class="button login" data-ignore="true">登&nbsp;&nbsp;录</a>
            </form>
        </div>
    </div>
    <footer style="height: 30px;background-color: transparent;border:none;">
        <span style="margin:0 auto;line-height: 30px;text-align: center;">版权所有&copy; 2013-${fns:getDate('yyyy')} ${fns:getAppProductName()}</span>
    </footer>
</div>
</body>
</html>