/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.orm.mybatis.entity;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

import java.util.Date;


/**
 * SqlTable支持类
 * @author Eryan
 * @version 2020-03-03
 */
public abstract class BaseSqlTable<T> extends SqlTable {

    public final SqlColumn<String> id = column("id");

    public BaseSqlTable(String tableName) {
        super(tableName);
    }


}
