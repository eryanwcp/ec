package com.eryansky.modules.sys.vo;

import java.io.Serializable;

public class TableColumnDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String columnName;

    private String columnNameCamelCase;

    private String columnComment;

    private String comment;

    private String columnType;

    private String dataType;

    private String isNullable;

    private String columnKey;

    private String variableType;


    public TableColumnDTO setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public TableColumnDTO setColumnNameCamelCase(String columnNameCamelCase) {
        this.columnNameCamelCase = columnNameCamelCase;
        return this;
    }

    public TableColumnDTO setColumnComment(String columnComment) {
        this.columnComment = columnComment;
        return this;
    }

    public TableColumnDTO setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public TableColumnDTO setColumnType(String columnType) {
        this.columnType = columnType;
        return this;
    }

    public TableColumnDTO setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    public TableColumnDTO setIsNullable(String isNullable) {
        this.isNullable = isNullable;
        return this;
    }

    public TableColumnDTO setColumnKey(String columnKey) {
        this.columnKey = columnKey;
        return this;
    }

    public TableColumnDTO setVariableType(String variableType) {
        this.variableType = variableType;
        return this;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public String getColumnNameCamelCase() {
        return this.columnNameCamelCase;
    }

    public String getColumnComment() {
        return this.columnComment;
    }

    public String getComment() {
        return this.comment;
    }

    public String getColumnType() {
        return this.columnType;
    }

    public String getDataType() {
        return this.dataType;
    }

    public String getIsNullable() {
        return this.isNullable;
    }

    public String getColumnKey() {
        return this.columnKey;
    }

    public String getVariableType() {
        return this.variableType;
    }

}