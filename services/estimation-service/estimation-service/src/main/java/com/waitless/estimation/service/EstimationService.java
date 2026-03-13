package com.waitless.estimation.service;

import com.waitless.estimation.dto.EstimationRequest;
import com.waitless.estimation.dto.EstimationResponse;
import lombok.Value;
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
    private Map<Long, Integer> queueServiceTimes;

    public EstimationResponse calculateEstimation(EstimationRequest request){

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
