/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.dao;

import com.eryansky.common.orm.mybatis.MyBatisDao;
import com.eryansky.common.orm.persistence.CrudDao;
import com.eryansky.modules.sys.mapper.SystemSerialNumber;
import org.apache.ibatis.annotations.Param;


/**
 * @author Eryan
 * @date 2016-07-14
 */
@MyBatisDao
public interface SystemSerialNumberDao extends CrudDao<SystemSerialNumber> {

    /**
     * 乐观锁更新方式
     *
     * @param entity
     * @return 返回更新数 0：更新失败 1：更新成功
     */
    int updateByVersion(SystemSerialNumber entity);

    /**
     * 重置序列号
     * @param entity
     * @return
     */
    int updateSerialNumber(SystemSerialNumber entity);

    SystemSerialNumber getByCode(SystemSerialNumber entity);
}

