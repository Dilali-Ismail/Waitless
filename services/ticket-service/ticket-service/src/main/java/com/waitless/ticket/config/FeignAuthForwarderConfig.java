package com.waitless.ticket.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignAuthForwarderConfig {

    /**
     * Propage le Bearer token entrant (frontend -> ticket-service) vers les appels Feign
     * (ticket-service -> user-service, estimation-service, etc.).
     *
     * Sans ça, les microservices protégés répondent 401.
     */
    @Bean
    public RequestInterceptor authForwarder() {
        return template -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return;
            HttpServletRequest request = attrs.getRequest();
            if (request == null) return;

            String auth = request.getHeader("Authorization");
            if (auth != null && !auth.isBlank()) {
                template.header("Authorization", auth);
            }
        };
    }
}

