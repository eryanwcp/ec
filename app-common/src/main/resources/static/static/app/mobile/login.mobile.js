$.afui.useOSThemes = false;
var $loginName;
var $password;
var $rememberMe;
var needEncrypt = needEncrypt;
var SALT;
var securityToken;
$(function () {
    $loginName = $("#loginName");
    $password = $("#password");
    $rememberMe = $("#rememberMe");

    $rememberMe.prop("checked", window.localStorage.getItem("rememberMe") !== "");
    $loginName.val(window.localStorage.getItem("loginName"));

    if (window.localStorage.getItem("rememberMe")) {
        $password.val(window.localStorage.getItem("password"));
    } else {
        $loginName.focus();
    }

    $password.change(function(){
        needEncrypt = true;
    });

    $rememberMe.click(function () {
        var checked = $(this).prop('checked');
        var _password = $password.val();
        if(needEncrypt){
            _password = md5(md5(_password+SALT)+securityToken);
        }
        if (checked) {
            window.localStorage.setItem("loginName", $loginName.val());
            window.localStorage.setItem("password", _password);
            window.localStorage.setItem("rememberMe", checked);
        } else {
            window.localStorage.setItem("password", "");
            window.localStorage.setItem("rememberMe", "");
        }
    });

});

function login() {
    if (!$loginName.val()) {
        $.afui.toast({
            message: "账号不能为空!",
            position: "tc",
            autoClose: true, //have to click the message to close
            type: "success"
        });
        return;
    }
    var _password = $password.val();
    if (!_password) {
        $.afui.toast({
            message: "密码不能为空!",
            position: "tc",
            autoClose: true, //have to click the message to close
            type: "success"
        });
        return;
    }
    if(needEncrypt){
        _password = md5(md5(_password+SALT)+securityToken);
    }

    $.ajax({
        url: ctxAdmin + '/login/login',
        type: 'post',
        data: {loginName: $loginName.val(), password: _password, checkDevice: false},
        traditional: true,
        async: false,
        dataType: 'json',
        success: function (data) {
            if (data.code === 1) {
                if (data['obj']) {
                    setTimeout(function () {//延时1秒 集群环境等待缓存同步
                        window.location = data['obj']['homeUrl'];
                    }, 1000);
                } else {
                    setTimeout(function () {//延时1秒 集群环境等待缓存同步
                        window.location = ctxMobile;
                    }, 1000);
                }
            } else {
                $.afui.toast({
                    message: data.msg,
                    position: "tc",
                    autoClose: true, //have to click the message to close
                    type: "warning"
                });
            }
        }
    });
}