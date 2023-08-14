package com.eryansky.common.orm.mybatis.sensitive.type;

/**
 * 脱敏类型
 *
 * @author Eryan
 * @version 2019-12-13
 */
public enum SensitiveType {
    /**
     * 不脱敏
     */
    NONE,
    /**
     * 默认脱敏方式
     */
    DEFAUL,
    /**
     * 中文名
     */
    CHINESE_NAME,
    /**
     * 身份证号
     */
    ID_CARD,
    /**
     * 座机号
     */
    FIXED_PHONE,
    /**
     * 手机号
     */
    MOBILE_PHONE,
    /**
     * 地址
     */
    ADDRESS,
    /**
     * 电子邮件
     */
    EMAIL,
    /**
     * 银行卡
     */
    BANK_CARD,
    /**
     * 公司开户银行联号
     */
    CNAPS_CODE,
    /**
     * 支付签约协议号
     */
    PAY_SIGN_NO,
    /**
     * 店铺编号
     */
    SHOP_CODE
}
