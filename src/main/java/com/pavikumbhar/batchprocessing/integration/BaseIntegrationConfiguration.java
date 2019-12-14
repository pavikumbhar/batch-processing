package com.pavikumbhar.batchprocessing.integration;

import java.time.Clock;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.jdbc.store.JdbcChannelMessageStore;
import org.springframework.integration.jdbc.store.channel.OracleChannelMessageStoreQueryProvider;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.scheduling.support.PeriodicTrigger;

@Configuration
@EnableIntegration
public class BaseIntegrationConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    public PollerMetadata defaultPoller() {
        final PollerMetadata pollerMetadata = new PollerMetadata();
        pollerMetadata.setTrigger(new PeriodicTrigger(10));
        return pollerMetadata;
    }
    
    @Bean
    @ServiceActivator(inputChannel = "loggingChannel")
    public LoggingHandler loggingHandler() {
        return new LoggingHandler(LoggingHandler.Level.INFO.name());
    }
    
    @Bean
    public OracleChannelMessageStoreQueryProvider channelMessageStoreQueryProvider() {
        return new OracleChannelMessageStoreQueryProvider();
    }

    @Bean
    public JdbcChannelMessageStore metadataStore(final DataSource dataSource, final OracleChannelMessageStoreQueryProvider channelMessageStoreQueryProvider) {
        final JdbcChannelMessageStore messageStore = new JdbcChannelMessageStore();
        messageStore.setDataSource(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(channelMessageStoreQueryProvider);
        return messageStore;
    }
}