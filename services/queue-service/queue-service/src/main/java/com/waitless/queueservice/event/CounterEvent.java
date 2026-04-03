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
public class CounterEvent {
    private String eventType;
    private Long counterId;
    private Long queueId;
    private Integer counterNumber;
    private Boolean isActive;
    private LocalDateTime timestamp;


}
