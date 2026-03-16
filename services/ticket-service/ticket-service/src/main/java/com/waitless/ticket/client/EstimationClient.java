package com.waitless.ticket.client;


import com.waitless.ticket.dto.response.EstimationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "estimation-service")
public interface EstimationClient {


    @GetMapping("/api/estimations/calculate")
    EstimationResponse calculateEstimation(@RequestParam("queueId") Long queueId, @RequestParam("position") Integer position);
}
