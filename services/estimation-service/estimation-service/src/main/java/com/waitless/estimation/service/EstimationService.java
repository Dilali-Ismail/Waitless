package com.waitless.estimation.service;

import com.waitless.estimation.dto.EstimationRequest;
import com.waitless.estimation.dto.EstimationResponse;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class EstimationService {

    @Value("${estimation.default-service-time:3}")
    private int defaultServiceTime;

    @Value("#{${estimation.queue-service-times:{}}}")
    private Map<Long, Integer> queueServiceTimes = new java.util.HashMap<>();

    public EstimationResponse calculateEstimation(EstimationRequest request){

        if (request.getQueueId() == null || request.getQueueId() <= 0) {
            log.error("Invalid queueId: {}", request.getQueueId());
            throw new IllegalArgumentException("Queue ID must be positive");
        }

        if (request.getPosition() == null || request.getPosition() < 1) {
            log.error("Invalid position: {}", request.getPosition());
            throw new IllegalArgumentException("Position must be at least 1");
        }

        int averageServiceTime = getAverageServiceTimeForQueue(request.getQueueId());

        int estimatedWaitMinutes = request.getPosition() * averageServiceTime;

        EstimationResponse response = EstimationResponse.builder()
                .estimatedWaitMinutes(estimatedWaitMinutes)
                .queueId(request.getQueueId())
                .position(request.getPosition())
                .calculatedAt(LocalDateTime.now())
                .build();

        return response;
    }

    private int getAverageServiceTimeForQueue(Long queueId){

        int avgTime = queueServiceTimes.getOrDefault(queueId, defaultServiceTime);
        return avgTime;
    }
}
