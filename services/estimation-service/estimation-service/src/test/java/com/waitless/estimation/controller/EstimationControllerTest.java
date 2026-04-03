package com.waitless.estimation.controller;

import com.waitless.estimation.dto.EstimationRequest;
import com.waitless.estimation.dto.EstimationResponse;
import com.waitless.estimation.service.EstimationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EstimationController.class)
class EstimationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstimationService estimationService;

    @Test
    void calculateEstimation_Success() throws Exception {
        // Arrange
        EstimationResponse response = EstimationResponse.builder()
                .estimatedWaitMinutes(15)
                .queueId(1L)
                .position(5)
                .calculatedAt(LocalDateTime.now())
                .build();

        when(estimationService.calculateEstimation(any(EstimationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/estimations/calculate")
                        .param("queueId", "1")
                        .param("position", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedWaitMinutes").value(15))
                .andExpect(jsonPath("$.queueId").value(1))
                .andExpect(jsonPath("$.position").value(5));
    }

    @Test
    void calculateEstimation_InvalidParameters_ReturnsBadRequest() throws Exception {
        // Arrange
        when(estimationService.calculateEstimation(any(EstimationRequest.class)))
                .thenThrow(new IllegalArgumentException("Queue ID must be positive"));

        // Act & Assert
        mockMvc.perform(get("/api/estimations/calculate")
                        .param("queueId", "0")
                        .param("position", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Queue ID must be positive"));
    }

    @Test
    void calculateEstimation_MissingParameters_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/estimations/calculate")
                        .param("queueId", "1"))
                .andExpect(status().isBadRequest());
    }
}
