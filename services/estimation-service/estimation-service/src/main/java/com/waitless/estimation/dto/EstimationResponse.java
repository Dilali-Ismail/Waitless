package com.waitless.estimation.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimationResponse {

    private Integer estimatedWaitMinutes;
    private Long queueId;
    private Integer position;
    private LocalDateTime calculatedAt;

}
