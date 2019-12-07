package com.pravinkumbhar.batchprocessing.listener;

import java.util.Map.Entry;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FileToDatabaseJobListener extends JobExecutionListenerSupport {

    private static final String NEW_LINE = "\n";
    private static final String SEPRATER = "==========================================================";
    
    @Override
    public void afterJob(JobExecution jobExecution) {
        StringBuilder summary = new StringBuilder();
        summary.append(NEW_LINE + "================== JOB SUMMARY ===========================" + NEW_LINE);
        summary.append(NEW_LINE + SEPRATER + NEW_LINE);
        summary.append("JOB SUMMARY  for " + jobExecution.getJobInstance().getJobName() + NEW_LINE);
        summary.append("  Started     : " + jobExecution.getStartTime() + NEW_LINE);
        summary.append("  Finished    : " + jobExecution.getEndTime() + NEW_LINE);
        summary.append("  Exit-Code   : " + jobExecution.getExitStatus().getExitCode() + NEW_LINE);
        summary.append("  Exit-Descr. : " + jobExecution.getExitStatus().getExitDescription() + NEW_LINE);
        summary.append("  Status      : " + jobExecution.getStatus() + NEW_LINE);
        summary.append(SEPRATER + NEW_LINE);

        summary.append("Job-Parameter: \n");
        JobParameters jp = jobExecution.getJobParameters();
        for (Entry<String, JobParameter> entry : jp.getParameters().entrySet()) {
            summary.append("  " + entry.getKey() + "=" + entry.getValue() + NEW_LINE);
        }
        summary.append(SEPRATER + NEW_LINE);
        
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            summary.append(NEW_LINE + SEPRATER + NEW_LINE);
            summary.append("Step " + stepExecution.getStepName() + NEW_LINE);
            summary.append("WriteCount: " + stepExecution.getWriteCount() + NEW_LINE);
            summary.append("Commits: " + stepExecution.getCommitCount() + NEW_LINE);
            summary.append("SkipCount: " + stepExecution.getSkipCount() + NEW_LINE);
            summary.append("Rollbacks: " + stepExecution.getRollbackCount() + NEW_LINE);
            summary.append("Filter: " + stepExecution.getFilterCount() + NEW_LINE);
            summary.append(SEPRATER + NEW_LINE);
        }
        log.info(summary.toString());
        
    }
}
