package com.pravinkumbhar.batchprocessing.tasklet;

import java.io.File;
import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pravinkumbhar.batchprocessing.integration.gateway.TransferGateway;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Pravin Kumbhar
 *
 */

@Slf4j
@Getter
@Setter
@Component
@StepScope
public class FtpTasklet implements Tasklet {

    @Autowired
    private TransferGateway transferGateway;
    
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
        String pathToFile = (String) jobParameters.get("pathToFile");
        log.debug("#################### Sending file {} ", pathToFile);
        File file = new File(pathToFile);
        transferGateway.upload(file);
        return RepeatStatus.FINISHED;
    }
    
}