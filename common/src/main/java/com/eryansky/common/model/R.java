package com.eryansky.common.model;

import java.io.Serializable;

/**
 * 通用响应结果封装类
 *
 * @param <T> 数据对象类型
 */
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int NO_LOGIN = -1;
    public static final int SUCCESS = 200;
    public static final int FAIL = 500;
    public static final int NO_PERMISSION = 403;

    private String msg = "操作成功！";
    private int code = SUCCESS;
    private T data;

    public R() {
        super();
    }

    public R(T data) {
        super();
        this.data = data;
    }

    public R(T data, String msg) {
        super();
        this.data = data;
        this.msg = msg;
    }

    public R(Throwable e) {
        super();
        // 增加 NPE 防空保护，防止 e.getMessage() 为 null 时 json 显示 null
        this.msg = (e != null && e.getMessage() != null) ? e.getMessage() : "系统执行异常";
        this.code = FAIL;
    }

    // ==================== 静态工厂方法 (Factory Methods) ====================

    /**
     * 成功响应（无数据）
     */
    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(SUCCESS);
        return r;
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(SUCCESS);
        r.setData(data);
        return r;
    }

    /**
     * 成功响应（带数据与自定义提示信息）
     */
    public static <T> R<T> ok(T data, String msg) {
        R<T> r = new R<>();
        r.setCode(SUCCESS);
        r.setData(data);
        r.setMsg(msg);
        return r;
    }

    /**
     * 失败响应（默认 500 状态码）
     */
    public static <T> R<T> fail() {
        R<T> r = new R<>();
        r.setCode(FAIL);
        r.setMsg("操作失败！");
        return r;
    }

    /**
     * 失败响应（自定义提示信息）
     */
    public static <T> R<T> fail(String msg) {
        R<T> r = new R<>();
        r.setCode(FAIL);
        r.setMsg(msg);
        return r;
    }


    /**
     * 失败响应（自定义状态码与提示信息）
     */
    public static <T> R<T> fail(T data,  String msg) {
        R<T> r = new R<>();
        r.setCode(FAIL);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    /**
     * 自定义响应（自定义状态码与提示信息）
     */
    public static <T> R<T> custom(T data, int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    /**
     * 根据 boolean 结果快速返回 R
     */
    public static R<Boolean> rest(boolean result) {
        R<Boolean> r = new R<>();
        r.setData(result);
        r.setCode(result ? SUCCESS : FAIL);
        r.setMsg(result ? "操作成功！" : "操作失败！");
        return r;
    }

    // ==================== Getter & Setter ====================

    public String getMsg() {
        return msg;
    }

    public R<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public int getCode() {
        return code;
    }

    public R<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public T getData() {
        return data;
    }

    public R<T> setData(T data) {
        this.data = data;
        return this;
    }

    /**
     * 常用便捷判定（Jackson 序列化时会自动生成 "success": true/false 节点）
     */
    public boolean isSuccess() {
        return SUCCESS == code;
    }

    @Override
    public String toString() {
        return "R{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}