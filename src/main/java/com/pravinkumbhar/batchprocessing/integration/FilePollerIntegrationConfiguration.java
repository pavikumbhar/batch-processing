package com.pravinkumbhar.batchprocessing.integration;

import java.io.File;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.integration.launch.JobLaunchingMessageHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;

@Configuration
@ConditionalOnProperty(prefix = "batch.file.poller", name = "enable", matchIfMissing = false)
public class FilePollerIntegrationConfiguration {
    
    @Bean
    public DirectChannel filePollerInboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public DirectChannel filePollerOutboundJobRequestChannel() {
        return new DirectChannel();
    }
    
    @Bean
    @InboundChannelAdapter(value = "filePollerInboundChannel", poller = @Poller(cron = "${batch.poller.cron}"))
    public MessageSource<File> fileMessageSource(@Value("${batch.poller.path}") final String path, @Value("${batch.poller.fileMask}") final String fileMask) {
        final FileReadingMessageSource source = new FileReadingMessageSource();
        final CompositeFileListFilter<File> compositeFileListFilter = new CompositeFileListFilter<>();
        final SimplePatternFileListFilter simplePatternFileListFilter = new SimplePatternFileListFilter(fileMask);
        final AcceptOnceFileListFilter<File> acceptOnceFileListFilter = new AcceptOnceFileListFilter<>();
        compositeFileListFilter.addFilter(simplePatternFileListFilter);
        compositeFileListFilter.addFilter(acceptOnceFileListFilter);
        source.setFilter(compositeFileListFilter);
        source.setDirectory(new File(path));
        return source;
    }

    @ServiceActivator(inputChannel = "filePollerOutboundJobRequestChannel", outputChannel = "loggingChannel")
    @Bean
    public JobLaunchingMessageHandler filePollerJobLaunchingGateway(final JobLauncher jobLauncher) {
        return new JobLaunchingMessageHandler(jobLauncher);
    }

}