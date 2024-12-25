package com.restkeeper.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class RabbitmqSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitmqSpringbootApplication.class, args);
    }
}
