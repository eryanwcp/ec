/**
 * 移动端公用javascript
 */
/**
 * 打开URL
 * @param args0
 * @param arg1
 * @param arg2
 */
function openURL(args0, arg1, arg2,arg3) {
    if (arguments.length === 1) {
        openURL1(args0);
    } else if (arguments.length === 3) {
        openURL2(args0, arg1, arg2);
    } else if (arguments.length === 4) {
        openURL3(args0, arg1, arg2,arg3);
    }
}
/**
 * 打开URL
 * @param url
 */
function openURL1(url){
    try {
        WebViewPlugin.webView(url);//移动端接口
    } catch (e) {
        window.location.href = url;
    }
}
/**
 * 打开URL
 * @param url
 * @param pathType {@link MicroAppPathType}
 * @param title
 */
function openURL2(url,pathType,title){
    try {
        window.flutter_inappwebview.callHandler('isAvaiable',{}).then(function (result) {
            if(result){
                window.flutter_inappwebview.callHandler('openURL',{"pathType":pathType,"url":url,"title":title}).then(function (result) {
                    console.log(result);
                });
            }else{
                WebViewPlugin.webView(url);//移动端接口
            }
        });

    } catch (e) {
        WebViewPlugin.webView(url);//移动端接口
    }
}

/**
 * 打开URL
 * @param url
 * @param pathType {@link MicroAppPathType}
 * @param title
 */
function openURL3(url,pathType,title,args){
    try {
        // window.addEventListener("flutterInAppWebViewPlatformReady", function (event) { });
        window.flutter_inappwebview.callHandler('openURL',{"pathType":pathType,"url":url,"title":title,"args":args}).then(function (result) {
            console.log(result);
        });
    } catch (e) {
        WebViewPlugin.webView(url);//移动端接口
    }
}
/**
 * 打开URL
 * @param url
 */
function openURLExtend(url){
    try {
        WebViewPlugin.webView.extend(url);//移动端接口
    } catch (e) {
        window.location.href = url;
    }
}
/**
 * 打开URL
 * @param url
 */
function openURLExtendOpen(url){
    try {
        WebViewPlugin.webView.extendOpen(url);//移动端接口
    } catch (e) {
        window.location.href = url;
    }
}

/**
 * 在浏览器中打开网页
 * @param url
 */
function browser(url){
    try {
        WebViewPlugin.browser(url);
    } catch (e) {
        window.location.href = url;
    }
}



/**
 * 关闭或返回
 */
function goBack(){
    try {
        WebViewPlugin.goBack();
    } catch (e) {
        history.go(-1);
    }
}
/**
 * 关闭或返回
 */
function closeOrGoBack(){
    WebViewPlugin.closeOrGoBack();
}

/**
 * 返回或关闭
 */
function goBackOrClose(){
    WebViewPlugin.goBackOrClose();
}

/**
 * 关闭
 */
function close(){
    WebViewPlugin.close();
}

/**
 * 同步标题
 * @param title
 */
function syncTitle(title){
    try {
        WebViewPlugin.syncTitle(title);
    } catch (e) {
        console.log(e);
    }
}

/**
 * 重定向到登录页面
 */
function redirectLogin(url){
    try {
        WebViewPlugin.redirectLogin();
    } catch (e) {
        if(url){
            window.location.href = url;
        }

    }
}

/**
 * 设置a标签忽略属性 不异步加载
 * @param parentSelect
 */
function resetAToNormal(parentSelect){
    var select = "a";
    if(parentSelect != undefined){
        $(parentSelect).find("a").attr({"data-ignore":"true"});
    }
    $(select).attr({"data-ignore":"true"});
}

/**
 * 计算总页数
 * @param total 总记录数
 * @param pageSize 分页大小
 * @returns {number}
 */
function getTotalPages(total,pageSize) {
    if (total < 0) {
        return -1;
    }

    var count = parseInt(total / pageSize);
    if (total % pageSize > 0) {
        count++;
    }
    return count;
}

/**
 * 获取URL中参数
 * 例如：var param1 = request.QueryString('param1')
 * @type {{QueryString: Function}}
 */
var request = {
    QueryString: function (param) {
        var uri = window.location.search;
        var re = new RegExp("" + param + "=([^&?]*)", "ig");
        return ((uri.match(re)) ? (uri.match(re)[0].substr(param.length + 1)) :null);
    }
};

$.isApp = function() {
    var ua = window.navigator.userAgent.toLowerCase();
    if (ua.match(/cordova-mp/i) == 'cordova-mp' || ua.match(/mp-flutter/i) == 'mp-flutter') {
        return true;
    } else {
        return false;
    }
};