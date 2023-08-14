<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>参数配置</title>
    <meta name="decorator" content="default_sys"/>
    <script type="text/javascript">
        var data = ${fns:toJson(data)};
        $(document).ready(function () {
            $("#inputForm").validate({
                submitHandler: function (form) {
                    loading('正在提交，请稍等...');
                    $("input[type='radio'][name='system.rest.enable']").removeAttr("disabled");
                    form.submit();
                },
                errorContainer: "#messageBox",
                errorPlacement: function (error, element) {
                    $("#messageBox").text("输入有误，请先更正。");
                    if (element.is(":checkbox") || element.is(":radio") || element.parent().is(".input-append")) {
                        error.appendTo(element.parent().parent());
                    } else {
                        error.insertAfter(element);
                    }
                }
            });
            $("input[type='radio'][name='system.security.limit.ip.whiteEnable']").change(function() {
                var value = this.value;
                if (value == "true") {
                    $("textarea[name='system.security.limit.ip.blacklist']").attr("readonly","readonly");
                }else if (value == "false") {
                    $("textarea[name='system.security.limit.ip.blacklist']").removeAttr("readonly");
                }
            });
            $.formLoadData($("#inputForm"),data);
            var value = data['system.security.limit.ip.whiteEnable'];
            if (value == "true") {
                $("textarea[name='system.security.limit.ip.blacklist']").attr("readonly","readonly");
            }else if (value == "false") {
                $("textarea[name='system.security.limit.ip.blacklist']").removeAttr("readonly");
            }


        });
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="${ctxAdmin}/sys/config/paramForm">参数配置</a></li>
    <li><a href="${ctxAdmin}/sys/config">参数配置（更多）</a></li>
