package com.restkeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@EnableDiscoveryClient
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class},scanBasePackages = {"com.restkeeper"})
@RefreshScope
public class EsProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsProviderApplication.class, args);
    }
}

