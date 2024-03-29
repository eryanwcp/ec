/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.dao;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.MyBatisDao;
import com.eryansky.common.orm.persistence.BaseDao;

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

}
