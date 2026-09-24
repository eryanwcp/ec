package com.eryansky.modules.notice.dao;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.MyBatisDao;
import com.eryansky.common.orm.persistence.CrudDao;
import com.eryansky.modules.notice.mapper.MessageSender;

import java.util.List;


/**
 * @author Eryan
 * @date 2016-03-14
 */
@MyBatisDao
public interface MessageSenderDao extends CrudDao<MessageSender> {

    int deleteByMessageId(Parameter parameter);

    List<MessageSender> findByMessageId(Parameter parameter);

}

