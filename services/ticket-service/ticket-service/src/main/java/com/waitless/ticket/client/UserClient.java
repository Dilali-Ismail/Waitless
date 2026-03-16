package com.waitless.ticket.client;


import com.waitless.ticket.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/api/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") String userId);
}
