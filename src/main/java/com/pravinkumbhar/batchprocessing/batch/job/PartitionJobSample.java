package com.pravinkumbhar.batchprocessing.batch.job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class PartitionJobSample {
    
    @Autowired
    private JobBuilderFactory jobs;
    
    @Autowired
    private StepBuilderFactory steps;
    
    @Bean
    public Step step1() {
        final int gridSize = 3;
        return steps.get("step1").partitioner(workerStep().getName(), partitioner())//
                .step(workerStep())//
                .gridSize(gridSize)//
                .taskExecutor(taskExecutor())//
                .build();
    }
    
    @Bean
    public SimpleAsyncTaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
    
    @Bean
    public Partitioner partitioner() {
        return gridSize -> {
            Map<String, ExecutionContext> map = new HashMap<>(gridSize);
            for (int i = 0; i < gridSize; i++) {
                ExecutionContext executionContext = new ExecutionContext();
                executionContext.put("data", "data" + i);
                String key = "partition" + i;
                map.put(key, executionContext);
            }
            return map;
        };
    }
    
    @Bean
    public Step workerStep() {
        return steps.get("workerStep").tasklet(getTasklet(null)).build();
    }
    
    @Bean
    @StepScope
    public Tasklet getTasklet(@Value("#{stepExecutionContext['data']}") String partitionData) {
        return (contribution, chunkContext) -> {
            if ("data2".equals(partitionData)) {
                throw new Exception("Boom!");
            }
            log.debug(Thread.currentThread().getName() + " processing partitionData = " + partitionData);
            Files.createFile(Paths.get(partitionData + ".txt"));
            return RepeatStatus.FINISHED;
        };
    }
    
    @Bean
    public Step moveFilesStep() {
        return steps.get("moveFilesStep").tasklet((contribution, chunkContext) -> {
            log.debug("moveFilesStep ");
            // add code to move files where needed
            return RepeatStatus.FINISHED;
        }).build();
    }
    
    @Bean
    public Step cleanupFilesStep() {
        return steps.get("cleanupFilesStep").tasklet((contribution, chunkContext) -> {
            log.debug("cleaning up..");
            deleteFiles();
            return RepeatStatus.FINISHED;
        }).build();
    }
    
    @Bean
    public Job job() {
        return jobs.get("job").flow(step1()).on("FAILED").to(cleanupFilesStep())//
                .from(step1()).on("*").to(moveFilesStep())//
                .from(moveFilesStep()).on("*").end() //
                .from(cleanupFilesStep()).on("*").fail() //
                .build() //
                .build();
    }
    
    public static void main(String[] args) throws Exception {
        deleteFiles();
        ApplicationContext context = new AnnotationConfigApplicationContext(PartitionJobSample.class);
        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        Job job = context.getBean(Job.class);
        jobLauncher.run(job, new JobParameters());
    }
    
    private static void deleteFiles() throws IOException {
        for (int i = 0; i <= 2; i++) {
            Files.deleteIfExists(Paths.get("data" + i + ".txt"));
        }
    }
    
}
