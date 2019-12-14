package com.pavikumbhar.batchprocessing.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pavikumbhar.batchprocessing.batch.config.BatchConfiguration;
import com.pavikumbhar.batchprocessing.listener.DatabaseToFileJobExecutionListener;
import com.pavikumbhar.batchprocessing.model.DBInformationDto;
import com.pavikumbhar.batchprocessing.processor.DatabaseToFileItemProcessor;
import com.pavikumbhar.batchprocessing.reader.DBInformationRowMapper;
import com.pavikumbhar.batchprocessing.tasklet.FileArchivingTasklet;
import com.pavikumbhar.batchprocessing.tasklet.FtpTasklet;

/**
 *
 * @author Pravin Kumbhar
 */

@Configuration
@ConditionalOnProperty(prefix = "batch.database.to.file", name = "enabled", matchIfMissing = false)
public class DatabaseToFileBatchConfiguration extends BatchConfiguration {
    
    private static final String QUERY_SELECT_INFORMATION = "SELECT ID,TITLE, DESCRIPTION FROM INFORMATION";
    
    /**
     * Reader
     *
     * @return
     */
    @Bean
    public JdbcCursorItemReader<DBInformationDto> databaseToFileItemReader() {
        JdbcCursorItemReader<DBInformationDto> itemReader = new JdbcCursorItemReader<>();
        itemReader.setDataSource(dataSource);
        itemReader.setSql(QUERY_SELECT_INFORMATION);
        itemReader.setRowMapper(new DBInformationRowMapper());
        return itemReader;
    }
    
    /**
     *
     * @return
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<DBInformationDto> databaseToFileFlatFileItemWriter(@Value("#{jobParameters[pathToFile]}") String pathToFile) {
        FlatFileItemWriter<DBInformationDto> flatFileItemWriter = new FlatFileItemWriter<>();
        flatFileItemWriter.setResource(resourceLoader.getResource("file:" + pathToFile));
        
        DelimitedLineAggregator<DBInformationDto> delimitedLineAggregator = new DelimitedLineAggregator<>();
        delimitedLineAggregator.setDelimiter("|");
        
        BeanWrapperFieldExtractor<DBInformationDto> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] { "id", "title", "description" });
        delimitedLineAggregator.setFieldExtractor(fieldExtractor);
        
        flatFileItemWriter.setLineAggregator(delimitedLineAggregator);
        return flatFileItemWriter;
        
    }
    
    @Bean
    public DatabaseToFileItemProcessor databaseToFileItemProcessor() {
        return new DatabaseToFileItemProcessor();
    }
    
    @Bean
    public Step databaseToFileStep(FlatFileItemWriter<DBInformationDto> databaseToFileFlatFileItemWriter) {
        final int chunkSize = 10;
        return stepBuilderFactory.get("databaseToFileStep") //
                .<DBInformationDto, DBInformationDto> chunk(chunkSize) //
                .reader(databaseToFileItemReader()) //
                .processor(databaseToFileItemProcessor()) //
                .writer(databaseToFileFlatFileItemWriter) //
                .build();
    }

    @Bean
    public Step uploadFileStep(FtpTasklet ftpTasklet) {
        return stepBuilderFactory.get("uploadFileStep") //
                .tasklet(ftpTasklet).build();
    }

    @Bean
    public Step fileArchivingStep(FileArchivingTasklet fileArchivingTasklet) {
        return stepBuilderFactory.get("fileArchivingStep") //
                .tasklet(fileArchivingTasklet).build();
    }

    @Bean
    Job databaseToFileJob(DatabaseToFileJobExecutionListener databaseToFileJobExecutionListener, Step databaseToFileStep, Step uploadFileStep,
            Step fileArchivingStep) {
        return jobBuilderFactory.get("databaseToFileJob") //
                .incrementer(new RunIdIncrementer()) //
                .listener(databaseToFileJobExecutionListener) //
                .flow(databaseToFileStep) //
                .next(uploadFileStep) //
                .next(fileArchivingStep) //
                .end() //
                .build(); //
    }
}
