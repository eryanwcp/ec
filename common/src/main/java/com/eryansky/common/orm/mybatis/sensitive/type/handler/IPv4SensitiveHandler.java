package com.eryansky.common.orm.mybatis.sensitive.type.handler;

import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveType;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveTypeHandler;
import com.eryansky.common.utils.StringUtils;

/**
 * IPv4脱敏，如：脱敏前：192.0.2.1；脱敏后：192.*.*.*。
 *
 * @author Eryan
 * @version 2024-06-25
 */
public class IPv4SensitiveHandler implements SensitiveTypeHandler {
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
        value = StringUtils.substringBefore(value, ".") + ".*.*.*";
        return value;
    }

}
