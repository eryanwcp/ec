package com.eryansky.modules.sys.vo;

import java.io.Serializable;

public class TableDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tableSchema;
    private String tableName;

    private String tableComment;


    public TableDTO setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
        return this;
    }

    public TableDTO setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public TableDTO setTableComment(String tableComment) {
        this.tableComment = tableComment;
        return this;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getTableComment() {
        return this.tableComment;
    }
}