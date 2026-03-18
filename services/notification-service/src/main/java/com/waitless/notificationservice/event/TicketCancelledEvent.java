package com.waitless.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCancelledEvent {
    private String eventType;
    private Long ticketId;
    private Long queueId;
    private String userId;
    private Integer previousPosition;
    private Integer estimatedWaitTimeMinutes;
    private boolean isLateCancellation;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime timestamp;
}
