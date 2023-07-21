package com.pavikumbhar.batchprocessing.quartz.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author pavikumbhar
 *
 */

@Slf4j
@DisallowConcurrentExecution
public class DatabaseToFileShchedulerJob extends AbstractScheduledJob {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {

            JobParameters jobParameters = new JobParametersBuilder()//
                    .addLong("time", System.currentTimeMillis())//
                    .addString("pathToFile", "D:/temp/filesystemdata" + System.currentTimeMillis() + ".csv")//
                    .toJobParameters();
            Job job = getJobLocator().getJob("databaseToFileJob");
            JobExecution jobExecution = getJobLauncher().run(job, jobParameters);
            log.info("{}_{} was completed successfully", job.getName(), jobExecution.getId());
        } catch (Exception e) {
            log.error("Encountered job execution exception! {}", e);
        }
        
    }
}