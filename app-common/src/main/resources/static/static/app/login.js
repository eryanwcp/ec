var ctx = ctx;
var ctxAdmin = ctxAdmin;
var ctxStatic = ctxStatic;
var sysInitTime = sysInitTime;
var isValidateCodeLogin = isValidateCodeLogin;
var rememberMeCookieValue = rememberMeCookieValue;
var needEncrypt = needEncrypt;
var SALT = SALT;
var securityToken = securityToken;
var homePage = homePage;


var $loginForm;
var $password, $rememberMe;
$(function () {
    $.backstretch([
        ctxStatic + '/js/images/bg1.jpg?_='+sysInitTime,
        ctxStatic + '/js/images/bg2.jpg?_='+sysInitTime,
        ctxStatic + '/js/images/bg3.jpg?_='+sysInitTime
    ], {duration: 5000, fade: 2000});
    $("#qr").qrcode({
        "render": "div",
        "size": 80,
        "color": "#3a3",
        "text": window.location.href
    });

    if(isValidateCodeLogin != "" && isValidateCodeLogin == "true"){
        $(".validateCode").show();
    }

    $loginForm = $("#loginForm").validate({
        rules: {
            validateCode: {remote: ctx + '/servlet/ValidateCodeServlet'}
        },
        messages: {
            loginName: {required: "请填写用户名."},password: {required: "请填写密码."},
            validateCode: {remote: "验证码不正确.", required: "请填写验证码."}
        },
        errorLabelContainer: "#messageBox",
        errorPlacement: function(error, element) {
            error.appendTo($("#loginError").parent());
        },
        submitHandler:function(form){
            login();
        }
    });

    $password = $("#password");
    $rememberMe = $("#rememberMe");

    $rememberMe.prop("checked", rememberMeCookieValue == "" ? false : true);

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
            $.cookie('_password', _password, {
                expires: 7
            });
            $.cookie('rememberMe', checked, {
                expires: 7
            });
        } else {
            $.cookie('_password', "", {
                expires: 7
            });
            $.cookie('rememberMe', "", {
                expires: 7
            });
        }
    });

});
// 登录
function login() {
    $("#messageBox2").addClass("hide");
    $.cookie('loginName', $("#loginName").val(), {
        expires: 7
    });
    var _password = $password.val();
    if(needEncrypt){
        _password = md5(md5(_password+SALT)+securityToken);
    }
    if ($rememberMe.prop("checked")) {
        $.cookie('_password', _password, {
            expires: 7
        });
    }
    $.ajax({
        url: ctxAdmin + '/login/login',
        type: 'post',
        data: {theme:'',encrypt:true,loginName:$("#loginName").val(),password:_password,validateCode:$("#validateCode").val()},
        traditional: true,
        async:false,
        dataType: 'json',
        success: function (data) {
            if (data.code == 1) {
                window.location = data.obj.homeUrl;
                //setTimeout(function(){//延时1秒 集群环境等待缓存同步
                //    window.location = data.obj;
                //},1000);
            }else if (data.code == 6) {//需要设置密码
                $("#loginError2").html(data.msg);
                $("#messageBox2").removeClass("hide");
                setTimeout(function(){
                   window.location.href = $.unEscape(data['obj']['url']);
                },3*1000);
            } else {
                $('#validateCode').val('');
                refreshCheckCode();
                $("#loginError2").html(data.msg);
                $("#messageBox2").removeClass("hide");
                if (data.obj != undefined && data.obj == true) {
                    $(".validateCode").show();
                }
            }
        }
    });
}

//刷新验证码
function refreshCheckCode() {
    //加上随机时间 防止IE浏览器不请求数据
    var url = ctx + '/servlet/ValidateCodeServlet?' + new Date().getTime();
    $('#validateCodeImage').attr('src', url);
}