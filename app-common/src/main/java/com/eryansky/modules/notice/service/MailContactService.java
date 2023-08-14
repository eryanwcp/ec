/**
*  Copyright (c) 2012-2022 https://www.eryansky.com
*
*/
package com.eryansky.modules.notice.service;

import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.core.orm.mybatis.service.CrudService;
import com.eryansky.utils.AppConstants;
import com.eryansky.modules.notice.dao.MailContactDao;
import com.eryansky.modules.notice.mapper.MailContact;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 邮件联系人 service
 * @author Eryan
 * @date 2020-03-05
 */
@Service
public class MailContactService extends CrudService<MailContactDao, MailContact> {

    public Page<MailContact> findPageByContactGroupId(Page<MailContact> page, String contactGroupId, String query) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put(BaseInterceptor.PAGE, page);
        parameter.put("contactGroupId",contactGroupId);
        parameter.put("query",query);
        return page.setResult(dao.findByContactGroupId(parameter));
    }

    public MailContact checkExist(String contactGroupId, String email) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("contactGroupId",contactGroupId);
        parameter.put("email",email);
        List<MailContact> list = dao.checkExist(parameter);
        return list.isEmpty() ? null:list.get(0);
    }

    public MailContact checkUserMailContactExist(String userId, String email) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("userId",userId);
        parameter.put("email",email);
        List<MailContact> list = dao.checkExist(parameter);
        return list.isEmpty() ? null:list.get(0);
    }

    public List<MailContact> findUserMailContactsWithInclude(String userId, Collection<String> includeIds, String query) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("userId",userId);
        parameter.put("ids",includeIds);
        parameter.put("query",query);
        return dao.findByUserIdWithInclude(parameter);
    }

    public List<MailContact> findByUserId(String userId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("userId",userId);
        return dao.findByUserId(parameter);
    }

    public List<MailContact> findByContactGroupId(String contactGroupId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put(DataEntity.FIELD_STATUS, DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("contactGroupId",contactGroupId);
        return dao.findByContactGroupId(parameter);
    }
}
