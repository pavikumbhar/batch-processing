package com.pravinkumbhar.batchprocessing.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.pravinkumbhar.batchprocessing.batch.config.BatchConfiguration;
import com.pravinkumbhar.batchprocessing.listener.FileToDatabaseJobListener;
import com.pravinkumbhar.batchprocessing.model.InformationDto;
import com.pravinkumbhar.batchprocessing.processor.FileToDatabaseItemProcessor;

/**
 *
 * @author Pravin Kumbhar
 */

@Configuration
@ConditionalOnProperty(prefix = "batch.file.to.database", name = "enabled", matchIfMissing = false)
public class FileToDatabaseBatchConfiguration extends BatchConfiguration {
    
    private static final String QUERY_INSERT_INFORMATION = "INSERT INTO INFORMATION (id, title, description) VALUES (:id, :title, :description)";
    
    @Bean
    public FlatFileItemReader<InformationDto> fileToDatabaseFlatFileItemReader() {
        FlatFileItemReader<InformationDto> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("filesystemdata.csv"));
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

    @Bean
    ItemProcessor<InformationDto, InformationDto> fileToDatabaseItemProcessor() {
        return new FileToDatabaseItemProcessor();
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
    public Step csvFileToDatabaseStep() {
        return stepBuilderFactory.get("csvFileToDatabaseStep").<InformationDto, InformationDto> chunk(1)//
                .reader(fileToDatabaseFlatFileItemReader()) //
                .processor(fileToDatabaseItemProcessor()) //
                .writer(fileToDatabaseItemWriter()) //
                .build();
    }

    @Bean
    Job csvFileToDatabaseJob(FileToDatabaseJobListener fileToDatabaseJobListener) {
        return jobBuilderFactory.get("csvFileToDatabaseJob") //
                .incrementer(new RunIdIncrementer()) //
                .listener(fileToDatabaseJobListener) //
                .flow(csvFileToDatabaseStep()) //
                .end() //
                .build();
    }

}
