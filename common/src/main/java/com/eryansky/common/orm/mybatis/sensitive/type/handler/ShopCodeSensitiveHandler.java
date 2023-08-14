package com.eryansky.common.orm.mybatis.sensitive.type.handler;

import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveType;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveTypeHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * 店铺编号脱敏处理类
 * 360121121653 脱敏后: 3601****1653
 *
 * @author Eryan
 * @version 2019-12-13
 */
public class ShopCodeSensitiveHandler implements SensitiveTypeHandler {
    @Override
    public SensitiveType getSensitiveType() {
        return SensitiveType.SHOP_CODE;
    }

    @Override
    public String handle(Object src) {
        if (src == null) {
            return null;
        }
        String value = src.toString();
        return StringUtils.left(value, 4).concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(value, 4), StringUtils.length(value), "*"), "****"));
    }

}
