package com.eryansky.common.orm.mybatis.sensitive.type.handler;

import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveType;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveTypeHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * 手机号或座机脱敏处理类
 * 手机号脱敏:手机号的前3位和后4位保留，其余的隐藏。18233583070 脱敏后: 182****3030
 * 座机脱敏：座机的前2位和后4位保留，其余的隐藏。
 *
 * @author Eryan
 * @version 2019-12-13
 */
public class MobileOrFixexPhoneSensitiveHandler implements SensitiveTypeHandler {
    @Override
    public SensitiveType getSensitiveType() {
        return SensitiveType.MOBILE_OR_FIXED_PHONE;
    }

    @Override
    public String handle(Object src) {
        if (src == null) {
            return null;
        }
        String value = src.toString();
        return StringUtils.left(value, value.length() == 11 ? 3:2).concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(value, 4), StringUtils.length(value), "*"), "***"));
    }

}
