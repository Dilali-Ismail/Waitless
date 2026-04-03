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
public class TicketCalledEvent {
    private String eventType;
    private Long ticketId;
    private Long queueId;
    private String userId;
    private String counterNumber;
    private Integer previousPosition;
    private LocalDateTime calledAt;
    private LocalDateTime timestamp;
}
