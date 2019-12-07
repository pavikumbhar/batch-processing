package com.pravinkumbhar.batchprocessing.integration.gateway;

import java.io.File;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface TransferGateway {
    
    @Gateway(requestChannel = "toftpChannel")
    void upload(File file);
    
}