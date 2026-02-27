package com.waitless.queueservice.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueEvent {
    private String eventType;
    private Long queueId;
    private Long companyId;
    private String queueName;
    private Boolean isActive;
    private Integer capacity;
    private Integer averageServiceTime;
    private LocalDateTime timestamp;
}
