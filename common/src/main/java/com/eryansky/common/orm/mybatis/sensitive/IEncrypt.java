package com.eryansky.common.orm.mybatis.sensitive;

/**
 * 加解密接口
 *
 * @author 尔演&Eryan eryanwcp@gmail.com
 * @version 2019-12-13
 */
public interface IEncrypt {
    /**
     * 对字符串进行加密存储
     *
     * @param src 源
     * @return 返回加密后的密文
     * @throws RuntimeException 算法异常
     */
    String encrypt(String src);

    /**
     * 对加密后的字符串进行解密
     *
     * @param encrypt 加密后的字符串
     * @return 返回解密后的原文
     * @throws RuntimeException 算法异常
     */
    String decrypt(String encrypt);
}
