/**
*  Copyright (c) XXX有限公司 2013-2026 https://github.com/eryanwcp/ec
*
*/
package com.eryansky.modules.sys.dao;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.MyBatisDao;
import com.eryansky.common.orm.persistence.PCrudDao;

import com.eryansky.modules.sys.mapper.UserDevice;

import java.util.List;

/**
 * 用户登录设备
 * @author eryan
 * @date 2026-09-16
 */
@MyBatisDao
public interface UserDeviceDao extends PCrudDao<UserDevice,String> {

    List<UserDevice> findByUserId(Parameter parameter);

}
