package com.eryansky.modules.sys.dao;

import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.MyBatisDao;
import com.eryansky.common.orm.persistence.BaseDao;
import com.eryansky.modules.sys.mapper.QuartzJobDetail;
import java.util.List;

@MyBatisDao
public interface JobDao extends BaseDao {

	List<QuartzJobDetail> findJobList(Parameter parameter);
}
