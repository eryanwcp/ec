/**
*  Copyright (c) 2012-2022 https://www.eryansky.com
*
*/
package com.eryansky.modules.notice.service;

import com.eryansky.common.exception.DaoException;
import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.exception.SystemException;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.core.orm.mybatis.service.CrudService;
import com.eryansky.modules.notice.mapper.MailContact;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.utils.AppConstants;
import com.eryansky.modules.notice._enum.ContactGroupType;
import com.eryansky.modules.notice.dao.ContactGroupDao;
import com.eryansky.modules.notice.mapper.ContactGroup;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 联系人分组 service
 * @author Eryan
 * @date 2020-03-05
 */
@Service
public class ContactGroupService extends CrudService<ContactGroupDao, ContactGroup> {

    public void deleteByIds(List<String> ids) throws DaoException, SystemException, ServiceException {
        if (!Collections3.isEmpty(ids)) {
            for (String id : ids) {
                delete(new ContactGroup(id));
//                deleteContactGroupObjects(id,null);//删除关联关系
            }
        } else {
            logger.warn("参数[ids]为空.");
        }
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

    public List<ContactGroup> findUserContactGroupsWithInclude(String userId, String query) {
        if(StringUtils.isBlank(query)){
            return Collections.emptyList();
        }
        return findUserContactGroups(userId,null,query);
    }

    public ContactGroup saveDefaultMailContactGroupIfNotExist(String userId) {
        return checkUserDefaultContractGroup(userId,ContactGroupType.Mail.getValue());
    }

    /**
     *
     * @param userId
     * @param contactGroupType {@link com.eryansky.modules.notice._enum.ContactGroupType}
     * @return
     * @throws DaoException
     * @throws SystemException
     * @throws ServiceException
     */
    public ContactGroup checkUserDefaultContractGroup(String userId,String contactGroupType) throws DaoException, SystemException, ServiceException {
        Validate.notBlank(userId, "参数[userId]不能为空或null.");
        Validate.notNull(contactGroupType, "参数[contactGroupType]不能为null.");
        List<ContactGroup> list = findUserContactGroups(userId,contactGroupType,null);
        ContactGroup contactGroup = list.stream().filter(ContactGroup::getIsDefault).findFirst().orElse(null);
        if(null == contactGroup){
            contactGroup = new ContactGroup();
            contactGroup.setContactGroupType(contactGroupType);
            contactGroup.setUserId(userId);
            contactGroup.setIsDefault(Boolean.TRUE);
            contactGroup.setName("默认组");
            this.save(contactGroup);
        }
        return contactGroup;
    }


    public ContactGroup checkExist(String userId, String contactGroupType, String name, String id) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("id", id);
        parameter.put("userId", userId);
        parameter.put("contactGroupType", contactGroupType);
        parameter.put("name", name);
        List<ContactGroup> list = dao.checkExist(parameter);
        return list.isEmpty() ? null : list.get(0);
    }

    public Page<User> findContactGroupUsers(Page<User> page, String contactGroupId, String query) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put("contactGroupId",contactGroupId);
        parameter.put("query",query);
        return page.setResult(dao.findContactGroupUsers(parameter));
    }

    public List<User> findContactGroupUsers(String contactGroupId, String query) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("contactGroupId",contactGroupId);
        parameter.put("query",query);
        return dao.findContactGroupUsers(parameter);
    }

    public List<User> findContactGroupUsers(String contactGroupId) {
        return findContactGroupUsers(contactGroupId,null);
    }

    public List<ContactGroup> findUserContactGroups(String userId, String contactGroupType) {
        return findUserContactGroups(userId,contactGroupType,null);
    }

    /**
     * 查找用户分组信息
     * @param userId 用户ID
     * @param contactGroupType {@link ContactGroupType}
     * @param query 查询关键字
     * @return
     * @throws DaoException
     * @throws SystemException
     * @throws ServiceException
     */
    public List<ContactGroup> findUserContactGroups(String userId,String contactGroupType,String query) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("userId",userId);
        parameter.put("contactGroupType",contactGroupType);
        parameter.put("query",query);
        return dao.findUserContactGroups(parameter);
    }

    public List<MailContact> findContactGroupMailContacts(String contactGroupId) {
        return null;
    }


    public void saveContactGroup(ContactGroup entity) {
        super.save(entity);
    }

    /**
     * 插入关联信息
     *
     * @param id  主ID
     * @param ids 附IDS
     */
    public void insertContactGroupObjects(String id, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("ids", ids);
        if (Collections3.isNotEmpty(ids)) {
            dao.insertContactGroupObjects(parameter);
        }
    }

    /**
     * 刪除关联信息
     *
     * @param id  主ID
     * @param ids 附IDS
     */
    public void deleteContactGroupObjects(String id, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("ids", ids);
        dao.deleteContactGroupObjects(parameter);
    }


    /**
     * 保存关联信息
     * 保存之前先删除原有
     *
     * @param id  主ID
     * @param ids 附IDS
     */
    public void saveContactGroupObjects(String id, Collection<String> ids) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("id", id);
        parameter.put("ids", ids);
        dao.deleteContactGroupObjects(parameter);
        if (Collections3.isNotEmpty(ids)) {
            dao.insertContactGroupObjects(parameter);
        }
    }

    /**
     * 查找关联IDS
     *
     * @param contactGroupId
     * @return
     */
    public List<String> findObjectIdsByContactGroupId(String contactGroupId) {
        return dao.findObjectIdsByContactGroupId(contactGroupId);
    }

}
