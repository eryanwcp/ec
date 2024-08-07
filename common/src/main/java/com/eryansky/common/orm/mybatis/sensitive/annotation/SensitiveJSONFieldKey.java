package com.eryansky.common.orm.mybatis.sensitive.annotation;

import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveType;

import java.lang.annotation.*;

/**
 * json字段中需要脱敏的key字段以及key脱敏类型
 *
 * @author Eryan
 * @version 2019-12-13
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface SensitiveJSONFieldKey {
    /**
     * json中的key的类型
     *
     * @return key
     */
    String key();
    /**
     * 该属性从json中key取得
     *
     * @return 返回字段名
     */
    String bindKey();

    /**
     * 脱敏类型
     * 不同的脱敏类型置换*的方式不同
     *
     * @return SensitiveType
     */
    SensitiveType type();
}
