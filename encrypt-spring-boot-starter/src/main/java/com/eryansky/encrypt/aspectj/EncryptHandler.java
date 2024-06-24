package com.eryansky.encrypt.aspectj;

import com.eryansky.encrypt.anotation.Decrypt;
import com.eryansky.encrypt.anotation.Encrypt;
import com.eryansky.encrypt.badger.HoneyBadgerEncrypt;
import com.eryansky.encrypt.enums.CipherMode;
import com.eryansky.encrypt.handler.ScenarioHolder;
import com.eryansky.common.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

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
    public static final Log echo = LogFactory.getLog(EncryptHandler.class);

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
            Object[] args = joinPoint.getArgs();
            if(null != args){
                Arrays.stream(args).forEach(v->{
                    if(v instanceof HttpServletResponse){
                        // 拿到密钥 设置到响应头 前端获取 通过RSA解密获取AES密钥 再通过AES解密器对密文解密
                        ((HttpServletResponse) v).setHeader(AESKEY, HoneyBadgerEncrypt.getAesKeyRSACiphertext());
                        //拿到密钥 设置到响应头 前端获取 通过RSA解密获取SM4密钥 再通过SM4解密器对密文解密
                        ((HttpServletResponse) v).setHeader(SM4KEY,HoneyBadgerEncrypt.getSm4KeyRSACiphertext());
                    }
                });
            }
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
            Object[] args = joinPoint.getArgs();
            if(null != args){
                Arrays.stream(args).forEach(v->{
                    if(v instanceof HttpServletRequest){
                        // 拿到密钥 设置到响应头 前端获取 通过RSA解密获取AES密钥 再通过AES解密器对密文解密
                        String aesKey = ((HttpServletRequest) v).getHeader(AESKEY);
                        //拿到密钥 设置到响应头 前端获取 通过RSA解密获取SM4密钥 再通过SM4解密器对密文解密
                        String sm4Key =  ((HttpServletRequest) v).getHeader(SM4KEY);
                        if (StringUtils.isNotBlank(aesKey) && !StringUtils.equals(aesKey,"null") ){
                            HoneyBadgerEncrypt.setRSACiphertextForAESKey(aesKey);
                        }
                        if (StringUtils.isNotBlank(sm4Key) && !StringUtils.equals(sm4Key,"null") ){
                            HoneyBadgerEncrypt.setRSACiphertextForSM4Key(sm4Key);
                        }
                    }
                });
            }
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
