<%@ page import="com.eryansky.common.web.utils.CookieUtils" %>
<%@ page import="com.eryansky.common.utils.encode.Encrypt" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ include file="/common/taglibs.jsp" %>
<%
    String loginNameOrName = CookieUtils.getCookie(request, "loginName");
    request.setAttribute("loginNameOrName",loginNameOrName);
%>
<html>
<head>
    <title>用户登录 - ${fns:getAppFullName()}</title>
    <meta http-equiv="X-UA-Compatible" content="IE=EDGE;chrome=1" />
    <meta name="renderer" content="webkit">
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8"/>
    <meta http-equiv="Cache-Control" content="no-store"/>
    <meta http-equiv="Pragma" content="no-cache"/>
    <meta http-equiv="Expires" content="0"/>
    <link rel="shortcut icon" href="${ctxStatic}/img/favicon.ico" />
    <script type="text/javascript" src="${ctxStatic}/js/jquery/jquery-1.12.4.min.js" charset="utf-8"></script>
    <script type="text/javascript" src="${ctxStatic}/js/jquery/jquery-migrate-1.4.1.min.js" charset="utf-8"></script>
    <%-- jQuery Cookie插件 --%>
    <script type="text/javascript" src="${ctxStatic}/js/jquery/jquery.cookie${yuicompressor}.js" charset="utf-8"></script>
    <link id="easyuiTheme" rel="stylesheet" type="text/css" href="${ctxStatic}/js/easyui/themes/<c:out value="${cookie.easyuiThemeName.value}" default="bootstrap"/>/easyui.css" />
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/js/easyui/extend/icon/easyui-icon.css" />
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/js/easyui/extend/icon/eu-icon.css" />
    <script type="text/javascript" src="${ctxStatic}/js/easyui/jquery.easyui.mine${yuicompressor}.js" charset="utf-8"></script>

    <%-- jQuery方法扩展 --%>
    <script type="text/javascript" src="${ctxStatic}/js/jquery/jquery-extend${yuicompressor}.js" charset="utf-8"></script>

    <link href="${ctxStatic}/js/jquery-validation-1.19.3/dist/extend/jquery-validate-extend.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/js/jquery-validation-1.19.3/dist/jquery.validate.min.js" type="text/javascript"  charset="utf-8"></script>
    <script src="${ctxStatic}/js/jquery-validation-1.19.3/dist/extend/jquery-validate-extend-methods.min.js" type="text/javascript"  charset="utf-8"></script>
    <script src="${ctxStatic}/js/jquery/qrcode/jquery.qrcode.min.js" type="text/javascript"></script>

    <link rel="stylesheet" href="${ctxStatic}/js/common/typica-login.css?_=${sysInitTime}">
    <script src="${ctxStatic}/js/common/backstretch.min.js"></script>

    <link rel="stylesheet" type="text/css" href="${ctxStatic}/js/bootstrap/2.3.2/css/bootstrap.css" />
    <link rel="stylesheet" type="text/css" href="${ctxStatic}/js/bootstrap/2.3.2/js/bootstrap.js" />
    <!--[if lte IE 6]><link href="${ctxStatic}/js/bootstrap/bsie/css/bootstrap-ie6.min.css" type="text/css" rel="stylesheet" />
<script src="${ctxStatic}/js/bootstrap/bsie/js/bootstrap-ie.min.js" type="text/javascript"></script><![endif]-->
    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]> <script src="${ctxStatic}/js/common/html5.js"></script><![endif]-->
    <script src="${ctxStatic}/js/md5/md5.min.js"></script>
    <style type="text/css">
        .control-group{border-bottom:0px;}
        .login-form label {
            display: inline-block;
        }
    </style>
    <script type="text/javascript">
        var ctx = "${ctx}";
        var ctxAdmin = "${ctxAdmin}";
        var ctxStatic = "${ctxStatic}";
        var sysInitTime = "${sysInitTime}";
        var isValidateCodeLogin = "${isValidateCodeLogin}";
        var rememberMeCookieValue = "${cookie.rememberMe.value}";
        var needEncrypt = ${empty cookie._password.value};
        var SALT = "<%=Encrypt.SALT%>";
        var securityToken = "${securityToken}";
        var homePage = "<%=request.getContextPath() + AppConstants.getAppHomePage()%>";
    </script>
    <script type="text/javascript" src="${ctxStatic}/app/login${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
</head>
<body>
<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>
            <div id="qr" style="float: right"></div>
        </div>
    </div>
</div>

<div class="container">
    <!--[if lte IE 6]><br/><div class='alert alert-block' style="text-align:left;padding-bottom:10px;"><a class="close" data-dismiss="alert">x</a><h4>温馨提示：</h4><p>你使用的浏览器版本过低。为了获得更好的浏览体验，我们强烈建议您 <a href="http://browsehappy.com" target="_blank">升级</a> 到最新版本的IE浏览器，或者使用较新版本的 Chrome、Firefox、Safari 等。</p></div><![endif]-->
    <div id="messageBox" class="alert alert-error hide"><button data-dismiss="alert" class="close">×</button>
        <label id="loginError" class="error"></label>
    </div>
    <div id="messageBox2" class="alert alert-error hide"><button data-dismiss="alert" class="close" onclick="$('#messageBox2').addClass('hide');">×</button>
        <label id="loginError2" class="error"></label>
    </div>
    <div id="login-wraper">
        <form id="loginForm" action="${ctxAdmin}/login/login?theme=${cookie.themeType.value}" class="form login-form" method="post" >
            <legend><span style="color:#08c;">${fns:getAppFullName()}</span></legend>
            <div class="body">
                <div class="control-group">
                    <%--<label for="loginName" class="control-label">用户名</label>--%>
                    <div class="controls">
                        <input type="text" id="loginName" name="loginName" class="required" style="width: 210px;height:36px;padding: 5px;"
                               value="${fns:urlDecode(loginNameOrName)}" placeholder="用户名"/>
                        <i class="icon-user" title="用户名"></i>
                    </div>
                </div>

                <div class="control-group">
                    <%--<label for="password" class="control-label">密码</label>--%>
                    <div class="controls">
                        <input type="password" id="password" name="password"  value="${cookie._password.value}"
                               class="required" style="width: 210px;height:36px;padding: 5px;" placeholder="密码"/>
                        <i class="icon-lock" title="密码"></i>
                    </div>
                </div>

                <div class="validateCode" style="display: none;">
                    <label for="validateCode" class="control-label">验证码：</label>
                    <tags:validateCode name="validateCode" inputCssStyle="margin-bottom:0;height:30px;"/>
                </div>

            </div>
            <div class="footer">
                <label class="checkbox inline" style="border: none;">
                    <input type="checkbox" id="rememberMe" name="rememberMe"> <span style="color:#08c;">记住我</span>
                </label>
                <input class="btn btn-primary" type="submit" value="登 录"/>
            </div>
        </form>
    </div>
</div>
<footer class="white navbar-fixed-bottom">
    版权所有 &copy; 2013-${fns:getDate('yyyy')} <a target="_blank" href="${fns:getAppProductURL()}">${fns:getAppProductName()}</a> ${fns:getAppProductContact()}
</footer>
</body>
</html>
