/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.service;

import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.model.Result;
import com.eryansky.common.model.TreeNode;
import com.eryansky.common.orm._enum.StatusState;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.core.orm.mybatis.service.TreeService;
import com.eryansky.modules.sys._enum.OrganType;
import com.eryansky.modules.sys._enum.SexType;
import com.eryansky.modules.sys.mapper.OrganExtend;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.utils.OrganUtils;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import com.eryansky.utils.CacheConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.eryansky.modules.sys.mapper.Organ;
import com.eryansky.modules.sys.dao.OrganDao;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 机构表 service
 *
 * @author Eryan
 * @date 2018-05-08
 */
@Service
public class OrganService extends TreeService<OrganDao, Organ> {

    public static final String ICON_ORGAN_ROOT = "eu-icon-organ-root";
    public static final String ICON_USER_RED = "eu-icon-user_red";
    public static final String ICON_USER = "eu-icon-user";
    public static final String ICON_GROUP = "eu-icon-group";

    @Autowired
    private UserService userService;


    /**
     * 保存或修改.
     */
    @CacheEvict(value = {CacheConstants.ORGAN_USER_TREE_1_CACHE,CacheConstants.ORGAN_USER_TREE_2_CACHE,CacheConstants.CACHE_OrganDao}, allEntries = true)
    public Organ saveOrgan(Organ entity) {
        logger.debug("清空缓存:{},{}", new Object[]{CacheConstants.ORGAN_USER_TREE_1_CACHE,CacheConstants.ORGAN_USER_TREE_2_CACHE,CacheConstants.CACHE_OrganDao});
        Assert.notNull(entity, "参数[entity]为空!");
        super.save(entity);
        return entity;
    }

    /**
     * 删除(根据主键ID).
     *
     * @param id 主键ID
     */
     @CacheEvict(value = {CacheConstants.ORGAN_USER_TREE_1_CACHE,CacheConstants.ORGAN_USER_TREE_2_CACHE,CacheConstants.CACHE_OrganDao}, allEntries = true)
    public String deleteById(final String id) {
        dao.delete(new Organ(id));
        logger.debug("清空缓存:{},{}", new Object[]{CacheConstants.ORGAN_USER_TREE_1_CACHE,CacheConstants.ORGAN_USER_TREE_2_CACHE,CacheConstants.CACHE_OrganDao});
        return id;
    }

    /**
     * 自定义删除方法.
     */
     @CacheEvict(value = {CacheConstants.ORGAN_USER_TREE_1_CACHE,CacheConstants.ORGAN_USER_TREE_2_CACHE,CacheConstants.CACHE_OrganDao}, allEntries = true)
    public void deleteByIds(Collection<String> ids) {
        for (String id : ids) {
            deleteById(id);
        }
    }

    /**
     * 自定义删除方法.
     */
     @CacheEvict(value = {CacheConstants.ORGAN_USER_TREE_1_CACHE,CacheConstants.ORGAN_USER_TREE_2_CACHE,CacheConstants.CACHE_OrganDao}, allEntries = true)
    public void deleteOwnerAndChilds(String id) {
        dao.deleteOwnerAndChilds(new Organ(id));
        logger.debug("清空缓存:{},{}", new Object[]{CacheConstants.ORGAN_USER_TREE_1_CACHE,CacheConstants.ORGAN_USER_TREE_2_CACHE,CacheConstants.CACHE_OrganDao});
    }

    /**
     * 递归
     *
     * @param organs
     */
    public void updateParentIds(Collection<Organ> organs) {
        for (Organ organ : organs) {
            dao.updateParentIds(organ);
        }
    }

    /**
     * 同步所有机构ID
     */
    public void syncAllParentIds() {
        List<Organ> rootOrgans = this.findRoots();
        updateParentIds(rootOrgans);
    }


