package com.pavikumbhar.batchprocessing.tasklet;

import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import com.pavikumbhar.batchprocessing.FileSpliter;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Component
@StepScope
public class FileSplittingTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
        String pathToFile = (String) jobParameters.get("inputFile");
        String targetDir = (String) jobParameters.get("stagingDirectory");
        log.debug("#################### splitTextFiles file {} ", pathToFile);
        FileSpliter.splitTextFiles(pathToFile, 100, "", "", targetDir);

        return RepeatStatus.FINISHED;
    }

}
