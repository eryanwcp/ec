package com.eryansky.modules.sys.vo;

/**
 * @author Eryan
 * @date 2018-06-14
 */
public class PasswordTip {

    public static final int CODE_NO = 0;
    public static final int CODE_YES = 1;//从未修改
    public static final int CODE_TIME_OUT = 2;//超期未修改

    private int code;
    private String msg;
    private String url;

    public PasswordTip() {
        this.code = CODE_NO;
    }

    public PasswordTip(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public PasswordTip(int code, String msg, String url) {
        this.code = code;
        this.msg = msg;
        this.url = url;
    }

    public int getCode() {
        return code;
    }

    public PasswordTip setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public PasswordTip setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public PasswordTip setUrl(String url) {
        this.url = url;
        return this;
    }

    public boolean isTip() {
        return CODE_YES == code || CODE_TIME_OUT == code;
    }

}
