package com.pavikumbhar.batchprocessing.integration.joblauncher;

import java.io.File;
import java.time.Clock;
import java.time.Instant;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring integration Transformer converts File Message to JobLaunchRequest.
 */

/**
 *
 * @author pravin kumnbhar
 *
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "batch.file.poller", name = "enable", matchIfMissing = false)
public class FileMessageToJobLaunchRequest {
    
    public static final String TIMESTAMP_PARAMETER = "timestamp";
    
    private final Job job;
    private final Clock clock;
    private final String jobParameter;
    
    @Autowired
    public FileMessageToJobLaunchRequest(final Job multiResourceJob, final Clock clock, @Value("${batch.job.parameter}") final String jobParameter) {
        this.job = multiResourceJob;
        this.clock = clock;
        this.jobParameter = jobParameter;
    }
    
    @Transformer(inputChannel = "filePollerInboundChannel", outputChannel = "filePollerOutboundJobRequestChannel")
    public JobLaunchRequest toRequest(final Message<File> message) {
        log.debug("toRequest : {}", message);
        final Instant timestamp = clock.instant();
        final JobParameters jobParameters = new JobParametersBuilder()//
                .addString(jobParameter, message.getPayload().getAbsolutePath())//
                .addString("stagingDirectory", "D:/stagingDirectory")//
                .addString("inputFile", message.getPayload().getAbsolutePath())//
                .addLong(TIMESTAMP_PARAMETER, timestamp.getEpochSecond()).toJobParameters();
        return new JobLaunchRequest(job, jobParameters);
    }
}