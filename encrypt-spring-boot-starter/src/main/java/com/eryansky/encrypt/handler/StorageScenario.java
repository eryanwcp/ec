package com.eryansky.encrypt.handler;

import com.eryansky.encrypt.anotation.Decrypt;
import com.eryansky.encrypt.anotation.Encrypt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * The type Storage scenario.
 * StorageScenario 使用场景加密存储
 * @author : 尔演@Eryan
 *
 */
public class StorageScenario extends ScenarioHandler {
    private static final Log echo = LogFactory.getLog(StorageScenario.class);


    @Override
    public void storageEncryptProcessor(Object[] args, MethodSignature signature, Encrypt encrypt) throws Throwable {
        super.storageEncryptProcessor(args, signature, encrypt);
    }

    @Override
    public void storageDecryptProcessor(Object process, MethodSignature signature, Decrypt decrypt) throws Throwable {
         super.storageDecryptProcessor(process, signature, decrypt);
    }
}
