/**
*  Copyright (c) 2012-2022 https://www.eryansky.com
*
*/
package com.eryansky.modules.notice.dao;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.MyBatisDao;
import com.eryansky.common.orm.persistence.CrudDao;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.notice.mapper.ContactGroup;

import java.util.List;

/**
 * 联系人分组
 * @author Eryan
 * @date 2020-03-05
 */
@MyBatisDao
public interface ContactGroupDao extends CrudDao<ContactGroup> {

    Integer getMaxSort();

    List<ContactGroup> checkExist(Parameter parameter);

    List<ContactGroup> findUserContactGroups(Parameter parameter);

    List<User> findContactGroupUsers(Parameter parameter);



    List<String> findObjectIdsByContactGroupId(String contactGroupId);

    int deleteContactGroupObjects(Parameter parameter);

    int insertContactGroupObjects(Parameter parameter);


}
