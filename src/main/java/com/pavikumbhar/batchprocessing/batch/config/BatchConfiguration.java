package com.pavikumbhar.batchprocessing.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import lombok.Getter;

/**
 *
 * @author pavikumbhar
 */
@Getter
public abstract class BatchConfiguration {

    @Autowired
    protected JobBuilderFactory jobBuilderFactory;

    @Autowired
    protected StepBuilderFactory stepBuilderFactory;

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected ResourceLoader resourceLoader;

}
