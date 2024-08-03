package com.eryansky.encrypt.handler;

import com.eryansky.encrypt.anotation.Decrypt;
import com.eryansky.encrypt.anotation.Encrypt;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Storage scenario.
 * StorageScenario 使用场景加密存储
 * @author : 尔演@Eryan
 *
 */
public class StorageScenario extends ScenarioHandler {
    private static final Logger echo = LoggerFactory.getLogger(StorageScenario.class);

    @Override
    public void storageEncryptProcessor(Object[] args, MethodSignature signature, Encrypt encrypt) throws Throwable {
        super.storageEncryptProcessor(args, signature, encrypt);
    }

    @Override
    public void storageDecryptProcessor(Object process, MethodSignature signature, Decrypt decrypt) throws Throwable {
         super.storageDecryptProcessor(process, signature, decrypt);
    }
}
