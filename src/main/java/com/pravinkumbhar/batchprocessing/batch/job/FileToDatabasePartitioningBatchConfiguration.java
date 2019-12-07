package com.pravinkumbhar.batchprocessing.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceArrayPropertyEditor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.pravinkumbhar.batchprocessing.batch.config.BatchConfiguration;
import com.pravinkumbhar.batchprocessing.model.InformationDto;

/**
 *
 * @author Pravin Kumbhar
 */

@Configuration
public class FileToDatabasePartitioningBatchConfiguration extends BatchConfiguration {
    
    private static final String QUERY_INSERT_INFORMATION = "INSERT INTO INFORMATION (id, title, description) VALUES (:id, :title, :description)";

    @Bean
    @StepScope
    public Tasklet fileSplittingTaskletOld(@Value("#{jobParameters['inputFile']}") String inputFile,
            @Value("#{jobParameters['stagingDirectory']}") String stagingDirectory) throws Exception {
        SystemCommandTasklet tasklet = new SystemCommandTasklet();
        tasklet.setCommand(String.format("split -a 5 -l 10000 %s %s", inputFile, stagingDirectory));
        final long timeout = 60000L;
        tasklet.setTimeout(timeout);
        tasklet.setWorkingDirectory("/temp");
        tasklet.afterPropertiesSet();

        return tasklet;
    }
    
    @Bean
    @StepScope
    public MultiResourcePartitioner fileToDatabasePartitioner(@Value("#{jobParameters['stagingDirectory']}") String stagingDirectory) {
        MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
        partitioner.setResources(getResources(stagingDirectory));
        final int gridSize = 10;
        partitioner.partition(gridSize);
        return partitioner;
    }
    
    @Bean
    public TaskExecutor fileToDatabaseTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        final int poolSizs = 4;
        taskExecutor.setMaxPoolSize(poolSizs);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<InformationDto> fileToDatabaseItemReader(@Value("#{stepExecutionContext['fileName']}") Resource file) {
        FlatFileItemReader<InformationDto> reader = new FlatFileItemReader<>();
        reader.setResource(file);
        DefaultLineMapper<InformationDto> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter("|");
        tokenizer.setNames("id", "title", "description");
        lineMapper.setLineTokenizer(tokenizer);

        BeanWrapperFieldSetMapper<InformationDto> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(InformationDto.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        
        reader.setLineMapper(lineMapper);
        
        return reader;
    }

    private Resource[] getResources(String stagingDirectory) {
        ResourceArrayPropertyEditor resourceLoader = new ResourceArrayPropertyEditor();
        resourceLoader.setAsText("file:" + stagingDirectory + "/*");
        return (Resource[]) resourceLoader.getValue();
        
    }
    
    @Bean
    public JdbcBatchItemWriter<InformationDto> fileToDatabaseItemWriter() {
        JdbcBatchItemWriter<InformationDto> informationWriter = new JdbcBatchItemWriter<>();
        informationWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<InformationDto>());
        informationWriter.setSql(QUERY_INSERT_INFORMATION);
        informationWriter.setDataSource(dataSource);
        return informationWriter;
    }
    
    @Bean
    public Step fileSplittingStep(Tasklet fileSplittingTasklet) {
        return stepBuilderFactory.get("fileSplittingStep") //
                .tasklet(fileSplittingTasklet) //
                .build();
    }
    
    @Bean
    public Step fileDeletingStep(Tasklet fileDeletingTasklet) {
        return stepBuilderFactory.get("fileDeletingTasklet") //
                .tasklet(fileDeletingTasklet) //
                .build();
    }

    @Bean
    public Step masterStep(Step processData, MultiResourcePartitioner fileToDatabasePartitioner) {
        return stepBuilderFactory.get("masterStep")//
                .partitioner(processData)//
                .partitioner("processDataStep", fileToDatabasePartitioner) //
                .taskExecutor(fileToDatabaseTaskExecutor())//

                .build();
    }
    
    @Bean
    public Step processData(FlatFileItemReader<InformationDto> fileToDatabaseFlatFileItemReader, JdbcBatchItemWriter<InformationDto> fileToDatabaseItemWriter) {
        final int chunkSize = 50;
        return stepBuilderFactory.get("processData")//
                .<InformationDto, InformationDto> chunk(chunkSize)//
                .reader(fileToDatabaseFlatFileItemReader) //
                .writer(fileToDatabaseItemWriter) //
                .build();
    }
    
    @Bean
    public Job multiResourceJob(Step fileSplittingStep, Step masterStep, Step fileDeletingStep) {
        return jobBuilderFactory.get("multiResourceJob") //
                .incrementer(new RunIdIncrementer()) //
                .flow(fileSplittingStep) //
                .next(masterStep) //
                .next(fileDeletingStep)//
                .end() //
                .build();
    }
}
