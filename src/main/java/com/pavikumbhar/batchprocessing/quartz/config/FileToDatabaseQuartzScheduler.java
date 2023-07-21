package com.pavikumbhar.batchprocessing.quartz.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.pavikumbhar.batchprocessing.quartz.job.FileToDatabaseShchedulerJob;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author pavikumbhar
 *
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "batch.file.to.database", name = "enabled", matchIfMissing = false)
public class FileToDatabaseQuartzScheduler extends AbstractQuartzScheduler {

    private static final String FILE_TO_DATABASE_TRIGGER = "FILE_TO_DATABASE_TRIGGER";
    private static final String FILE_TO_DATABASE_JOB = "FILE_TO_DATABASE_JOB";
    
    @Value("${batch.file.to.database.cron.expression}")
    private String cronExpression;

    @PostConstruct
    public void init() {
        log.info("Loading {} Scheduler...", FileToDatabaseQuartzScheduler.class.getSimpleName());
    }

    @Bean
    public SchedulerFactoryBean fileToDatabaseScheduler() {
        return createScheduler(FileToDatabaseQuartzScheduler.class.getSimpleName(), fileToDatabaseCronTrigger().getObject());
    }

    @Bean
    public JobDetailFactoryBean fileToDatabaseQuartzJob() {
        return createJobDetail(FILE_TO_DATABASE_JOB, FileToDatabaseShchedulerJob.class);
    }

    @Bean
    public CronTriggerFactoryBean fileToDatabaseCronTrigger() {
        return createCronTrigger(fileToDatabaseQuartzJob().getObject(), FILE_TO_DATABASE_TRIGGER, cronExpression);
    }

}
