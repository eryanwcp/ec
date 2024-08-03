package com.eryansky.common.orm.mybatis.sensitive.type.handler;

import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveType;
import com.eryansky.common.orm.mybatis.sensitive.type.SensitiveTypeHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * 姓名脱敏的解析类
 * 例子：李四 ：李*
 * 张三丰 ：张*丰
 *
 * @author Eryan
 * @version 2019-12-13
 */
public class NameSensitiveHandler implements SensitiveTypeHandler {
    @Override
    public SensitiveType getSensitiveType() {
        return SensitiveType.CHINESE_NAME;
    }

    @Override
    public String handle(Object src) {
        if (src == null) {
            return "";
        }
        String fullName = src.toString();
        if(fullName.length() > 2){
            return fullName.replaceAll("(.).+(.)", "$1*$2");
        }
        String name = StringUtils.left(fullName, 1);
        return StringUtils.rightPad(name, StringUtils.length(fullName), "*");
    }
}
