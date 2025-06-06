/**
 *  Copyright (c) 2012-2024 https://www.eryansky.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.core.orm.mybatis.service;

import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.reflection.ReflectionUtils;
import com.eryansky.core.orm.mybatis.dao.PTreeDao;
import com.eryansky.core.orm.mybatis.entity.PTreeEntity;

import java.io.Serializable;
import java.util.List;

/**
 * Service基类
 * @author Eryan
 * @version 2014-05-16
 */
public abstract class PTreeService<D extends PTreeDao<T,PK>, T extends PTreeEntity<T,PK>,PK extends Serializable> extends PCrudService<D, T,PK> {
	
	public void save(T entity) {
		
		@SuppressWarnings("unchecked")
		Class<T> entityClass = ReflectionUtils.getClassGenricType(getClass(), 1);
		// 如果没有设置父节点，则代表为跟节点，有则获取父节点实体
		if (entity.getParent() == null || entity.getParentId() == null
				 || "0".equals(entity.getParentId()) || Long.valueOf(0).equals(entity.getParentId())){
			entity.setParent(null);
		}else{
			entity.setParent(super.get(entity.getParentId()));
		}
		if (entity.getParent() == null){
			T parentEntity = null;
			try {
				Class idType = entity.getPKType();
				parentEntity = entityClass.getConstructor(Long.class == idType ? Long.class:String.class).newInstance(Long.class == idType ? Long.valueOf(0):"0");
			} catch (Exception e) {
				throw new ServiceException(e);
			}
			entity.setParent(parentEntity);
			entity.getParent().setParentIds(StringUtils.EMPTY);
		}
		
		// 获取修改前的parentIds，用于更新子节点的parentIds
		String oldParentIds = entity.getParentIds();
		
		// 设置新的父节点串
		entity.setParentIds(entity.getParent().getParentIds()+entity.getParent().getId()+",");
		
		// 保存或更新实体
		super.save(entity);
		
		// 更新子节点 parentIds
		T o = null;
		try {
			o = entityClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new ServiceException(e);
		}
		o.setParentIds("%,"+entity.getId()+",%");
		List<T> list = dao.findByParentIdsLike(o);
		for (T e : list){
			if (e.getParentIds() != null && oldParentIds != null){
				e.setParentIds(e.getParentIds().replace(oldParentIds, entity.getParentIds()));
				preUpdateChild(entity, e);
				dao.updateParentIds(e);
			}
		}
		
	}
	
	/**
	 * 预留接口，用户更新子节前调用
	 * @param childEntity
	 */
	protected void preUpdateChild(T entity, T childEntity) {
		
	}

}
