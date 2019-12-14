package com.pavikumbhar.batchprocessing.quartz.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.pavikumbhar.batchprocessing.quartz.job.DatabaseToDatabaseShchedulerJob;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Pravin Kumbhar
 *
 */

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "batch.database.to.database", name = "enabled", matchIfMissing = false)
public class DatabaseToDatabaseQuartzScheduler extends AbstractQuartzScheduler {
    
    private static final String DATABASE_TO_DATABASE_TRIGGER = "DATABASE_TO_DATABASE_TRIGGER";
    private static final String DATABASE_TO_DATABASE_JOB = "DATABASE_TO_DATABASE_JOB";

    @Value("${batch.database.to.database.cron.expression}")
    private String cronExpression;
    
    @PostConstruct
    public void init() {
        log.info("Loading {} Scheduler...", DatabaseToDatabaseQuartzScheduler.class.getSimpleName());
    }

    @Bean
    public SchedulerFactoryBean databaseToDatabaseScheduler() {
        return createScheduler(DatabaseToDatabaseQuartzScheduler.class.getSimpleName(), databaseToDatabaseCronTrigger().getObject());
    }

    @Bean
    public JobDetailFactoryBean databaseToDatabaseQuartzJob() {
        return createJobDetail(DATABASE_TO_DATABASE_JOB, DatabaseToDatabaseShchedulerJob.class);
    }

    @Bean
    public CronTriggerFactoryBean databaseToDatabaseCronTrigger() {
        return createCronTrigger(databaseToDatabaseQuartzJob().getObject(), DATABASE_TO_DATABASE_TRIGGER, cronExpression);
    }

}
