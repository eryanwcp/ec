package com.eryansky.core.quartz.extend;

import com.eryansky.core.quartz.QuartzJob;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * quartz.properties中配置：
 * org.quartz.jobStore.driverDelegateClass= com.eryansky.core.quartz.extend.StdJDBCDelegate
 */

public class StdJDBCDelegate extends org.quartz.impl.jdbcjobstore.StdJDBCDelegate {

    private static final Logger logger = LoggerFactory.getLogger(StdJDBCDelegate.class);

    /**
     * 允许设置trigger 指定一台机器进行任务调度执行；
     *
     * @param conn
     * @param noLaterThan
     * @param noEarlierThan
     * @param maxCount
     * @return
     * @throws SQLException
     */
    @Override
    public List<TriggerKey> selectTriggerToAcquire(Connection conn, long noLaterThan, long noEarlierThan, int maxCount)
            throws SQLException {
        List<TriggerKey> triggerKeys = super.selectTriggerToAcquire(conn, noLaterThan, noEarlierThan, maxCount);
        if (!StdSchedulerFactory.DEFAULT_INSTANCE_ID.equals(instanceId) && null != triggerKeys && triggerKeys.size() > 0) {
            return triggerKeys.stream().filter(k -> {
                Class clazz = null;
                try {
                    clazz = Class.forName(k.getName());
                    QuartzJob quartzJobAnnotation = AnnotationUtils.findAnnotation(clazz, QuartzJob.class);
                    if (null != quartzJobAnnotation && !QuartzJob.AUTO_GENERATE_INSTANCE_ID.equals(quartzJobAnnotation.instanceId()) && !quartzJobAnnotation.instanceId().equals(instanceId)) {
                        logger.debug("跳过本机实例：{} 指定执行实例:{}.{} {}", instanceId, k.getGroup(), k.getName(), quartzJobAnnotation.instanceId());
                        return false;
                    }
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage()+","+k.getName(), e);
                }
                return true;

            }).collect(Collectors.toList());
        }
        return triggerKeys;
    }


}