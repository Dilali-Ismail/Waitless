package com.waitless.queueservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.kafka.bootstrap-servers=127.0.0.1:9092",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8180/mock-certs"
})
class QueueServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
