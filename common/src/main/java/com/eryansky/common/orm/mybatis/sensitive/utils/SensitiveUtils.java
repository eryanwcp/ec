package com.eryansky.common.orm.mybatis.sensitive.utils;

import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveType;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveTypeHandler;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveTypeRegisty;
import com.eryansky.common.utils.StringUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Proxy;

/**
 * 插件工具类，获取到对象的真实对象，可能存在多层代理
 *
 * @author Eryan
 * @version 2019-12-13
 */
public final class SensitiveUtils {

    private SensitiveUtils() {
    }

    /**
     * 获得真正的处理对象,可能多层代理.
     */
    @SuppressWarnings("unchecked")
    public static <T> T realTarget(Object target) {
        if (Proxy.isProxyClass(target.getClass())) {
            MetaObject metaObject = SystemMetaObject.forObject(target);
            return realTarget(metaObject.getValue("h.target"));
        }
        return (T) target;
    }


    /**
     * 获取脱敏数据
     * @param src
     * @param sensitiveType {@link SensitiveType}
     * @return
     */
    public static String getSensitive(String src, SensitiveType sensitiveType) {
        if(null == sensitiveType){
            throw new IllegalArgumentException("参数[sensitiveType]不能为空");
        }
        if(null == src || StringUtils.EMPTY.equals(src)){
            return src;
        }
        SensitiveTypeHandler handler = SensitiveTypeRegisty.get(sensitiveType);
        return handler.handle(src);
    }
}
