/**
 * Copyright (c) 2012-2024 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.dao;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.MyBatisDao;
import com.eryansky.common.orm.persistence.BaseDao;
import com.eryansky.modules.sys.vo.TableColumnDTO;
import com.eryansky.modules.sys.vo.TableDTO;

import java.util.List;
import java.util.Map;

/**
 * 系统DAO接口
 *
 * @author Eryan
 * @date 2017-09-19
 */
@MyBatisDao
public interface SystemDao extends BaseDao {

    /**
     * organ表同步到扩展表
     *
     * @param parameter 参数
     * @return
     */
    int insertToOrganExtend(Parameter parameter);
    /**
     * organ表同步到扩展表 存储过程 批量处理
     *
     * @param parameter 参数
     * @return
     */
    int insertToOrganExtendByFunction(Parameter parameter);

    /**
     * 删除organ扩展表数据
     *
     * @param parameter 参数
     * @return
     */
    int deleteOrganExtend(Parameter parameter);

    List<TableDTO> findTableList(Parameter parameter);

    List<TableColumnDTO> findTableColumnByTableName(Parameter parameter);

    List<Map<String,Object>> findTableDataByTableName(Parameter parameter);

}
