package com.pavikumbhar.batchprocessing.tasklet;

import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
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
public class FileArchivingTasklet extends AbstractFileArchivingTasklet {

    @Override
    protected String archiveDirectoryPath() {
        return "D:/WinFTP/";
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
        String pathToFile = (String) jobParameters.get("pathToFile");
        log.debug("#################### File Archiving {} ", pathToFile);
        super.archiveFile(pathToFile);
        return RepeatStatus.FINISHED;
    }
    
}
