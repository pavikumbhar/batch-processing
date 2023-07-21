package com.pavikumbhar.batchprocessing.quartz.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.pavikumbhar.batchprocessing.quartz.job.DatabaseToFileShchedulerJob;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author pavikumbhar
 *
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "batch.database.to.file", name = "enabled", matchIfMissing = false)
public class DatabaseToFileQuartzScheduler extends AbstractQuartzScheduler {
    
    private static final String DATABASE_TO_FILE_TRIGGER = "DATABASE_TO_FILE_TRIGGER";
    private static final String DATABASE_TO_FILE_JOB = "DATABASE_TO_FILE_JOB";

    @Value("${batch.database.to.file.cron.expression}")
    private String cronExpression;

    @PostConstruct
    public void init() {
        log.info("Loading {} Scheduler...", DatabaseToFileQuartzScheduler.class.getSimpleName());
    }
    
    @Bean
    public SchedulerFactoryBean databaseToFileScheduler() {
        return createScheduler(DatabaseToFileShchedulerJob.class.getSimpleName(), databaseToFileCronTrigger().getObject());
    }
    
    @Bean
    public JobDetailFactoryBean databaseToFileQuartzJob() {
        return createJobDetail(DATABASE_TO_FILE_JOB, DatabaseToFileShchedulerJob.class);
    }
    
    @Bean
    public CronTriggerFactoryBean databaseToFileCronTrigger() {
        return createCronTrigger(databaseToFileQuartzJob().getObject(), DATABASE_TO_FILE_TRIGGER, cronExpression);
    }
    
}
