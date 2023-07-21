package com.pavikumbhar.batchprocessing.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 *
 * @author pavikumbhar
 */

@Configuration
public class BaseBatchCopnfig {

    /**
     * Setting to automatically register Job in JobRegistry.
     *
     * @param jobRegistry
     *            this Spring Batch Job Registry
     * @return JobRegistry BeanPostProcessor
     */
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(final JobRegistry jobRegistry) {
        final JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }

   // @Bean
    public JobRepository jobRepository(final DataSource dataSource, final PlatformTransactionManager transactionManager) throws Exception {
        final JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
        factoryBean.setDatabaseType("ORACLE");
        factoryBean.setDataSource(dataSource);
        factoryBean.setTransactionManager(transactionManager);
        factoryBean.setIsolationLevelForCreate("ISOLATION_READ_UNCOMMITTED");
        return factoryBean.getObject();
    }
}