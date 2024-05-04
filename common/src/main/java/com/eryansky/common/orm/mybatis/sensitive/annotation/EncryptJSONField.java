package com.eryansky.common.orm.mybatis.sensitive.annotation;

import java.lang.annotation.*;

/**
 * 对json内的key_value进行加密
 *
 * @author Eryan
 * @version 2019-12-13
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EncryptJSONField {
    /**
     * 需要加密的字段的数组
     *
     * @return 返回结果
     */
    EncryptJSONFieldKey[] encryptList();
}
