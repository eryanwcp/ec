package com.eryansky.common.orm.mybatis.sensitive.type.handler;

import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveType;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveTypeHandler;
import com.eryansky.common.utils.StringUtils;

/**
 * 【密码】密码的全部字符都用*代替，比如：******
 *
 * @author Eryan
 * @version 2024-06-25
 */
public class PasswordSensitiveHandler implements SensitiveTypeHandler {
    @Override
    public SensitiveType getSensitiveType() {
        return SensitiveType.IPV4;
    }

    @Override
    public String handle(Object src) {
        if (src == null) {
            return null;
        }
        String value = src.toString();
        value =  StringUtils.repeat('*', value.length());;
        return value;
    }

}
