package com.eryansky.encrypt.generator;

import com.eryansky.encrypt.enums.CipherMode;

/**
 * The interface Generator secret key.
 * 密钥生成统一接口
 * @author : 尔演@Eryan
 *
 */
public interface GeneratorSecretKey {
    /**
     * RSA生成base64编码字符串
     * AES生成密钥 key  偏移量iv 长度16
     *
     * @param cipherMode the cipher mode
     * @return 生产密钥的方法 {@link CipherMode} 代理类接口
     */
    Object generatorKey(CipherMode cipherMode);
}
