package com.waitless.user.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class TicketCompletedEvent {

    private String eventType;
    private Long ticketId;
    private Long queueId;
    private String userId;
    private Integer counterNumber;

    private LocalDateTime completedAt;

    private LocalDateTime calledAt;

    private Long serviceTimeSeconds;

    private LocalDateTime timestamp;
}
