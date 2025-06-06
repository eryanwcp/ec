package com.eryansky.encrypt.enums;

/**
 * 加密模式
 *
 * @author : 尔演@Eryan
 * @date : 2024-06-17
 */
public enum CipherMode {
    /**
     * AES 对称加密
     */
    AES,
    /**
     * RSA 非对称加密
     */
    RSA,
    /**
     * SM4 对称加密（国密）.
     */
    SM4,
    /**
     * BASE64
     */
    BASE64,
    /**
     * 混合加密 AES_RSA
     */
    AES_RSA,
    /**
     *混合加密 SM4_RSA
     */
    SM4_RSA,
    /**
     * 该值不能用于 @Encrypt @Decrypt注解 这用于 @Badger
     * 如果为DEFAULT 则会使用@Encrypt 或者@Decrypt的加密模式 @Badger注解的默认值为DEFAULT
     * 你可以切换成其他支持的属性 比如AES RSA SM4 或者 混合加密方式
     */
    DEFAULT,

}
