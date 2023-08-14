package com.eryansky.utils;

import com.eryansky.common.orm.mybatis.sensitive.encrypt.AesSupport;
import com.eryansky.common.utils.encode.Encryption;
import com.eryansky.common.utils.encode.Encryption;

/**
 * @author
 * @date 2019-08-07 
 */
public class DruidPassword {
    public static void main(String[] args) throws Exception {
        AesSupport a = new AesSupport();
        System.out.println(a.encrypt("password"));
        System.out.println(a.decrypt(a.encrypt("password")));
        System.out.println(a.decrypt("6c6a6e9194f90785296e0a794dedf34d1f300ec703cf2b4e"));
    }
}
