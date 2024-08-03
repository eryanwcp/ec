package com.eryansky.encrypt.handler;

import com.eryansky.encrypt.anotation.Decrypt;
import com.eryansky.encrypt.anotation.Encrypt;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Transmit scenario.
 * TransmitScenario 适用场景网络传输
 * @author : 尔演@Eryan
 *
 */
public class TransmitScenario extends ScenarioHandler {
    private static final Logger echo = LoggerFactory.getLogger(TransmitScenario.class);

    @Override
    public void transmitEncryptProcessor(Object process, MethodSignature signature, Encrypt encrypt) throws Throwable {
        super.transmitEncryptProcessor(process, signature, encrypt);

    }

    @Override
    public void transmitDecryptProcessor(Object[] args, MethodSignature signature, Decrypt decrypt) throws Throwable {
        super.transmitDecryptProcessor(args, signature, decrypt);
    }
}
