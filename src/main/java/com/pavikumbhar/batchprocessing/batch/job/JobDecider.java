package com.pavikumbhar.batchprocessing.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class JobDecider {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    private static final String TYPE_ONE = "TYPE_ONE";
    private static final String TYPE_TWO = "TYPE_TWO";

    @Bean
    public Step firstStep() {
        return stepBuilderFactory.get("firstStep").tasklet((contribution, chunkContext) -> {
            log.debug("firstStep  ");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public JobExecutionDecider decider() {
        return (jobExecution, stepExecution) -> new FlowExecutionStatus(TYPE_ONE); // or TYPE2
    }

    @Bean
    public Step stepType1() {
        return stepBuilderFactory.get("stepType1").tasklet((contribution, chunkContext) -> {
            log.debug("stepType1 ");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step stepType2() {
        return stepBuilderFactory.get("stepType2").tasklet((contribution, chunkContext) -> {
            log.debug("stepType2 ");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step lastStep() {
        return stepBuilderFactory.get("lastStep").tasklet((contribution, chunkContext) -> {
            log.debug("lastStep ");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job").start(firstStep()).next(decider()).on(TYPE_ONE).to(stepType1())//
                .from(decider()).on(TYPE_TWO).to(stepType2())//
                .from(stepType1()).on("*").to(lastStep())//
                .from(stepType2()).on("*").to(lastStep()).build().build();
    }

    public static void main(String[] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(JobDecider.class);
        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        Job job = context.getBean(Job.class);
        jobLauncher.run(job, new JobParameters());
    }

}
