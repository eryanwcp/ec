package com.eryansky.configure;

import com.eryansky.core.quartz.QuartJobSchedulingListener;
import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({ QuartzAutoConfiguration.class})
public class QuartzConfigurer {


    @Bean
    public QuartJobSchedulingListener quartJobSchedulingListener(Scheduler scheduler) {
        return new QuartJobSchedulingListener(scheduler);
    }


}