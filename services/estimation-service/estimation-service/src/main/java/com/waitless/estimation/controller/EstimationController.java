package com.waitless.estimation.controller;


import com.waitless.estimation.dto.EstimationRequest;
import com.waitless.estimation.dto.EstimationResponse;
import com.waitless.estimation.service.EstimationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estimations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EstimationController {

    private final EstimationService estimationService;

    @GetMapping("/calculate")
    public ResponseEntity<EstimationResponse> calculateEstimation(
            @RequestParam(name = "queueId")
            @Valid Long queueId,

            @RequestParam(name = "position")
            @Valid Integer position) {

        EstimationRequest request = EstimationRequest.builder()
                .queueId(queueId)
                .position(position)
                .build();

        EstimationResponse response = estimationService.calculateEstimation(request);
        return ResponseEntity.ok(response);
    }
    
}
