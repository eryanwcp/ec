package com.eryansky.fastweixin.api.entity;

/**
 * 订阅模版参数
 */
public class SubcribeParam extends BaseModel {

    /**
     * 值
     */
    private Object value;

    public SubcribeParam() {
        super();
    }

    public SubcribeParam(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public SubcribeParam setValue(Object value) {
        this.value = value;
        return this;
    }

}
