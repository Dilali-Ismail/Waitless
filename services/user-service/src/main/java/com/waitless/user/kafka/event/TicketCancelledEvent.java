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
@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketCancelledEvent {

    private String eventType;

    private Long ticketId;

    private Long queueId;

    private String userId;

    private Integer previousPosition;

    private Integer estimatedWaitTimeMinutes;

    private Boolean isLateCancellation;

    private LocalDateTime createdAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime timestamp;

    /**
     * Calcule l'heure estimée d'appel : heure d'annulation + temps d'attente estimé.
     * Utilisé par le handler pour déterminer si l'annulation est tardive.
     */
    public LocalDateTime getEstimatedCallTime() {
        if (cancelledAt == null || estimatedWaitTimeMinutes == null) {
            return null;
        }
        return cancelledAt.plusMinutes(estimatedWaitTimeMinutes);
    }
}
