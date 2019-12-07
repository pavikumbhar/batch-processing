package com.pravinkumbhar.batchprocessing.listener;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@JobScope
@Component
public class DatabaseToFileJobExecutionListener implements JobExecutionListener {
    
    private DateTime startTime;
    private String jobName;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = new DateTime();
        jobName = jobExecution.getJobInstance().getJobName();
        log.debug("[ {} ] Job starts at : {} ", jobName, startTime);
        long jobId = jobExecution.getJobId();
        
        jobExecution.getExecutionContext().put("jobId", jobId);
    }
    
    @Override
    public void afterJob(JobExecution jobExecution) {
        DateTime stopTime = new DateTime();
        log.debug("[ {} ]Job stops at {}:", jobName, stopTime);
        log.debug("Total time take in millis {}:", getTimeInMillis(startTime, stopTime));
        
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.debug("[ {} ] job completed successfully", jobName);
            //Here you can perform some other business logic like cleanup
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.debug("[ {} ] job failed with following exceptions ", jobName);
            List<Throwable> exceptionList = jobExecution.getAllFailureExceptions();
            for (Throwable th : exceptionList) {
                log.error("exception : {}", th.getLocalizedMessage());
            }
        }
    }
    
    private long getTimeInMillis(DateTime start, DateTime stop) {
        return stop.getMillis() - start.getMillis();
    }

}
