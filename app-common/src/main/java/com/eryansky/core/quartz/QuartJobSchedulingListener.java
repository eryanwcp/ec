package com.eryansky.core.quartz;

import com.eryansky.modules.sys.service.JobService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;


import java.util.Map;
import java.util.Set;


public class QuartJobSchedulingListener implements ApplicationListener<ContextRefreshedEvent> {
    Logger logger = LoggerFactory.getLogger(QuartJobSchedulingListener.class);

    private Scheduler scheduler;
    @Autowired
    private JobService jobService;

    public QuartJobSchedulingListener(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public QuartJobSchedulingListener setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            ApplicationContext applicationContext = event.getApplicationContext();
            this.loadCronTriggers(applicationContext, scheduler);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void loadCronTriggers(ApplicationContext applicationContext, Scheduler scheduler) {
        Map<String, Object> quartzJobBeans = applicationContext.getBeansWithAnnotation(QuartzJob.class);
        Set<String> beanNames = quartzJobBeans.keySet();
//        List<CronTrigger> cronTriggerBeans = new ArrayList<CronTrigger>();
        for (String beanName : beanNames) {
            Object object = quartzJobBeans.get(beanName);
            try {
                if (Job.class.isAssignableFrom(object.getClass())) {
                    QuartzJob quartzJobAnnotation = AnnotationUtils.findAnnotation(object.getClass(), QuartzJob.class);
                    JobKey jobKey = new JobKey(quartzJobAnnotation.name(), quartzJobAnnotation.group());
                    JobDetail job = JobBuilder
                            .newJob((Class<? extends Job>) object.getClass())
                            .withIdentity(jobKey)
                            .build();
                    CronTrigger cronTrigger = TriggerBuilder
                            .newTrigger()
                            .withIdentity(object.getClass().getName(), quartzJobAnnotation.group())
                            .withSchedule(CronScheduleBuilder.cronSchedule(quartzJobAnnotation.cronExp()))
                            .build();
                    boolean exist = scheduler.checkExists(jobKey);
                    if(exist){
                        scheduler.deleteJob(jobKey);
                    }
                    if(quartzJobAnnotation.enable()){
                        scheduler.scheduleJob(job, cronTrigger);
                        //指定执行实例
//                        jobService.updateTriggersInstanceName(scheduler.getSchedulerName(),cronTrigger.getKey().getName(),cronTrigger.getKey().getGroup(),QuartzJob.DEFAULT_INSTANCE_NAME.equals(quartzJobAnnotation.instanceName()) ? null:quartzJobAnnotation.instanceName());
                    }else{
                        logger.warn("定时任务未启用：{} {}",object.getClass().getName(),quartzJobAnnotation.cronExp());
                    }
                } else {
                    String errorMsg = object.getClass() + " doesn't implemented " + Job.class.getName();
                    logger.error(errorMsg);
                    throw new RuntimeException(errorMsg);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
