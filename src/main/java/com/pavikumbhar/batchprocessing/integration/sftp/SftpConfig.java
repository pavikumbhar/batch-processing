package com.pavikumbhar.batchprocessing.integration.sftp;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageHandler;

import com.jcraft.jsch.ChannelSftp.LsEntry;

@Configuration
@ConditionalOnProperty(prefix = "sftp", name = "active", havingValue = "true", matchIfMissing = false)
public class SftpConfig {
    
    @Value("${sftp.host}")
    private String sftpHost;
    
    @Value("${sftp.port:22}")
    private int sftpPort;
    
    @Value("${sftp.user}")
    private String sftpUser;
    
    @Value("${sftp.privateKey:#{null}}")
    private Resource sftpPrivateKey;
    
    @Value("${sftp.privateKeyPassphrase:}")
    private String sftpPrivateKeyPassphrase;
    
    @Value("${sftp.password:#{null}}")
    private String sftpPasword;
    
    @Value("${sftp.remote.directory:/}")
    private String sftpRemoteDirectory;
    
    @Bean
    public SessionFactory<LsEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(sftpHost);
        factory.setPort(sftpPort);
        factory.setUser(sftpUser);
        if (sftpPrivateKey != null) {
            factory.setPrivateKey(sftpPrivateKey);
            factory.setPrivateKeyPassphrase(sftpPrivateKeyPassphrase);
        } else {
            factory.setPassword(sftpPasword);
        }
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<>(factory);
    }
    
    @Bean
    @ServiceActivator(inputChannel = "toftpChannel")
    public MessageHandler sftpMessageHandlerServiceActivator() {
        SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
        handler.setRemoteDirectoryExpression(new LiteralExpression(sftpRemoteDirectory));
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