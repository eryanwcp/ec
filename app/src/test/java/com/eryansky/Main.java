/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky;

import com.eryansky.common.orm.mybatis.sensitive.IEncrypt;
import com.eryansky.common.orm.mybatis.sensitive.encrypt.AesSupport;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.*;

/**
 * @author Eryan
 * @date 2020-06-17 
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Encryption as = new Encryption();
        System.out.println(as.encrypt("1","key"));//7e0cd7be3e66d4a8
        System.out.println(as.decrypt(as.encrypt("1","key"),"key"));

//        IEncrypt iEncrypt = new AesSupport("1");
//        System.out.println(iEncrypt.encrypt("1"));;
//        System.out.println(iEncrypt.decrypt(iEncrypt.encrypt("1")));;
//

//
        String key = "1234567890123456";
        String value = "23";
//        byte[] iv = Cryptos.generateIV();
        Byte[] iv = Collections3.toObjects(value.getBytes());
        System.out.println(EncryptionSafe.byteArr2HexStr(Cryptos.aesEncrypt(value.getBytes(),key.getBytes(),iv)));
        System.out.println(new String(Cryptos.aesDecrypt(Cryptos.aesEncrypt(value.getBytes(),key.getBytes(),iv),key.getBytes(),iv)));
        System.out.println(new String(Cryptos.aesDecrypt(EncryptionSafe.hexStr2ByteArr("ca63c3fb80a12fe67d38086fdf1c9bb35eeb"),key.getBytes(),iv)));


        System.out.println(EncryptionSafe.encrypt(value,key));
        System.out.println(EncryptionSafe.decrypt(EncryptionSafe.encrypt(value,key),key));
        System.out.println(EncryptionSafe.decrypt("6538b7f6d8536daddf19da54b699fdc934bc",key));

        System.out.println(String.format("%1$"+16+ "s", key+'a'));

    }
}
