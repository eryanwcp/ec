package com.eryansky.common.orm.mybatis.sensitive;

import java.util.List;
import java.util.Map;

/**
 * 加解密接口
 *
 * @author Eryan
 * @version 2019-12-13
 */
public interface IEncrypt {

    /**
     * 加密方法 不为空时，优先级高于注解
     *
     */
    String defaultType();

    /**
     * 对字符串进行加密存储
     *
     * @param data 源
     * @param type 加密、解密方法
     * @return 返回加密后的密文
     */
    String encrypt(String data,String type);
    /**
     * 对字符串进行加密存储
     *
     * @param datas 源
     * @param type 加密、解密方法
     * @return 返回加密后的密文
     */
    List<String> batchEncrypt(List<String> datas,String type);
    /**
     * 对字符串进行加密存储
     *
     * @param mapData 源 key 加密方法 value需要加密的数据
     * @return 返回加密后的密文
     */
    Map<String,List<String>> batchEncrypt(Map<String,List<String>> mapData);

    /**
     * 对加密后的字符串进行解密
     *
     * @param data 加密后的字符串
     * @param type 加密、解密方法
     * @return 返回解密后的原文
     */
    String decrypt(String data,String type);

    /**
     * 对加密后的字符串进行解密
     *
     * @param datas 加密后的字符串
     * @param type 加密、解密方法
     * @return 返回解密后的原文
     */
    List<String> batchDecrypt(List<String> datas,String type);
    /**
     * 对加密后的字符串进行解密
     *
     * @param mapData key 加密方法 value需要解密的数据
     * @return 返回解密后的原文
     */
    Map<String,List<String>> batchDecrypt(Map<String,List<String>> mapData);
}
