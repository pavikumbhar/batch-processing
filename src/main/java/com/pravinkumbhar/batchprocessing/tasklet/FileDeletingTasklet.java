package com.pravinkumbhar.batchprocessing.tasklet;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Component
@StepScope
public class FileDeletingTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
        String targetDir = (String) jobParameters.get("stagingDirectory");
        log.debug("#################### deleteing  file {} ", targetDir);
        FileUtils.cleanDirectory(new File(targetDir));
        return RepeatStatus.FINISHED;
    }
    
}
