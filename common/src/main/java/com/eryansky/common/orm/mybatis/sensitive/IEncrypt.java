package com.eryansky.common.orm.mybatis.sensitive;

import java.util.List;

/**
 * 加解密接口
 *
 * @author Eryan
 * @version 2019-12-13
 */
public interface IEncrypt {

    /**
     * 对字符串进行加密存储
     *
     * @param data 源
     * @return 返回加密后的密文
     */
    String encrypt(String data);
    /**
     * 对字符串进行加密存储
     *
     * @param datas 源
     * @return 返回加密后的密文
     */
    List<String> batchEncrypt(List<String> datas);

    /**
     * 对加密后的字符串进行解密
     *
     * @param data 加密后的字符串
     * @return 返回解密后的原文
     */
    String decrypt(String data);

    /**
     * 对加密后的字符串进行解密
     *
     * @param datas 加密后的字符串
     * @return 返回解密后的原文
     */
    List<String> batchDecrypt(List<String> datas);
}
