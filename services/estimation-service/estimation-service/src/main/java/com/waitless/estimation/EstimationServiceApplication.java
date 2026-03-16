package com.waitless.estimation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class EstimationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstimationServiceApplication.class, args);
    }

}
