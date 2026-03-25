package com.waitless.estimation.service;

import com.waitless.estimation.dto.EstimationRequest;
import com.waitless.estimation.dto.EstimationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class EstimationServiceTest {

    @InjectMocks
    private EstimationService estimationService;

    @BeforeEach
    void setUp() {
        // Arrange default configuration
        ReflectionTestUtils.setField(estimationService, "defaultServiceTime", 3);
        ReflectionTestUtils.setField(estimationService, "queueServiceTimes", new HashMap<Long, Integer>());
    }

    @Test
    void calculateEstimation_Success_DefaultTime() {
        // Arrange
        Long queueId = 1L;
        Integer position = 5;
        EstimationRequest request = EstimationRequest.builder()
                .queueId(queueId)
                .position(position)
                .build();

        // Act
        EstimationResponse response = estimationService.calculateEstimation(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEstimatedWaitMinutes()).isEqualTo(15); // 5 * 3
        assertThat(response.getQueueId()).isEqualTo(queueId);
        assertThat(response.getPosition()).isEqualTo(position);
        assertThat(response.getCalculatedAt()).isNotNull();
    }

    @Test
    void calculateEstimation_Success_CustomTime() {
        // Arrange
        Long queueId = 2L;
        Integer position = 4;
        Map<Long, Integer> customServiceTimes = new HashMap<>();
        customServiceTimes.put(queueId, 10);
        ReflectionTestUtils.setField(estimationService, "queueServiceTimes", customServiceTimes);

        EstimationRequest request = EstimationRequest.builder()
                .queueId(queueId)
                .position(position)
                .build();

        // Act
        EstimationResponse response = estimationService.calculateEstimation(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEstimatedWaitMinutes()).isEqualTo(40); // 4 * 10
    }

    @Test
    void calculateEstimation_InvalidQueueId_ThrowsException() {
        // Arrange
        EstimationRequest request = EstimationRequest.builder()
                .queueId(0L)
                .position(5)
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> estimationService.calculateEstimation(request));
    }

    @Test
    void calculateEstimation_InvalidPosition_ThrowsException() {
        // Arrange
        EstimationRequest request = EstimationRequest.builder()
                .queueId(1L)
                .position(0)
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> estimationService.calculateEstimation(request));
    }
}
