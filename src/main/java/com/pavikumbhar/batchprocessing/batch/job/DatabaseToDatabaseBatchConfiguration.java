package com.pavikumbhar.batchprocessing.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pavikumbhar.batchprocessing.batch.config.BatchConfiguration;
import com.pavikumbhar.batchprocessing.model.DBInformationDto;
import com.pavikumbhar.batchprocessing.model.PartialInformationDto;
import com.pavikumbhar.batchprocessing.processor.DatabaseToDatabaseItemProcessor;
import com.pavikumbhar.batchprocessing.reader.DBInformationRowMapper;

@Configuration
@ConditionalOnProperty(prefix = "batch.database.to.database", name = "enabled", matchIfMissing = false)
public class DatabaseToDatabaseBatchConfiguration extends BatchConfiguration {

    private static final String QUERY_SELECT_INFORMATION = "SELECT ID,TITLE, DESCRIPTION FROM INFORMATION";
    private static final String QUERY_INSERT_PARTIAL_INFORMATION = "INSERT INTO PARTIAL_INFORMATION (ID, TITLE) VALUES (:id, :title)";
    
    @Bean
    public ItemStreamReader<DBInformationDto> databasseToDatabaseItemReader() {
        JdbcCursorItemReader<DBInformationDto> itemReader = new JdbcCursorItemReader<>();
        itemReader.setDataSource(dataSource);
        itemReader.setSql(QUERY_SELECT_INFORMATION);
        itemReader.setRowMapper(new DBInformationRowMapper());
        return itemReader;
    }
    
    @Bean
    public ItemProcessor<DBInformationDto, PartialInformationDto> databaseToDatabaseItemProcessor() {
        return new DatabaseToDatabaseItemProcessor();
    }
    
    @Bean
    public ItemWriter<PartialInformationDto> databaseToDatabaseItemWriter() {
        JdbcBatchItemWriter<PartialInformationDto> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<PartialInformationDto>());
        writer.setSql(QUERY_INSERT_PARTIAL_INFORMATION);
        writer.setDataSource(dataSource);
        return writer;
    }
    
    @Bean
    public Job databaseToDatabaseJob() {
        return jobBuilderFactory.get("databaseToDatabaseJob") //
                .incrementer(new RunIdIncrementer()) //
                .start(databaseToDatabaseStep()) //
                .build();
    }
    
    @Bean
    public Step databaseToDatabaseStep() {
        final int chunkSize = 10;
        return stepBuilderFactory.get("databaseToDatabaseStep") //
                .<DBInformationDto, PartialInformationDto> chunk(chunkSize) //
                .reader(databasseToDatabaseItemReader()) //
                .processor(databaseToDatabaseItemProcessor()) //
                .writer(databaseToDatabaseItemWriter()) //
                .build();
    }
    
}
