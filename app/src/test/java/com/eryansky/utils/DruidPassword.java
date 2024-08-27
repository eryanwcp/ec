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
        System.out.println(a.decrypt("d61e0c08056e2dde35a8af83579c7b93e1423ce0ec29b354"));
    }
}
