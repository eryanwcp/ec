package com.eryansky.fastweixin.api.entity;

import com.eryansky.fastweixin.util.JSONUtil;

/**
 * 抽象实体类
 *
 * @author Eryan
 * @date 2016-03-15
 */
public abstract class BaseModel implements Model {
    @Override
    public String toJsonString() {
        return JSONUtil.toJson(this);
    }
}
