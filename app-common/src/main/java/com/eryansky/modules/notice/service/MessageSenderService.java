/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.service;

import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.core.orm.mybatis.entity.DataEntity;
import com.eryansky.core.orm.mybatis.service.CrudService;
import com.eryansky.modules.notice.dao.MessageSenderDao;
import com.eryansky.modules.notice.mapper.MessageSender;
import com.eryansky.utils.AppConstants;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Eryan
 * @date 2016-03-14
 */
@Service
public class MessageSenderService extends CrudService<MessageSenderDao, MessageSender> {


    /**
     * 根据消息ID删除
     *
     * @param messageId
     * @return
     */
    public int deleteByMessageId(String messageId) {
        Parameter parameter = Parameter.newParameter();
        parameter.put("messageId", messageId);
        return dao.deleteByMessageId(parameter);
    }

    /**
     * 根据消息ID查找
     *
     * @param messageId
     * @return
     */
    public List<MessageSender> findByMessageId(String messageId){
        Parameter parameter = new Parameter();
        parameter.put(DataEntity.FIELD_STATUS,DataEntity.STATUS_NORMAL);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put("messageId",messageId);
        return dao.findByMessageId(parameter);
    }

    @Override
    public Page<MessageSender> findPage(Page<MessageSender> page, MessageSender entity) {
        entity.setEntityPage(page);
        page.setResult(dao.findList(entity));
        return page;
    }
}
