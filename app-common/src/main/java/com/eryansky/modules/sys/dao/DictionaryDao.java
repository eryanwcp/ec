/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.dao;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.MyBatisDao;
import com.eryansky.common.orm.persistence.CrudDao;
import com.eryansky.modules.sys.mapper.Dictionary;

import java.util.List;


/**
 * 日志DAO接口
 *
 * @author Eryan
 * @version 2015-9-26
 */
@MyBatisDao
public interface DictionaryDao extends CrudDao<Dictionary> {
    /**
     * 查找第一级所有数据
     *
     * @return
     */
    Dictionary getByCode(Dictionary dictionary);

    /**
     * 根据查询条件查询
     *
     * @param dictionary
     * @return
     */
    Dictionary getBy(Dictionary dictionary);

    /**
     * 查找第一级所有数据
     *
     * @return
     */
    List<Dictionary> findParents(Dictionary dictionary);

    /**
     * 根据编码查找子级所有数据
     *
     * @param dictionary
     * @return
     */
    List<Dictionary> findChilds(Dictionary dictionary);

    /**
     * 查找最大排序值
     *
     * @return
     */
    Integer getMaxSort(Dictionary dictionary);


    /**
     * 自定义SQL查询
     *
     * @param parameter
     * @return
     */
    List<Dictionary> findByWhereSQL(Parameter parameter);

    /**
     * 自定义SQL查询
     *
     * @param parameter
     * @return
     */
    List<Dictionary> findBySql(Parameter parameter);
}
