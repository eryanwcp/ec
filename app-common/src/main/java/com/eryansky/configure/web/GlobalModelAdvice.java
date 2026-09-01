package com.eryansky.configure.web;

import com.eryansky.common.utils.encode.Cryptos;
import com.eryansky.common.utils.encode.Sm4Utils;
import com.eryansky.encrypt.config.EncryptProvider;
import com.eryansky.encrypt.enums.CipherMode;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @Value("${devMode:true}")
    private boolean devMode;

    @Value("${adminPath:/a}")
    private String adminPath;

    @Value("${mobilePath:/m}")
    private String mobilePath;

    @Value("${frontPath:/f}")
    private String frontPath;

    @ModelAttribute("ctx")
    public String ctx(HttpServletRequest request) {
        return request.getContextPath();
    }

    @ModelAttribute("ctxAdmin")
    public String ctxAdmin(HttpServletRequest request) {
        return request.getContextPath() + adminPath;
    }

    @ModelAttribute("ctxMobile")
    public String ctxMobile(HttpServletRequest request) {
        return request.getContextPath() + mobilePath;
    }

    @ModelAttribute("ctxFront")
    public String ctxFront(HttpServletRequest request) {
        return request.getContextPath() + frontPath;
    }

    @ModelAttribute("ctxStatic")
    public String ctxStatic(HttpServletRequest request) {
        return request.getContextPath() + "/static";
    }

    @ModelAttribute("urlSuffix")
    public String urlSuffix(HttpServletRequest request) {
        return AppConstants.getUrlSuffix();
    }

    @ModelAttribute("appURL")
    public String appURL(HttpServletRequest request) {
        return AppUtils.getAppURL();
    }

    @ModelAttribute("sysInitTime")
    public long sysInitTime(HttpServletRequest request) {
        return AppConstants.SYS_INIT_TIME;
    }

    @ModelAttribute("publicKey")
    public String publicKey(HttpServletRequest request) {
        return EncryptProvider.publicKeyBase64();
    }


    @ModelAttribute("requestEncrypt")
    public String requestEncrypt(HttpServletRequest request) throws Exception {
        return CipherMode.SM4.name();//AES SM4
    }
//    @ModelAttribute("requestEncryptKey")
//    public String requestEncryptKey(HttpServletRequest request) throws Exception {
//        return requestEncryptAesKey(request);
//    }
//
//    @ModelAttribute("requestEncryptAesKey")
//    public String requestEncryptAesKey(HttpServletRequest request) throws Exception {
//        return Cryptos.getBase64EncodeKey();//AES base64编码
//    }
//
//    @ModelAttribute("requestEncryptSm4Key")
//    public String requestEncryptSm4Key(HttpServletRequest request) throws Exception {
//        return Sm4Utils.generateHexKeyString();//hex编码
//    }

    @ModelAttribute("yuicompressor")
    public String yuicompressor(HttpServletRequest request) {
        return AppConstants.isdevMode() ? "" : ".min";
    }


    @ModelAttribute("appName")
    public String appName(HttpServletRequest request) {
        return AppConstants.getAppName();
    }

    @ModelAttribute("appFullName")
    public String appFullName(HttpServletRequest request) {
        return AppConstants.getAppFullName();
    }

}