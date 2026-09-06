package com.eryansky.modules.sys.vo;

import com.eryansky.common.orm.Page;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class TablePageDTO implements Serializable {
    private Page<Map<String, Object>> page;
    private List<TableColumnDTO> columns;

    public TablePageDTO() {
    }

    public Page<Map<String, Object>> getPage() {
        return page;
    }

    public TablePageDTO setPage(Page<Map<String, Object>> page) {
        this.page = page;
        return this;
    }

    public List<TableColumnDTO> getColumns() {
        return columns;
    }

    public TablePageDTO setColumns(List<TableColumnDTO> columns) {
        this.columns = columns;
        return this;
    }
}