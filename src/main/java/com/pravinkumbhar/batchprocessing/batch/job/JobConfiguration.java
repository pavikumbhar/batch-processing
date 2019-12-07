package com.pravinkumbhar.batchprocessing.batch.job;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

public class JobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public FlatFileItemReader<Map<String, String>> reader() throws Exception {
        FlatFileItemReader<Map<String, String>> reader = new FlatFileItemReader<>();
        reader.setName("DatReader");
        byte[] rawbytes = { 0x1D };
        String delimiter = new String(rawbytes);
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(delimiter);
        String[] names = new String[59];
        for (int i = 1; i < 60; i++) {
            names[i - 1] = String.valueOf(i);
        }
        tokenizer.setNames(names);

        DefaultLineMapper<Map<String, String>> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> {
            Map<String, String> item = new TreeMap<>();
            String[] fieldSetNames = fieldSet.getNames();

            for (String curName : fieldSetNames) {
                item.put(curName, fieldSet.readString(curName));
            }

            return item;
        });

        reader.setLineMapper(lineMapper);
        reader.setResource(new ClassPathResource("source.dat"));
        reader.afterPropertiesSet();

        return reader;
    }

    @Bean
    public ItemWriter<Map<String, String>> writer() {
        return items -> {
            for (Map<String, String> item : items) {
                System.out.println("****************** ITEM **********************");
                for (Map.Entry<String, String> entry : item.entrySet()) {
                    System.out.println(String.format("%s:%s", entry.getKey(), entry.getValue()));
                }
            }
        };
    }

    @Bean
    public Step step() throws Exception {
        return stepBuilderFactory.get("step1").<Map<String, String>, Map<String, String>> chunk(5)//
                .reader(reader())//
                .writer(writer())//
                .build();
    }

    @Bean
    public Job job() throws Exception {
        return jobBuilderFactory.get("job")//
                .start(step())//
                .build();
    }
}