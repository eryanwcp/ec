/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.service;

import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.utils.AppConstants;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eryansky.modules.sys.mapper.Post;
import com.eryansky.modules.sys.dao.PostDao;
import com.eryansky.core.orm.mybatis.service.CrudService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 岗位表 service
 *
 * @author Eryan
 * @date 2018-05-08
 */
@Service
public class PostService extends CrudService<PostDao, Post> {

    /**
     * 根据ID删除
     *
     * @param id
     */
    public void deleteById(String id) {
        delete(new Post(id));
    }

    /**
     * 根据IDS删除
     *
     * @param ids
     */
    public void deleteByIds(List<String> ids) {
        if (Collections3.isNotEmpty(ids)) {
            for (String id : ids) {
                deleteById(id);
            }
        } else {
            logger.warn("参数[ids]为空.");
        }
    }


    public void savePost(Post entity) {
        super.save(entity);
        savePostOrgans(entity.getId(), entity.getOrganIds());
    }

    public Page<Post> findPage(Page<Post> p, Post entity) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put(BaseInterceptor.PAGE, p);
        parameter.put("organId", entity.getOrganId());
        parameter.put("query", entity.getQuery());
        return p.setResult(dao.findQuery(parameter));
    }


    /**
     * 根据编码查找
     *
     * @param postCode
     * @return
     */
    public Post getByCode(String postCode) {
        Validate.notNull(postCode, "参数[postCode]不能为null或空");
        Parameter parameter = new Parameter();
        parameter.put("code", postCode);
        List<Post> list = dao.findPost(parameter);
        if (Collections3.isNotEmpty(list) && list.size() > 1) {
            logger.warn("岗位编码为[" + postCode + "]包含" + list.size() + "条数据");
        }
        return list.get(0);
    }

    /**
     * 根据机构ID以及岗位编码查找
     *
     * @param organId  机构ID
     * @param postCode 岗位编码
     */
    public Post getPostByOC(String organId, String postCode) {
        Validate.notNull(organId, "参数[organId]不能为null");
        Validate.notNull(postCode, "参数[postCode]不能为null或空");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("code", postCode);
        parameter.put("organId", organId);
        List<Post> list = dao.findPost(parameter);
        if (Collections3.isNotEmpty(list) && list.size() > 1) {
            logger.warn("岗位编码为[" + postCode + "]包含" + list.size() + "条数据");
        }
        return Collections3.isNotEmpty(list) ? list.get(0) : null;
    }

    /**
     * 机构岗位
     *
     * @param organId
     * @return
     */
    public List<Post> findPostsByOrganId(String organId) {
        Validate.notNull(organId, "参数[organId]不能为null");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("organId", organId);
        return dao.findPostsByOrganId(parameter);
    }


    /**
     * 机构岗位
     *
     * @param organIds
     * @return
     */
    public List<Post> findPostsByOrganIds(Collection<String> organIds) {
        Validate.notEmpty(organIds, "参数[organIds]不能为null");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("organIds", organIds);
        return dao.findPostsByOrganIds(parameter);
    }

    /**
     * 用户岗位
     *
     * @param userId
     * @return
     */
    public List<Post> findPostsByUserId(String userId) {
        return findPostsByUserId(userId,null);
    }

    /**
     * 用户岗位
     *
     * @param userId
     * @param organId
     * @return
     */
    public List<Post> findPostsByUserId(String userId,String organId) {
        Validate.notNull(userId, "参数[userId]不能为null");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("userId", userId);
        parameter.put("organId", organId);
        return dao.findPostsByUserId(parameter);
    }


    /**
     * 用户岗位IDS
     *
     * @param userId
     * @return
     */
    public List<String> findPostIdsByUserId(String userId) {
        return findPostIdsByUserId(userId,null);
    }

    /**
     * 用户岗位IDS
     *
     * @param userId
     * @param organId
     * @return
     */
    public List<String> findPostIdsByUserId(String userId,String organId) {
        Validate.notNull(userId, "参数[userId]不能为null");
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("userId", userId);
        parameter.put("organId", organId);
        return dao.findPostIdsByUserId(parameter);
    }


    /**
     * 自定义SQL查询
     *
     * @param whereSQL
     * @return
     */
    public List<Post> findByWhereSQL(String whereSQL) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("whereSQL", whereSQL);
        return dao.findByWhereSQL(parameter);
    }

    /**
     * 自定义SQL查询
     *
     * @param sql
     * @return
     */
    public List<Post> findBySql(String sql) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("sql", sql);
        return dao.findBySql(parameter);
    }

    /**
     * 保存岗位机构关联信息
     * 保存之前先删除原有
     *
     * @param id  岗位ID
     * @param ids 机构IDS
     */
    public void savePostOrgans(String id, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("ids", ids);
        dao.deletePostOrgansByPostId(parameter);
        if (Collections3.isNotEmpty(ids)) {
            dao.insertPostOrgans(parameter);
        }
    }


    /**
     * 保存岗位用户关联信息
     * 保存之前先删除原有
     *
     * @param id      岗位ID
     * @param organId 机构ID
     * @param ids     用户IDS
     */
    public void savePostOrganUsers(String id, String organId, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("organId", organId);
        parameter.put("ids", ids);
        dao.deletePostUsersByPostIdAndOrganId(parameter);
        if (Collections3.isNotEmpty(ids)) {
            dao.insertPostUsers(parameter);
        }
    }

    /**
     * 保存岗位用户关联信息
     * 保存之前先删除原有
     *
     * @param id      岗位ID
     * @param organId 机构ID
     * @param ids     用户IDS
     */
    public void addPostOrganUsers(String id, String organId, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("organId", organId);
        parameter.put("ids", ids);
        dao.deletePostUsersByPostIdAndOrganIdAndUserIds(parameter);
        if (Collections3.isNotEmpty(ids)) {
            dao.insertPostUsers(parameter);
        }
    }

    /**
     * 删除岗位用户关联信息
     *
     * @param id      岗位ID
     * @param organId 机构ID
     * @param ids     用户IDS
     */
    public int insertPostUsers(String id, String organId, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("organId", organId);
        parameter.put("ids", ids);
        return  dao.insertPostUsers(parameter);
    }


    /**
     * 删除岗位用户关联信息
     *
     * @param id      岗位ID
     * @param organId 机构ID
     * @param ids     用户IDS
     */
    public int deletePostUsersByPostIdAndOrganId(String id, String organId, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("organId", organId);
        parameter.put("ids", ids);
        return  dao.deletePostUsersByPostIdAndOrganId(parameter);
    }


    /**
     * 删除岗位用户关联信息
     *
     * @param id      岗位ID
     * @param userId  用户ID
     */
    public int deletePostUsersByPostIdAndUserId(String id, String userId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("userId", userId);
        return  dao.deletePostUsersByPostIdAndUserId(parameter);
    }


    /**
     * 删除指定岗位-机构-用户关联信息
     *
     * @param id  角色ID
     * @param organId 机构ID
     * @param ids 用户IDS
     */
    public void deletePostUsersByPostIdAndOrganIdAndUserIds(String id,String organId, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("organId", organId);
        parameter.put("ids", ids);
        if (Collections3.isNotEmpty(ids)) {
            dao.deletePostUsersByPostIdAndOrganIdAndUserIds(parameter);
        }
    }

}
