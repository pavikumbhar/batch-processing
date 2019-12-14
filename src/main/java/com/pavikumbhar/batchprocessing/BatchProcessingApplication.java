package com.pavikumbhar.batchprocessing;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * @author Pravin Kumbhar
 *
 */
@EnableBatchProcessing
@SpringBootApplication
@PropertySource(value = "file:${app.propertyLoc}ftp.properties")
@PropertySource(value = "file:${app.propertyLoc}batch.properties")
public class BatchProcessingApplication extends SpringBootServletInitializer {
    
    public static void main(String[] args) {
        SpringApplication.run(BatchProcessingApplication.class, args);
    }
    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(BatchProcessingApplication.class);
    }
    
}
