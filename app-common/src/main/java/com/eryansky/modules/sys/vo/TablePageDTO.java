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

    public void setPage(Page<Map<String, Object>> page) {
        this.page = page;
    }

    public List<TableColumnDTO> getColumns() {
        return columns;
    }

    public void setColumns(List<TableColumnDTO> columns) {
        this.columns = columns;
    }
}