    /**
     * 根据编码得到Organ.
     *
     * @param code 机构编码
     * @return
     */
    public Organ getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("code", code);
        return dao.getByCode(parameter);
    }


    /**
     * 根据ID或编码得到
     *
     * @param idOrCode 机构ID或编码
     * @return
     */
    public Organ getByIdOrCode(String idOrCode) {
        return getByIdOrCode(idOrCode,DataEntity.STATUS_NORMAL);
    }

    /**
     * 根据ID或编码得到
     *
     * @param idOrCode 机构ID或编码
     * @param status {@link StatusState}
     * @return
     */
    public Organ getByIdOrCode(String idOrCode, String status) {
        if (StringUtils.isBlank(idOrCode)) {
            return null;
        }
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, status);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("idOrCode", idOrCode);
        return dao.getByIdOrCode(parameter);
    }

    /**
     * 根据编码得到Organ.
     *
     * @param code 机构编码
     * @return
     */
    public Organ getDeleteByIdOrCode(String id, String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_DELETE);
        parameter.put("id", id);
        parameter.put("code", code);
        return dao.getDeleteByIdOrCode(parameter);
    }

    /**
     * 根据系统编码得到Organ.
     *
     * @param sysCode 机构系统编码
     * @return
     */
    public Organ getBySysCode(String sysCode) {
        if (StringUtils.isBlank(sysCode)) {
            return null;
        }
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("sysCode", sysCode);
        return dao.getBySysCode(parameter);
    }


    /**
     * 查找所有
     *
     * @return
     */
    public List<Organ> findAllNormal() {
        return dao.findAllList(new Organ());
    }

    /**
     * 查找所有（包括已删除）
     *
     * @return
     */
    public List<Organ> findAllWithDelete() {
        Organ entity = new Organ();
        entity.setStatus(null);
        return dao.findAllList(entity);
    }

    /**
     * @param organIds 必须包含的机构ID
     * @param query    查询关键字
     * @param types    机构类型
     * @return
     */
    public List<Organ> findWithInclude(Collection<String> organIds, String query, Collection<String> types) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("ids", organIds);
        parameter.put("types", types);
        parameter.put("query", query);
        return dao.findWithInclude(parameter);
    }

    /**
     * @param organIds 必须包含的部门ID
     * @param query
     * @return
     */
    public List<Organ> findDepartmensWithInclude(Collection<String> organIds, String query) {
        List<String> organTypes = Lists.newArrayList(OrganType.department.getValue());
        return findWithInclude(organIds, query, organTypes);
    }

    /**
     * 查找所有单位
     *
     * @return
     */
    public List<Organ> findCompanys() {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("type", OrganType.organ.getValue());
        return dao.findCustomQuery(parameter);
    }

    /**
     * 查找所有单位 树形
     *
     * @return
     */
    public List<TreeNode> findCompanysTree() {
        return findCompanysTree(false);
    }

    /**
     * 查找所有单位 树形
     * @param shortName
     * @return
     */
    public List<TreeNode> findCompanysTree(Boolean shortName) {
        List<Organ> organs = this.findCompanys();
        List<TreeNode> tempTreeNodes = Lists.newArrayList();
        Iterator<Organ> iterator = organs.iterator();
        while (iterator.hasNext()) {
            Organ organ = iterator.next();
            TreeNode treeNode = this.organToTreeNode(organ, false, true,shortName);
            tempTreeNodes.add(treeNode);
        }
        return AppUtils.toTreeTreeNodes(tempTreeNodes);
    }


    /**
     * 机构树
     *
     * @return
     */
    public List<TreeNode> findOrganTree() {
        return findOrganTree(null, null);
    }

    /**
     * 机构树
     *
     * @param parentId 顶级机构 查询所有为null
     * @return
     */
    @Cacheable(value = CacheConstants.ORGAN_USER_TREE_1_CACHE, condition = "#cacheable == true")
    public List<TreeNode> findOrganTree(String parentId, boolean cacheable) {
        return findOrganTree(parentId, null);
    }

    public List<TreeNode> findOrganTree(String parentId, boolean cacheable, boolean cascade) {
        return findOrganUserTree(parentId, null, false, null, cascade,null);
    }


    /**
     * 机构树
     *
     * @param parentId       顶级机构 查询所有为null
     * @param excludeOrganId 排除的机构ID
     * @return
     */
    public List<TreeNode> findOrganTree(String parentId, String excludeOrganId) {
        return findOrganTree(parentId, excludeOrganId, null);
    }

    /**
     * 机构树
     *
     * @param parentId       顶级机构 查询所有为null
     * @param excludeOrganId 排除的机构ID
     * @return
     */
    public List<TreeNode> findOrganTree(String parentId, String excludeOrganId, Boolean shortName) {
        return findOrganUserTree(parentId, excludeOrganId, false, null, true,shortName);
    }

    /**
     * 机构用户树
     *
     * @param parentId 顶级机构 查询所有为null
     * @param addUser  是否在机构下添加用户
     * @return
     */
    public List<TreeNode> findOrganUserTree(String parentId, boolean addUser, boolean cascade) {
        return findOrganUserTree(parentId, null, addUser, null, cascade,null);
    }

    /**
     * 机构用户树
     *
     * @param parentId       顶级机构 查询所有为null
     * @param checkedUserIds 选中的用户
     * @return
     */
    @Cacheable(value = CacheConstants.ORGAN_USER_TREE_2_CACHE)
    public List<TreeNode> findOrganUserTree(String parentId, Collection<String> checkedUserIds, boolean cascade) {
        return findOrganUserTree(parentId, null, true, checkedUserIds, cascade,null);
    }


    /**
     * @param parentId       顶级机构 查询所有为null
     * @param excludeOrganId 排除的机构ID
     * @param addUser        是否在机构下添加用户
     * @param checkedUserIds 选中的用户（addUser为true时 有效）
     * @param cascade        级联
     * @param shortName      机构简称
     * @return
     */
    public List<TreeNode> findOrganUserTree(String parentId, String excludeOrganId, boolean addUser, Collection<String> checkedUserIds, boolean cascade,Boolean shortName) {
        List<Organ> organs = null;
        if (parentId == null) {
            if (cascade) {
                organs = this.findAllNormal();
            } else {
                organs = this.findRoots();
            }
        } else {
            if (cascade) {
                organs = this.findOwnerAndChilds(parentId);
            } else {
                organs = this.findByParent(parentId, StatusState.NORMAL.getValue());
            }

        }

        List<TreeNode> tempTreeNodes = Lists.newArrayList();
        Map<String, TreeNode> tempMap = Maps.newLinkedHashMap();

        Iterator<Organ> iterator = organs.iterator();
        while (iterator.hasNext()) {
            Organ organ = iterator.next();
            if (StringUtils.isNotBlank(excludeOrganId) && organ.getId().equals(excludeOrganId)) {
                continue;
            }
            //排除下级机构
            if (StringUtils.isNotBlank(excludeOrganId) && organ.getParentIds() != null && organ.getParentIds().contains(excludeOrganId)) {
                continue;
            }
            TreeNode treeNode = this.organToTreeNode(organ, addUser,null,shortName);
            if (cascade && addUser) {
                List<User> organUsers = userService.findOrganDefaultUsers(organ.getId());
                for (User organUser : organUsers) {
                    TreeNode userTreeNode = userToTreeNode(organUser);
                    if (Collections3.isNotEmpty(checkedUserIds)) {
                        if (checkedUserIds.contains(userTreeNode.getId())) {
                            userTreeNode.setChecked(true);
                        }
                    }
                    treeNode.addChild(userTreeNode);
                }
            }
            tempTreeNodes.add(treeNode);
            tempMap.put(organ.getId(), treeNode);
        }


        if (addUser) {
            if (parentId != null) {
                Organ parentOrgan = this.get(parentId);
                if (parentOrgan != null) {
                    List<User> parentOrganUsers = userService.findOrganDefaultUsers(parentOrgan.getId());
                    for (User parentOrganUser : parentOrganUsers) {
                        TreeNode parentUserTreeNode = userToTreeNode(parentOrganUser);
                        if (Collections3.isNotEmpty(checkedUserIds)) {
                            if (checkedUserIds.contains(parentUserTreeNode.getId())) {
                                parentUserTreeNode.setChecked(true);
                            }
                        }
                        tempTreeNodes.add(parentUserTreeNode);
                        tempMap.put(parentOrganUser.getId(), parentUserTreeNode);
                    }
                }
            }
        }

        Set<String> keyIds = tempMap.keySet();
        Iterator<String> iteratorKey = keyIds.iterator();
        while (iteratorKey.hasNext()) {
            TreeNode treeNode = tempMap.get(iteratorKey.next());
            if (StringUtils.isNotBlank(treeNode.getpId())) {
                TreeNode pTreeNode = getParentTreeNode(treeNode.getpId(), (String) treeNode.getAttributes().get("nType"), tempTreeNodes);
                if (pTreeNode != null) {
                    pTreeNode.setState(TreeNode.STATE_CLOASED);
                    if (cascade && Collections3.isEmpty(treeNode.getChildren())) {
                        treeNode.setState(TreeNode.STATE_OPEN);
                    }
                    pTreeNode.addChild(treeNode);
                    iteratorKey.remove();
                }
            }

        }
        return tempMap.entrySet().parallelStream().map(v-> {
            if(cascade && Collections3.isEmpty(v.getValue().getChildren())){
                v.getValue().setState(TreeNode.STATE_OPEN);
            }
            return v.getValue();
        }).collect(Collectors.toList());
    }

    /**
     * 机构转TreeNode
     *
     * @param organ 机构
     * @return
     */
    public TreeNode organToTreeNode(Organ organ) {
        return organToTreeNode(organ, null);
    }

    /**
     * 机构转TreeNode
     *
     * @param organ   机构
     * @param addUser 状态栏考虑用户
     * @return
     */
    public TreeNode organToTreeNode(Organ organ, Boolean addUser) {
        return organToTreeNode(organ, addUser, null,false);
    }


    /**
     * 机构转TreeNode
     *
     * @param organ   机构
     * @param addUser 状态栏考虑用户
     * @param onlyCompany 仅单位
     * @param shortName 简称
     * @return
     */
    public TreeNode organToTreeNode(Organ organ, Boolean addUser, Boolean onlyCompany,Boolean shortName) {
        TreeNode treeNode = new TreeNode(organ.getId(), null != shortName && shortName && StringUtils.isNotBlank(organ.getShortName()) ? organ.getShortName():organ.getName());
        if (StringUtils.isBlank(organ.getParentId()) || "0".equals(organ.getParentId())) {
            treeNode.setIconCls(ICON_ORGAN_ROOT);
        } else {
            treeNode.setIconCls(ICON_GROUP);
        }
        if (addUser != null && addUser) {
            Integer childOrganCount = findChildCount(organ.getId());
            Integer childUserCount = userService.findOrganUserCount(organ.getId());
            treeNode.setState(((null != childOrganCount && 0 != childOrganCount) || (null != childUserCount && 0 != childUserCount)) ? TreeNode.STATE_CLOASED : TreeNode.STATE_OPEN);
        } else if (onlyCompany != null && onlyCompany) {
            Collection<String> types = Lists.newArrayList(OrganType.organ.getValue());
            Integer childCompanyCount = findChildCount(organ.getId(),types);
            treeNode.setState((null != childCompanyCount && 0 != childCompanyCount) ? TreeNode.STATE_CLOASED : TreeNode.STATE_OPEN);
        } else {
            treeNode.setState(organ.getState());
        }
        treeNode.setpId(organ.getParentId());
        treeNode.addAttribute("code", organ.getCode());
//        treeNode.addAttribute("sysCode", organ.getSysCode());
        treeNode.addAttribute("type", organ.getType());
        treeNode.addAttribute("nType", "o");//节点类型 机构
        return treeNode;
    }

    /**
     * 用户转TreeNode
     *
     * @param user 用户
     * @return
     */
    public TreeNode userToTreeNode(User user) {
        TreeNode treeNode = new TreeNode(user.getId(), user.getName());
        if (SexType.girl.getValue().equals(user.getSex())) {
            treeNode.setIconCls(ICON_USER_RED);
        } else {
            treeNode.setIconCls(ICON_USER);
        }
        treeNode.addAttribute("nType", "u");//节点类型 用户
        treeNode.addAttribute("loginName", user.getLoginName());//节点类型 用户
        return treeNode;
    }


    /**
     * 用户转TreeNode
     *
     * @param users 用户
     * @return
     */
    public List<TreeNode> userToTreeNode(Collection<User> users) {
        List<TreeNode> treeNodes = Lists.newArrayList();
        for (User user : users) {
            treeNodes.add(userToTreeNode(user));
        }
        return treeNodes;
    }

    /**
     * 查找父级节点
     *
     * @param parentId
     * @param treeNodes
     * @return
     */
    public TreeNode getParentTreeNode(String parentId, Collection<TreeNode> treeNodes) {
        return treeNodes.parallelStream().filter(treeNode-> parentId.equals(treeNode.getId())).findFirst().orElse(null);
    }

    /**
     * 查找父级节点
     *
     * @param parentId
     * @param treeNodes
     * @return
     */
    public TreeNode getParentTreeNode(String parentId, String type, Collection<TreeNode> treeNodes) {
        return treeNodes.parallelStream().filter(treeNode->{
            String _type = (String) treeNode.getAttributes().get("nType");
            return parentId.equals(treeNode.getId()) && _type != null && _type.equals(type);
        }).findFirst().orElse(null);
    }

    /**
     * 查找根节点
     *
     * @return
     */
    public List<Organ> findRoots() {
        return findByParent(null, null);
    }

    /**
     * 根据父ID得到 Organ. <br>
     * 默认按 sort asc排序.
     *
     * @param parentId 父节点ID(当该参数为null的时候查询顶级机构列表)
     * @return
     */
    public List<Organ> findByParent(String parentId) {
        return findByParent(parentId, null);
    }

    /**
     * 根据父ID得到 Organ. <br>
     * 默认按 sort asc排序.
     *
     * @param parentId 父节点ID(当该参数为null的时候查询顶级机构列表)
     * @param status   数据状态 @see com.eryansky.common.orm._enum.StatusState
     *                 <br>status传null则使用默认值 默认值:StatusState.NORMAL.getValue()
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Organ> findByParent(String parentId, String status) {
        //默认值 正常
        if (status == null) {
            status = StatusState.NORMAL.getValue();
        }
        List<String> list = Lists.newArrayList(status);
        return findDataByParent(parentId, list);
    }

    /**
     * 数据列表
     *
     * @param parentId
     * @param status
     * @return
     */
    public List<Organ> findDataByParent(String parentId, Collection<String> status) {
        //默认值 正常
        if (Collections3.isEmpty(status)) {
            status = Lists.newArrayList(StatusState.NORMAL.getValue(), StatusState.LOCK.getValue(), StatusState.AUDIT.getValue());
        }
        return findChild(parentId, status, null);
    }


    public List<Organ> findChild(String parentId) {
        //默认值 正常
        List<String> status = Lists.newArrayList(StatusState.NORMAL.getValue());
        return findChild(parentId, status, null);
    }

    public List<Organ> findChildCompany(String parentId) {
        //默认值 正常
        List<String> status = Lists.newArrayList(StatusState.NORMAL.getValue());
        List<String> types = Lists.newArrayList(OrganType.organ.getValue());
        return findChild(parentId, status, types);
    }


    public List<Organ> findChild(String parentId, Collection<String> types) {
        //默认值 正常
        List<String> list = Lists.newArrayList(StatusState.NORMAL.getValue());
        return findChild(parentId, list, types);
    }


    /**
     * 数据列表
     *
     * @param parentId
     * @param status
     * @param types
     * @return
     */
    public List<Organ> findChild(String parentId, Collection<String> status, Collection<String> types) {
        Parameter parameter = new Parameter();
        parameter.put("parentId", parentId);
        parameter.put(DataEntity.FIELD_STATUS, status);
        parameter.put("types", types);
        return dao.findChild(parameter);
    }

    /**
     * 查找子节点数量
     *
     * @param parentId
     * @return
     */
    public Integer findChildCount(String parentId) {
        return findChildCount(parentId,null);
    }

    /**
     * 查找子节点数量
     *
     * @param parentId
     * @param types
     * @return
     */
    public Integer findChildCount(String parentId, Collection<String> types) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("parentId", parentId);
        parameter.put("types", types);
        return dao.findChildCount(parameter);
    }


    public List<String> findChildIds(String parentId) {
        return findChildIds(parentId, null);
    }

    public List<String> findChildIds(String parentId, Collection<String> status) {
        //默认值 正常
        if (Collections3.isEmpty(status)) {
            status = Lists.newArrayList(StatusState.NORMAL.getValue());
        }
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, status);
        parameter.put("parentId", parentId);
        return dao.findChildIds(parameter);
    }


    /**
     * 得到排序字段的最大值.
     *
     * @return 返回排序字段的最大值
     */
    public Integer getMaxSort() {
        Integer max = dao.getMaxSort();
        return max == null ? 0 : max;
    }

    /**
     * 根据ID查找
     *
     * @param organIds 机构ID集合
     * @return
     */
    public List<Organ> findOrgansByIds(Collection<String> organIds) {
        Parameter parameter = new Parameter();
        parameter.put("organIds", organIds);
        return dao.findOrgansByIds(parameter);
    }


    /**
     * 查找当前部门下所有用户ID
     *
     * @param organId
     * @return
     */
    public List<String> findOrganUserIds(String organId) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("organId", organId);
        return dao.findOrganUserIds(parameter);
    }


    /**
     * 查找用户所属机构
     *
     * @param userId 用户ID
     * @return
     */
    public List<Organ> findOrgansByUserId(String userId) {
        Parameter parameter = new Parameter();
        parameter.put("userId", userId);
        return dao.findOrgansByUserId(parameter);
    }

    /**
     * 查找用户所属机构IDS
     *
     * @param userId 用户ID
     * @return
     */
    public List<String> findOrganIdsByUserId(String userId) {
        Parameter parameter = new Parameter();
        parameter.put("userId", userId);
        return dao.findOrganIdsByUserId(parameter);
    }


    /**
     * 查找岗位关联的机构信息
     *
     * @param postId 岗位ID
     * @return
     */
    public List<Organ> findAssociationOrgansByPostId(String postId) {
        Parameter parameter = new Parameter();
        parameter.put("postId", postId);
        return dao.findAssociationOrgansByPostId(parameter);
    }

    /**
     * 查找岗位关联的机构信息IDS
     *
     * @param postId 岗位ID
     * @return
     */
    public List<String> findAssociationOrganIdsByPostId(String postId) {
        if (StringUtils.isBlank(postId)) {
            return Lists.newArrayList();
        }
        Parameter parameter = new Parameter();
        parameter.put("postId", postId);
        return dao.findAssociationOrganIdsByPostId(parameter);
    }


    /**
     * 查询指定机构以及子机构
     *
     * @param id 机构ID
     * @return
     */
    public List<Organ> findChilds(String id) {
        return findChilds(id, null);
    }

    /**
     * 查询指定机构以及子机构
     *
     * @param id    机构ID
     * @param types 机构类型
     * @return
     */
    public List<Organ> findChilds(String id, Collection<String> types) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("id", id);
        parameter.put("types", types);
        return dao.findChilds(parameter);
    }

    /**
     * 查找下级单位
     *
     * @param id 机构ID
     * @return
     */
    public List<Organ> findChildsCompanys(String id) {
        List<String> types = Lists.newArrayList(OrganType.organ.getValue());
        return findChilds(id, types);
    }

    /**
     * 查找下级直属部门
     *
     * @param id 机构ID
     * @return
     */
    public List<Organ> findChildsDepartments(String id) {
        List<String> types = Lists.newArrayList(OrganType.department.getValue());
        return findChilds(id, types);
    }


    /**
     * 查询指定机构以及子机构
     *
     * @param id 机构ID
     * @return
     */
    public List<String> findChildsIds(String id) {
        return findChildsIds(id, null);
    }

    /**
     * 查询指定机构以及子机构
     *
     * @param id    机构ID
     * @param types 机构类型
     * @return
     */
    public List<String> findChildsIds(String id, Collection<String> types) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("id", id);
        parameter.put("types", types);
        return dao.findChildsIds(parameter);
    }

    /**
     * 查找下级直属单位IDS
     *
     * @param id 机构ID
     * @return
     */
    public List<String> findChildsCompanyIds(String id) {
        List<String> types = Lists.newArrayList(OrganType.organ.getValue());
        return findChildsIds(id, types);
    }

    /**
     * 查找下级直属部门IDS
     *
     * @param id 机构ID
     * @return
     */
    public List<String> findChildsDepartmentIds(String id) {
        List<String> types = Lists.newArrayList(OrganType.department.getValue());
        return findChildsIds(id, types);
    }


    /**
     * 查询指定机构以及子机构
     *
     * @param id 机构ID
     * @return
     */
    public List<Organ> findOwnerAndChilds(String id) {
        return findOwnerAndChilds(id, null);
    }

    /**
     * 查询指定机构以及子机构
     *
     * @param id    机构ID
     * @param types 机构类型
     * @return
     */
    public List<Organ> findOwnerAndChilds(String id, List<String> types) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("id", id);
        parameter.put("types", types);
        return dao.findOwnerAndChilds(parameter);
    }


    /**
     * 查找本机以及下级所有单位
     *
     * @param id 机构ID
     * @return
     */
    public List<Organ> findOwnerAndChildsCompanys(String id) {
        List<String> types = Lists.newArrayList(OrganType.organ.getValue());
        return findOwnerAndChilds(id, types);
    }

    /**
     * 查找本机以及下级所有单位
     *
     * @param id 机构ID
     * @return
     */
    public List<TreeNode> findOwnerAndChildsCompanysTree(String id) {
        return findOwnerAndChildsCompanysTree(id,null);
    }

    /**
     * 查找本机以及下级所有单位
     *
     * @param id 机构ID
     * @return
     */
    public List<TreeNode> findOwnerAndChildsCompanysTree(String id,Boolean shortName) {
        List<Organ> organs = this.findOwnerAndChildsCompanys(id);
        List<TreeNode> tempTreeNodes = Lists.newArrayList();
        Iterator<Organ> iterator = organs.iterator();
        while (iterator.hasNext()) {
            Organ organ = iterator.next();
            TreeNode treeNode = this.organToTreeNode(organ,null,null,shortName);
            tempTreeNodes.add(treeNode);
        }

        return AppUtils.toTreeTreeNodes(tempTreeNodes);
    }

    /**
     * 查找下级所有部门
     *
     * @param id 机构ID
     * @return
     */
    public List<Organ> findOwnerAndChildsDepartments(String id) {
        List<String> types = Lists.newArrayList(OrganType.department.getValue());
        return findOwnerAndChilds(id, types);
    }


    /**
     * 查询指定机构以及子机构
     *
     * @param id 机构ID
     * @return
     */
    public List<String> findOwnerAndChildsIds(String id) {
        return findOwnerAndChildsIds(id, null);
    }

    /**
     * 查询指定机构以及子机构
     *
     * @param id    机构ID
     * @param types 机构类型
     * @return
     */
    public List<String> findOwnerAndChildsIds(String id, Collection<String> types) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("id", id);
        parameter.put("types", types);
        return dao.findOwnerAndChildsIds(parameter);
    }

    /**
     * 查找下级所有单位IDS
     *
     * @param id 机构ID
     * @return
     */
    public List<String> findOwnerAndChildsCompanyIds(String id) {
        List<String> types = Lists.newArrayList(OrganType.organ.getValue());
        return findOwnerAndChildsIds(id, types);
    }

    /**
     * 查找下级所有部门IDS
     *
     * @param id 机构ID
     * @return
     */
    public List<String> findOwnerAndChildsDepartmentIds(String id) {
        List<String> types = Lists.newArrayList(OrganType.department.getValue());
        return findOwnerAndChildsIds(id, types);
    }


    /**
     * 查询本机以及下级机构IDS
     *
     * @param id    机构ID
     * @return
     */
    public List<String> findOwnerAndChildIds(String id) {
        return findOwnerAndChildIds(id,null);
    }

    /**
     * 查询本机以及下级机构IDS
     *
     * @param id    机构ID
     * @param types 机构类型
     * @return
     */
    public List<String> findOwnerAndChildIds(String id, Collection<String> types) {
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("id", id);
        parameter.put("types", types);
        return dao.findOwnerAndChildIds(parameter);
    }

    /**
     * 快速查找方法
     *
     * @param userIds 用户
     * @param shortOrganName 机构简称
     * @param rootId 根ID
     * @returnIDS
     */
    public List<TreeNode> findOrganUserTreeByUserIds(Collection<String> userIds,Boolean shortOrganName,String rootId) throws ServiceException {
        List<User> users = userService.findUsersByIds(userIds);
        return userService.toTreeNodeByUsersAndRootOrganId(users, shortOrganName,rootId);
    }



    /*机构扩展表*/

    /**
     * 根据机构ID查找
     *
     * @param organId
     * @return
     */
    public OrganExtend getOrganExtend(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("id", organId);
        return dao.getOrganExtend(parameter);
    }

    /**
     * 根据机构编码查找
     *
     * @param organCode 机构编码
     * @return
     */
    public OrganExtend getOrganExtendByCode(String organCode) {
        if (StringUtils.isBlank(organCode)) {
            return null;
        }
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("code", organCode);
        return dao.getOrganExtendByCode(parameter);
    }

    /**
     * 根据机构ID查找
     *
     * @param organId
     * @return
     */
    public OrganExtend getOrganCompany(String organId) {
        if (StringUtils.isBlank(organId)) {
            return null;
        }
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("id", organId);
        return dao.getOrganCompany(parameter);
    }

    /**
     * 根据用户ID查找
     *
     * @param userId
     * @return
     */
    public OrganExtend getOrganExtendByUserId(String userId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("userId", userId);
        return dao.getOrganExtendByUserId(parameter);
    }

    /**
     * 根据用户账号查找
     *
     * @param loginName
     * @return
     */
    public OrganExtend getOrganExtendByUserLoginName(String loginName) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("loginName", loginName);
        return dao.getOrganExtendByUserLoginName(parameter);
    }


    /**
     * 根据用户ID查找
     *
     * @param userId
     * @return
     */
    public OrganExtend getCompanyByUserId(String userId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("userId", userId);
        return dao.getCompanyByUserId(parameter);
    }


    /**
     * 根据用户ID查找
     *
     * @param userId
     * @return
     */
    public OrganExtend getHomeCompanyByUserId(String userId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("userId", userId);
        return dao.getHomeCompanyByUserId(parameter);
    }

    /**
     * 查找所有单位
     *
     * @return
     */
    public List<OrganExtend> findCompanyOrganExtends() {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        List<String> types = Lists.newArrayList(OrganType.organ.getValue());
        parameter.put("types", types);
        return dao.findOrganExtends(parameter);
    }


    /**
     * 查找机构下直属部门
     *
     * @param organId
     * @return
     */
    public List<OrganExtend> findDepartmentOrganExtendsByCompanyId(String organId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("organId", organId);
        return dao.findDepartmentOrganExtendsByCompanyId(parameter);
    }


    /**
     * 查找机构下直属部门ID
     *
     * @param organId
     * @return
     */
    public List<String> findDepartmentOrganIdsByCompanyId(String organId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("organId", organId);
        return dao.findDepartmentOrganIdsByCompanyId(parameter);
    }


    /**
     * 查找机构下直属部门以及小组
     *
     * @param organId
     * @return
     */
    public List<OrganExtend> findDepartmentAndGroupOrganExtendsByCompanyId(String organId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("organId", organId);
        return dao.findDepartmentAndGroupOrganExtendsByCompanyId(parameter);
    }


    /**
     * 查找机构下直属部门以及小组IDS
     *
     * @param organId
     * @return
     */
    public List<String> findDepartmentAndGroupOrganIdsByCompanyId(String organId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put("organId", organId);
        return dao.findDepartmentAndGroupOrganIdsByCompanyId(parameter);
    }

    /*机构扩展表*/


    /**
     * 自定义SQL查询
     *
     * @param whereSQL
     * @return
     */
    public List<Organ> findByWhereSQL(String whereSQL) {
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
    public List<Organ> findBySql(String sql) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("sql", sql);
        return dao.findBySql(parameter);
    }

    /**
     * 根据IDS查询
     *
     * @param ids
     * @return
     */
    public List<Organ> findByIds(List<String> ids) {
        if(Collections3.isEmpty(ids)){
            return Collections.emptyList();
        }
        Parameter parameter = Parameter.newParameter();
        parameter.put("ids", ids);
        return dao.findByIds(parameter);
    }

    /**
     * 根据员工编号查询
     *
     * @param codes
     * @return
     */
    public List<Organ> findByCodes(List<String> codes) {
        if(Collections3.isEmpty(codes)){
            return Collections.emptyList();
        }
        Parameter parameter = Parameter.newParameter();
        parameter.put("codes", codes);
        return dao.findByCodes(parameter);
    }

    /**
     * 根据员工编号查询
     *
     * @param sysCodes
     * @return
     */
    public List<Organ> findBySysCodes(List<String> sysCodes) {
        if(Collections3.isEmpty(sysCodes)){
            return Collections.emptyList();
        }
        Parameter parameter = Parameter.newParameter();
        parameter.put("sysCodes", sysCodes);
        return dao.findBySysCodes(parameter);
    }
}
