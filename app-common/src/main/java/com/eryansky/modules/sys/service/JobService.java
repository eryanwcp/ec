package com.eryansky.modules.sys.service;

import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.core.orm.mybatis.service.BaseService;
import com.eryansky.core.quartz.QuartzJob;
import com.eryansky.modules.sys.dao.JobDao;
import com.eryansky.modules.sys.mapper.QuartzJobDetail;
import com.eryansky.utils.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

@Service
public class JobService extends BaseService {

    @Autowired
    private JobDao dao;

    public Page<QuartzJobDetail> findJobList(Page<QuartzJobDetail> page, String jobName, String jobState) {
        Parameter parameter = Parameter.newPageParameter(page, AppConstants.getJdbcType());
        parameter.put("jobName", jobName);
        parameter.put("jobState", jobState);
        page.setResult(dao.findJobList(parameter));
        page.getResult().forEach(v->{
            QuartzJob quartzJob = getAnnotationByTriggerName(v.getTriggerName());
            v.setInstanceName(null != quartzJob ? quartzJob.instanceId():null);
        });
        return page;
    }

    public QuartzJob getAnnotationByTriggerName(String name) {
        Class clazz = null;
        try {
            clazz = Class.forName(name);
            QuartzJob quartzJobAnnotation = AnnotationUtils.findAnnotation(clazz, QuartzJob.class);
            return quartzJobAnnotation;
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}