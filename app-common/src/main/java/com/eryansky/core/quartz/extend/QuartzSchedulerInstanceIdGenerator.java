package com.eryansky.core.quartz.extend;

import com.eryansky.common.utils.net.IpUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleInstanceIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetAddress;

/**
 *
 * Quartz id实例生成器扩展
 * quartz.properties中配置：org.quartz.scheduler.instanceIdGenerator.class=com.eryansky.core.quartz.extend.QuartzSchedulerInstanceIdGenerator
 * 默认生成器 {@link SimpleInstanceIdGenerator}
 */
public class QuartzSchedulerInstanceIdGenerator extends SimpleInstanceIdGenerator {

    private static final Logger logger = LoggerFactory.getLogger(QuartzSchedulerInstanceIdGenerator.class);

    private static final String OS_NAME = "os.name";

    private static final String WINDOWS = "Windows";

    private static final String MAC = "Mac OS";

    @Override
    public String generateInstanceId() throws SchedulerException {
        String id;
        try {
            if (isLocalDev()) {
                id = getHostName();
            } else {
                id = IpUtils.getActivityLocalIp();
            }

            if (StringUtils.isBlank(id)) {
                id = super.generateInstanceId();
            }
        } catch (Exception e) {
            throw new SchedulerException("Couldn't generate instance id!", e);
        }
        return id;
    }

    private String getHostName() throws SchedulerException {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            throw new SchedulerException("Couldn't get host name!", e);
        }
    }

    private boolean isLocalDev() {
        if (StringUtils.indexOfIgnoreCase(System.getProperty(OS_NAME), WINDOWS) >= 0) {
            return true;
        }

        if (StringUtils.indexOfIgnoreCase(System.getProperty(OS_NAME), MAC) >= 0) {
            return true;
        }

        return false;
    }
}