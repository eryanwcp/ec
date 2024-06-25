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
    DEFAULT,
    /**
     * 密码
     */
    PASSWORD,
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
     * 手机号或座机号
     */
    MOBILE_OR_FIXED_PHONE,
    /**
     * 地址
     */
    ADDRESS,
    /**
     * 电子邮件
     */
    EMAIL,
    /**
     * 中国大陆车牌，包含普通车辆、新能源车辆
     */
    CAR_LICENSE,
    /**
     * 银行卡
     */
    BANK_CARD,
    /**
     * IPv4地址
     */
    IPV4,
    /**
     * IPv6地址
     */
    IPV6,
    /**
     * 公司开户银行联号
     */
    CNAPS_CODE,
    /**
     * 支付签约协议号
     */
    PAY_SIGN_NO,
    /**
     * 统一社会信用代码
     */
    UniformSocialCreditCode,
    /**
     * 店铺编号
     */
    SHOP_CODE
}
