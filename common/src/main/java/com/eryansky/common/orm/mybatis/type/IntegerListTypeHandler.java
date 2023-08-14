package com.eryansky.common.orm.mybatis.type;

/**
 * @author Eryan
 * @version 2020-02-12
 */
public class IntegerListTypeHandler extends ListTypeHandler<Integer> {

    public IntegerListTypeHandler() {
        super("int");
    }
}