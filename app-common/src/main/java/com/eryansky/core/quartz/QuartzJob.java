package com.eryansky.core.quartz;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
//@Scope("prototype")
public @interface QuartzJob {

    String AUTO_GENERATE_INSTANCE_ID = "AUTO";
	/**
	 * 是否启用
	 * @return
	 */
    boolean enable() default true;

    /**
     * 执行实例名称（仅集群模式下有效org.quartz.jobStore.isClustered = true），默认为在所有节点随机执行
     * 对应参数org.quartz.scheduler.instanceId 指定名称或根据系统自动生成（AUTO） linux为IP地址；windows、macos为机器名
     * @return
     */
    String instanceId() default AUTO_GENERATE_INSTANCE_ID;

    /**
     * 任务名称
     * @return
     */
    String name();

    /**
     * 任务分组
     * @return
     */
    String group() default "DEFAULT_GROUP";

    /**
     * cron表达式
     * @return
     */
    String cronExp();
}
