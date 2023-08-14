/**
 *  Copyright (c) 2012-2022 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.orm.mybatis.service;

import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.persistence.CrudDao;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.core.orm.mybatis.entity.BaseEntity;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.google.common.collect.Lists;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Service基类
 * @author Eryan
 * @version 2014-05-16
 */
public abstract class CrudService<D extends CrudDao<T>, T extends BaseEntity<T>> extends BaseService {

	/**
	 * 持久层对象
	 */
	@Autowired
	protected D dao;


	/**
	 * 获取单条数据
	 * @param id
	 * @return
	 */
	public T get(String id) {
		return dao.get(id);
	}

	/**
	 * 获取单条数据
	 * @param entity
	 * @return
	 */
	public T get(T entity) {
		return dao.get(entity);
	}

	/**
	 * 查询列表数据
	 * @param entity
	 * @return
	 */
	public List<T> findList(T entity) {
		return dao.findList(entity);
	}

	/**
	 * 查询分页数据
	 * @param page 分页对象
	 * @param entity
	 * @return
	 */
	public Page<T> findPage(Page<T> page, T entity) {
		entity.setEntityPage(page);
		page.setResult(dao.findList(entity));
		return page;
	}


	/**
	 * 动态查询（多个对象）
	 * @param selectStatement
	 * @return
	 */
	public List<T> selectMany(SelectStatementProvider selectStatement){
		return dao.selectMany(selectStatement);
	}

	/**
	 * 动态查询（单个对象）
	 * @return
	 */
	public T selectOne(SelectStatementProvider selectStatement){
		return dao.selectOne(selectStatement);
	}


	/**
	 * 保存数据（插入或更新）
	 * @param entity
	 */
	public void save(T entity) {
		if (entity.getIsNewRecord()){
			entity.prePersist();
			dao.insert(entity);
		}else{
			entity.preUpdate();
			dao.update(entity);
		}
	}

	/**
	 * 保存数据（批量插入）
	 * @param list
	 */
	public int insertBatch(Collection<T> list){
		return dao.insertBatch(list);
	}

	/**
	 * 保存数据（批量拆分插入） 每次限定100条
	 *
	 * @param list
	 */
	public void insertAutoBatch(Collection<T> list) {
		insertAutoBatch(list, null);
	}

	/**
	 * 保存数据（批量拆分插入）
	 *
	 * @param list
	 * @param group 分组大小，默认值：100
	 */
	public void insertAutoBatch(Collection<T> list, Integer group) {
		List<List<T>> groupList = Collections3.fixedGrouping(Lists.newArrayList(list), null != group ? group : 100);
		for (List<T> fixedGroup : groupList) {
			dao.insertBatch(fixedGroup);
		}
	}


	/**
	 * 删除数据（逻辑删除）
	 * @param entity
	 */
	public void delete(T entity) {
		entity.preUpdate();
		dao.delete(entity);
	}


	/**
	 * 删除数据（逻辑删除）
	 * @param id
	 */
	@Deprecated
	public void delete(String id) {
		dao.delete(id);
	}

	/**
	 * 删除或还原删除数据
	 * @param entity
	 * @param isRe 是否还原
	 */
	public void delete(T entity, Boolean isRe) {
		if(isRe != null && isRe){
			if(entity instanceof DataEntity){
				DataEntity dataEntity = (DataEntity) entity;
				dataEntity.setStatus(DataEntity.STATUS_NORMAL);
			}
			save(entity);
		}else{
			delete(entity);
		}
	}

	/**
	 * 删除数据（物理删除）
	 * @param entity
	 */
	public int clear(T entity) {
		return dao.clear(entity);
	}

	/**
	 * 删除数据（物理删除）
	 * @param id
	 */
	public int clear(String id) {
		return dao.clear(id);
	}

    /**
     * 清空数据（物理删除）
     */
    public int clearAll() {
        return dao.clearAll();
    }
}
