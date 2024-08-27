package com.eryansky.encrypt.aspectj;

import com.eryansky.encrypt.anotation.Decrypt;
import com.eryansky.encrypt.anotation.Encrypt;
import com.eryansky.encrypt.badger.HoneyBadgerEncrypt;
import com.eryansky.encrypt.enums.CipherMode;
import com.eryansky.encrypt.handler.ScenarioHolder;
import com.eryansky.encrypt.handler.StorageScenario;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;

/**
 * The type Encrypt handler.
 * AES加密处理器
 *
 * @author : 尔演@Eryan
 * @date : 2022-07-23
 */
@Order(1)
@Aspect
public class EncryptHandler{
    /**
     * The constant echo.
     */
    private static final Logger echo = LoggerFactory.getLogger(EncryptHandler.class);
    public static String  AESKEY = "AES-RSA";
    public static String  SM4KEY = "SM4-RSA";


    private final HoneyBadgerEncrypt honeyBadgerEncrypt;

    public EncryptHandler(@NonNull HoneyBadgerEncrypt honeyBadgerEncrypt) {
        this.honeyBadgerEncrypt = honeyBadgerEncrypt;
    }

    /**
     * 根据不同场景 选择性调用 传输加密:对执行结果 存储加密:对参数
     *
     * @param joinPoint the join point
     * @return the object
     * @throws Throwable the throwable
     */
    @Around("@annotation(encrypt)")
    public Object encrypt(ProceedingJoinPoint joinPoint , Encrypt encrypt) throws Throwable {
        //混合加密 if encrypt.dynamic() == true 则是动态模式 每一次的密钥都会变化
        if (encrypt.cipher().equals(CipherMode.SM4_RSA) || encrypt.cipher().equals(CipherMode.AES_RSA)){
            honeyBadgerEncrypt.initHybridEncryption(encrypt.cipher(),encrypt.dynamic());
        }

        try {
            return ScenarioHolder.scenarioSchedule(joinPoint);
        }finally {
            HoneyBadgerEncrypt.rsaCiphertexts.clear();
            HoneyBadgerEncrypt.symmetricCryptos.clear();
        }
    }

    /**
     * 传输解密:对参数  存储解密:对执行结果
     *
     * @param joinPoint the join point
     * @param decrypt   the decrypt
     * @return the object
     * @throws Throwable the throwable
     */
    @Around("@annotation(decrypt)")
    public Object decrypt(ProceedingJoinPoint joinPoint, Decrypt decrypt) throws Throwable {
        //混合加密 if encrypt.dynamic() == true 则是动态模式 每一次的密钥都会变化
        if (decrypt.cipher().equals(CipherMode.SM4_RSA) || decrypt.cipher().equals(CipherMode.AES_RSA)){
            honeyBadgerEncrypt.initHybridDecryption(decrypt.cipher(),decrypt.dynamic());
        }
        try {
            return ScenarioHolder.scenarioSchedule(joinPoint);
        }finally {
            HoneyBadgerEncrypt.rsaCiphertexts.clear();
            HoneyBadgerEncrypt.symmetricCryptos.clear();
        }
    }
}