</ul><br/>
<form id="inputForm" action="${ctxAdmin}/sys/config/saveParam" method="post" class="form-horizontal">
    <tags:message content="${message}"/>
    <fieldset>
        <legend>基础配置</legend>
        <div class="control-group">
            <label class="control-label">应用版本：</label>
            <div class="controls">
                <input name="app.version" type="text" class="input-xxlarge" readonly/>
                <span class="help-inline"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">应用名称：</label>
            <div class="controls">
                <input name="app.name" type="text" class="input-xxlarge"/>
                <span class="help-inline"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">应用名称（全称）：</label>
            <div class="controls">
                <input name="app.fullName" type="text" class="input-xxlarge"/>
                <span class="help-inline"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">应用名称（简称）：</label>
            <div class="controls">
                <input name="app.shortName" type="text" class="input-xxlarge"/>
                <span class="help-inline"></span>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">厂商信息：</label>
            <div class="controls">
                <input name="app.productName" type="text" class="input-xxlarge"/>
                <span class="help-inline"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">厂商URL：</label>
            <div class="controls">
                <input name="app.productURL" type="text" class="input-xxlarge"/>
                <span class="help-inline"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">客服信息：</label>
            <div class="controls">
                <input name="app.productContact" type="text" class="input-xxlarge"/>
                <span class="help-inline"></span>
            </div>
        </div>
    </fieldset>

    <fieldset>
        <legend>系统安全</legend>
        <div class="control-group">
            <label class="control-label">系统安全：</label>
            <div class="controls">
                <label><input name="security.on" type="radio" value="true"/>启用</label>
                <label><input name="security.on" type="radio" value="false"/>禁用</label>
                <span class="help-inline">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;密码安全、会话控制等</span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">登录密码安全检查：</label>
            <div class="controls">
                <label><input name="security.password.checkLogin" type="radio" value="true"/>启用</label>
                <label><input name="security.password.checkLogin" type="radio" value="false"/>禁用</label>
                <span class="help-inline">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;启用登录密码安全检查</span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">强密码策略：</label>
            <div class="controls">
                <label><input name="security.password.checkPolicy" type="radio" value="true"/>启用</label>
                <label><input name="security.password.checkPolicy" type="radio" value="false"/>禁用</label>
                <span class="help-inline">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;数字、字母、符号混排，至少8位</span>
            </div>
        </div>


        <div class="control-group">
            <label class="control-label">密码更新周期：</label>
            <div class="controls">
                <input name="security.password.updateCycle" type="number" min="0" max="9999" class="input-small"/>
                <span class="help-inline">默认值：30（天）</span>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">密码重复校验：</label>
            <div class="controls">
                <input name="security.password.repeatCount" type="number" min="0" max="100" class="input-small"/>
                <span class="help-inline">用户密码至少多少次内不能重复 不校验：0  默认值：5</span>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">密码错误次数：</label>
            <div class="controls">
                <input name="security.password.loginAgainSize" type="number" min="0" max="100" class="input-small"/>
                <span class="help-inline">开启验证码设置为0 默认值：3</span>
            </div>
        </div>


        <div class="control-group">
            <label class="control-label">最大登录用户数限制：</label>
            <div class="controls">
                <input name="security.sessionUser.MaxSize" type="number" min="-1" max="999999" class="input-small"/>
                <span class="help-inline">不允许登录：小于0 不限制；0 限制：具体数值</span>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">用户可创建会话数量：</label>
            <div class="controls">
                <input name="security.sessionUser.UserSessionSize" type="number" min="0" max="100" class="input-small"/>
                <span class="help-inline">用户可创建会话数量 默认值：0</span>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">日志保留时间：</label>
            <div class="controls">
                <input name="system.logKeepTime" type="number" min="0" max="9999" class="input-small"/>
                <span class="help-inline">数据库审计日志 单位：天</span>
            </div>
        </div>


        <div class="control-group">
            <label class="control-label">账号白名单：</label>
            <div class="controls">
                <textarea name="system.security.limit.user.whitelist" class="input-xxlarge"></textarea>
                <span class="help-inline">不受“最大登录用户数限制”，每行一个或多个之间以";"分割，支持"*"通配符</span>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label">IP访问控制：</label>
            <div class="controls">
                <label><input name="system.security.limit.ip.enable" type="radio" value="true"/>启用</label>
                <label><input name="system.security.limit.ip.enable" type="radio" value="false"/>禁用</label>
                <span class="help-inline">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;启用IP访问控制，全局配置</span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">仅限IP白名单：</label>
            <div class="controls">
                <label><input name="system.security.limit.ip.whiteEnable" type="radio" value="true"/>启用</label>
                <label><input name="system.security.limit.ip.whiteEnable" type="radio" value="false"/>禁用</label>
                <span class="help-inline">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;仅限IP白名单，仅限IP白名单可以访问，IP黑名单无需设置</span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">IP白名单：</label>
            <div class="controls">
                <textarea name="system.security.limit.ip.whitelist" class="input-xxlarge"></textarea>
                <span class="help-inline">系统访问IP白名单（本机无需配置），每行一个或多个之间以";"分割，支持"*"通配符</span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">IP黑名单：</label>
            <div class="controls">
                <textarea name="system.security.limit.ip.blacklist" class="input-xxlarge"></textarea>
                <span class="help-inline">系统访问IP黑名单，每行一个或多个之间以";"分割，支持"*"通配符</span>
            </div>
        </div>
    </fieldset>



    <fieldset>
        <legend>内部代理</legend>
        <div class="control-group">
            <label class="control-label">内部代理：</label>
            <div class="controls">
                <label><input name="system.security.proxy.enable" type="radio" value="true"/>启用</label>
                <label><input name="system.security.proxy.enable" type="radio" value="false"/>禁用</label>
                <span class="help-inline">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;内部代理/a/sys/proxy/*，全局配置</span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">内部代理URL白名单：</label>
            <div class="controls">
                <textarea name="system.security.proxy.whitelist" class="input-xxlarge"></textarea>
                <span class="help-inline">每行一个或多个之间以";"分割，支持"*"通配符</span>
            </div>
        </div>
    </fieldset>


    <fieldset>
        <legend>REST服务</legend>
        <div class="control-group">
            <label class="control-label">REST授权：</label>
            <div class="controls">
                <label><input name="system.rest.enable" type="radio" value="true" disabled/>启用</label>
                <label><input name="system.rest.enable" type="radio" value="false" disabled/>禁用</label>
                <span class="help-inline">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;是否开启REST授权，需重启服务</span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">X-Api-Key：</label>
            <div class="controls">
                <input name="system.rest.defaultApiKey" type="text" class="input-xxlarge"/>
                <span class="help-inline"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">IP访问限制：</label>
            <div class="controls">
                <label><input name="system.rest.limit.ip.enable" type="radio" value="true"/>启用</label>
                <label><input name="system.rest.limit.ip.enable" type="radio" value="false"/>禁用</label>
                <span class="help-inline"></span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">IP白名单：</label>
            <div class="controls">
                <textarea name="system.rest.limit.ip.whitelist" class="input-xxlarge"></textarea>
                <span class="help-inline">每行一个或多个之间以";"分割，支持"*"通配符</span>
            </div>
        </div>
    </fieldset>


<%--    <div class="control-group">--%>
<%--        <label class="control-label">URL黑名单限制：</label>--%>
<%--        <div class="controls">--%>
<%--            <label><input name="system.rest.limit.url.enable" type="radio" value="true"/>启用</label>--%>
<%--            <label><input name="system.rest.limit.url.enable" type="radio" value="false"/>禁用</label>--%>
<%--            <span class="help-inline">需重启服务</span>--%>
<%--        </div>--%>
<%--    </div>--%>

    <div class="form-actions">
        <e:hasPermission name="sys:config:edit">
            <input id="btnSave" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;
        </e:hasPermission>
        <input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
    </div>
</form>


</body>
</html>
