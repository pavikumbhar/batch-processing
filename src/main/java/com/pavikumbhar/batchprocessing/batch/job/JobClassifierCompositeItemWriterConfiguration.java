package com.pavikumbhar.batchprocessing.batch.job;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.classify.BackToBackPatternClassifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.pavikumbhar.batchprocessing.batch.config.BatchConfiguration;
import com.pavikumbhar.batchprocessing.classifier.ProductRouterClassifier;
import com.pavikumbhar.batchprocessing.model.Product;
import com.pavikumbhar.batchprocessing.writer.DeleteProductItemWriter;
import com.pavikumbhar.batchprocessing.writer.InsertProductItemWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class JobClassifierCompositeItemWriterConfiguration extends BatchConfiguration {
    
    private static final String INSERT = "C";
    private static final String UPDATE = "U";
    private static final String DELETE = "D";
    private static final int CHUNK_SIZE = 3;
    
    @Bean
    @StepScope
    public FlatFileItemReader<Product> productItemReader() {
        FlatFileItemReader<Product> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("products-delimited-big.txt"));
        reader.setLinesToSkip(1);
        DefaultLineMapper<Product> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(",");
        tokenizer.setNames("id", "name", "description", "price", "operation");
        lineMapper.setLineTokenizer(tokenizer);
        
        BeanWrapperFieldSetMapper<Product> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(Product.class);
        lineMapper.setFieldSetMapper(mapper);
        reader.setLineMapper(lineMapper);

        return reader;
    }

    @Bean
    public ClassifierCompositeItemWriter<Product> itemWriter(ItemWriter<Product> insertJdbcBatchItemWriter, ItemWriter<Product> updateJdbcBatchItemWriter,
            ItemWriter<Product> deleteJdbcBatchItemWriter) {
        ClassifierCompositeItemWriter<Product> classifierCompositeItemWriter = new ClassifierCompositeItemWriter<>();
        classifierCompositeItemWriter.setClassifier(product -> {
            ItemWriter<? super Product> itemWriter = null;
            if (INSERT.equalsIgnoreCase(product.getOperation())) {
                itemWriter = insertJdbcBatchItemWriter;
            }

            if (UPDATE.equalsIgnoreCase(product.getOperation())) {
                itemWriter = updateJdbcBatchItemWriter;
            }
            
            if (DELETE.equalsIgnoreCase(product.getOperation())) {
                itemWriter = deleteJdbcBatchItemWriter;
            }
            
            log.info("Product would be classified with " + itemWriter.getClass().getSimpleName());
            return itemWriter;
        });
        return classifierCompositeItemWriter;
    }
    
    @Bean
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ItemWriter<Product> productItemWriter() {
        Map<String, ItemWriter<? extends Product>> matcherMap = new HashMap<>();
        matcherMap.put(INSERT, insertJdbcBatchItemWriter());
        matcherMap.put(UPDATE, updateJdbcBatchItemWriter());
        matcherMap.put(DELETE, deleteJdbcBatchItemWriter());
        
        BackToBackPatternClassifier classifier = new BackToBackPatternClassifier();
        classifier.setRouterDelegate(new ProductRouterClassifier());
        classifier.setMatcherMap(matcherMap);

        ClassifierCompositeItemWriter<Product> writer = new ClassifierCompositeItemWriter<>();
        writer.setClassifier(classifier);
        return writer;
    }
    
    @Bean
    public ItemWriter<Product> insertJpaBatchItemWriter() {
        return new InsertProductItemWriter();
    }

    @Bean
    public ItemWriter<Product> deleteJpaBatchItemWriter() {
        return new DeleteProductItemWriter();
    }
    
    @Bean
    public ItemWriter<Product> insertJdbcBatchItemWriter() {
        JdbcBatchItemWriter<Product> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Product>());
        writer.setSql("INSERT INTO PRODUCT (ID, NAME, PRICE) VALUES(:id, :name, :price)");
        writer.setDataSource(dataSource);
        writer.afterPropertiesSet();
        return writer;
    }

    @Bean
    public ItemWriter<Product> updateJdbcBatchItemWriter() {
        JdbcBatchItemWriter<Product> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Product>());
        writer.setSql("UPDATE PRODUCT SET NAME=:name, PRICE=:price WHERE ID=:id");
        writer.setDataSource(dataSource);
        writer.afterPropertiesSet();
        return writer;
    }
    
    @Bean
    public ItemWriter<Product> deleteJdbcBatchItemWriter() {
        JdbcBatchItemWriter<Product> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Product>());
        writer.setSql("DELETE PRODUCT WHERE ID=:id");
        writer.setDataSource(dataSource);
        writer.afterPropertiesSet();
        return writer;
    }

    @Bean
    public Job writeProductsJob(Step readWriteStep) {
        return jobBuilderFactory.get("writeProductJob")//
                .start(readWriteStep)//
                .build();
    }

    @Bean
    public Step readWriteStep(ItemReader<Product> productItemReader, ItemWriter<Product> insertJdbcBatchItemWriter,
            ItemWriter<Product> updateJdbcBatchItemWriter, ItemWriter<Product> deleteJdbcBatchItemWriter) {
        return stepBuilderFactory.get("readWriteStep").<Product, Product> chunk(CHUNK_SIZE)//
                .reader(productItemReader())//
                .writer(itemWriter(insertJdbcBatchItemWriter, updateJdbcBatchItemWriter, deleteJdbcBatchItemWriter))//
                .build();

    }
}