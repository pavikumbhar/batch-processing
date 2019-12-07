package com.pravinkumbhar.batchprocessing.integration.ftp;

import java.io.File;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.outbound.FtpMessageHandler;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.messaging.MessageHandler;

@Configuration
@ConditionalOnProperty(prefix = "sftp", name = "active", havingValue = "false", matchIfMissing = false)
public class FtpConfig {
    
    @Value("${ftp.host}")
    private String ftpHost;
    
    @Value("${ftp.port:21}")
    private int ftpPort;
    
    @Value("${ftp.user}")
    private String ftpUser;
    
    @Value("${ftp.password:#{null}}")
    private String ftpPasword;
    
    @Value("${ftp.remote.directory:/}")
    private String ftpRemoteDirectory;
    
    @Bean
    public SessionFactory<FTPFile> ftpSessionFactory() {
        final DefaultFtpSessionFactory defaultFtpSessionFactory = new DefaultFtpSessionFactory();
        defaultFtpSessionFactory.setClientMode(FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE);
        defaultFtpSessionFactory.setHost(ftpHost);
        defaultFtpSessionFactory.setPort(ftpPort);
        defaultFtpSessionFactory.setUsername(ftpUser);
        defaultFtpSessionFactory.setPassword(ftpPasword);
        return defaultFtpSessionFactory;
    }

    @Bean
    @ServiceActivator(inputChannel = "toftpChannel")
    public MessageHandler ftpMessageHandlerServiceActivator() {
        FtpMessageHandler handler = new FtpMessageHandler(ftpSessionFactory());
        handler.setRemoteDirectoryExpression(new LiteralExpression(ftpRemoteDirectory));
        handler.setCharset("UTF-8");
        handler.setFileNameGenerator(message -> {
            if (message.getPayload() instanceof File) {
                return ((File) message.getPayload()).getName();
            } else {
                throw new IllegalArgumentException("File expected as payload.");
            }
        });
        return handler;
    }
    
}