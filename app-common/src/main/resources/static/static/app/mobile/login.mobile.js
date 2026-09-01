$.afui.useOSThemes = false;
var $loginName;
var $password;
var requestEncrypt = requestEncrypt;
var publicKey = publicKey;
var securityToken = securityToken;
$(function () {
    $loginName = $("#loginName");
    $password = $("#password");
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
    let encryptKey = '';
    let requestEncryptKey = '';
    if("SM4" === requestEncrypt){
        encryptKey = Sm4Utils.generateSm4HexKey();
        requestEncryptKey = RSAUtils.encryptHexString(encryptKey,publicKey);
        _password = Sm4Utils.encrypt(_password,encryptKey);
    }else if("AES" === requestEncrypt){
        encryptKey = Cryptos.generateAesBase64Key();
        requestEncryptKey = RSAUtils.encryptBase64String(encryptKey,publicKey);
        _password = Cryptos.encrypt(_password,encryptKey);
    }

    $.ajax({
        url: ctxAdmin + '/login/login',
        type: 'post',
        headers: {"Encrypt": requestEncrypt, "Encrypt-Key": requestEncryptKey},
        data: {
            client_id: $("#client_id").val(),
            redirect_uri: $("#redirect_uri").val(),
            loginName: $("#loginName").val(),
            password: _password,
            _csrf_token: securityToken,
            checkDevice: false
        },
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