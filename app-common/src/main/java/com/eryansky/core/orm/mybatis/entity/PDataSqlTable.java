/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.orm.mybatis.entity;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

import java.io.Serializable;
import java.util.Date;


/**
 * SqlTable支持类
 * @author Eryan
 * @version 2020-03-03
 */
public abstract class PDataSqlTable<T, PK extends Serializable> extends SqlTable {

    public final SqlColumn<PK> id = column("id");
    public final SqlColumn<String> status = column("status");
    public final SqlColumn<Integer> version = column("version");
    public final SqlColumn<String> createUser = column("create_user");
    public final SqlColumn<Date> createTime = column("create_time");
    public final SqlColumn<String> updateUser = column("update_user");
    public final SqlColumn<Date> updateTime = column("update_time");

    public PDataSqlTable(String tableName) {
        super(tableName);
    }

}
