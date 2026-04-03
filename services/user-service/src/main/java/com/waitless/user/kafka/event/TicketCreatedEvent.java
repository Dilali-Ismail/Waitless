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
public class TicketCreatedEvent {

    private String eventType;
    private Long ticketId;
    private Long queueId;
    private String userId;
    private String clientName;
    private Integer position;
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime timestamp;
}
