/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.notice.dao;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.MyBatisDao;
import com.eryansky.common.orm.persistence.CrudDao;
import com.eryansky.modules.notice.mapper.Message;

import java.util.List;


/**
 * 消息DAO接口
 *
 * @author Eryan
 * @date 2016-03-14
 */
@MyBatisDao
public interface MessageDao extends CrudDao<Message> {

    List<Message> findQueryList(Parameter parameter);
}

