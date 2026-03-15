package com.waitless.estimation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableFeignClients
public class EstimationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstimationServiceApplication.class, args);
    }

}
