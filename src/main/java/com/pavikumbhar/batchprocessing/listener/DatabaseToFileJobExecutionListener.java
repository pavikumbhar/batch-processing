package com.pavikumbhar.batchprocessing.listener;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
    
    private LocalDateTime startTime;
    private String jobName;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = LocalDateTime.now();
        jobName = jobExecution.getJobInstance().getJobName();
        log.debug("[ {} ] Job starts at : {} ", jobName, startTime);
        long jobId = jobExecution.getJobId();
        
        jobExecution.getExecutionContext().put("jobId", jobId);
    }
    
    @Override
    public void afterJob(JobExecution jobExecution) {
    	LocalDateTime stopTime = LocalDateTime.now();
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
    
    private long getTimeInMillis(LocalDateTime start, LocalDateTime stop) {
        return ChronoUnit.MILLIS.between(start, stop);

    }

}
