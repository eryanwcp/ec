package com.eryansky.common.orm.mybatis.sensitive.type.handler;

import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveType;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveTypeHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * 银行卡号脱敏
 * 只留前四位和后四位
 * 6227 0383 3938 3938 393 脱敏结果: 6227 **** **** ***8 393
 *
 * @author Eryan
 * @version 2019-12-13
 */
public class BandCardSensitiveHandler implements SensitiveTypeHandler {
    @Override
    public SensitiveType getSensitiveType() {
        return SensitiveType.BANK_CARD;
    }

    @Override
    public String handle(Object src) {
        if (src == null) {
            return null;
        }
        String bankCard = src.toString();
        return StringUtils.left(bankCard, 4).concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(bankCard, 4), StringUtils.length(bankCard), "*"), "***"));

    }
}
