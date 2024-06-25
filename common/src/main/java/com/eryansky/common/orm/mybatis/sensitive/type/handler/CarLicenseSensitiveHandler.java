package com.eryansky.common.orm.mybatis.sensitive.type.handler;

import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveType;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveTypeHandler;
import com.eryansky.common.utils.StringUtils;

/**
 * 【中国车牌】车牌中间用*代替
 * eg1：null       -》 ""
 * eg1：""         -》 ""
 * eg3：苏D40000   -》 苏D4***0
 * eg4：陕A12345D  -》 陕A1****D
 * eg5：京A123     -》 京A123     如果是错误的车牌，不处理
 *
 * @author Eryan
 * @version 2024-06-25
 */
public class CarLicenseSensitiveHandler implements SensitiveTypeHandler {
    @Override
    public SensitiveType getSensitiveType() {
        return SensitiveType.CAR_LICENSE;
    }

    @Override
    public String handle(Object src) {
        if (src == null) {
            return null;
        }
        String value = src.toString();
        // 普通车牌
        if (value.length() == 7) {
            value = StringUtils.hide(value, 3, 6);
        } else if (value.length() == 8) {
            // 新能源车牌
            value = StringUtils.hide(value, 3, 7);
        }
        return value;
    }

}
