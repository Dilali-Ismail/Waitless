package com.waitless.ticket.service;

import com.waitless.ticket.entity.Ticket;
import com.waitless.ticket.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EventPublisher {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic.ticket-events}")
    private String ticketEventsTopic;

    public void publishTicketCreated(Ticket ticket) {
        TicketCreatedEvent event = TicketCreatedEvent.builder()
                .eventType("TICKET_CREATED")
                .ticketId(ticket.getId())
                .queueId(ticket.getQueueId())
                .userId(ticket.getUserId())
                .clientName(ticket.getClientName())
                .position(ticket.getPosition())
                .status(ticket.getStatus().name())
                .createdAt(ticket.getCreatedAt())
                .timestamp(LocalDateTime.now())
                .build();

        sendEvent(ticket.getId().toString(), event);
    }

    public void publishTicketCalled(Ticket ticket, Integer previousPosition) {
        TicketCalledEvent event = TicketCalledEvent.builder()
                .eventType("TICKET_CALLED")
                .ticketId(ticket.getId())
                .queueId(ticket.getQueueId())
                .userId(ticket.getUserId())
                .counterNumber(ticket.getCounterNumber())
                .previousPosition(previousPosition)
                .calledAt(ticket.getCalledAt())
                .timestamp(LocalDateTime.now())
                .build();

        sendEvent(ticket.getId().toString(), event);
    }

    public void publishTicketCompleted(Ticket ticket) {
        // Calculer temps de service
        long serviceTime = ChronoUnit.SECONDS.between(
                ticket.getCalledAt(),
                ticket.getCompletedAt()
        );

        TicketCompletedEvent event = TicketCompletedEvent.builder()
                .eventType("TICKET_COMPLETED")
                .ticketId(ticket.getId())
                .queueId(ticket.getQueueId())
                .userId(ticket.getUserId())
                .counterNumber(ticket.getCounterNumber())
                .calledAt(ticket.getCalledAt())
                .completedAt(ticket.getCompletedAt())
                .serviceTimeSeconds(serviceTime)
                .timestamp(LocalDateTime.now())
                .build();

        sendEvent(ticket.getId().toString(), event);
    }

    public void publishTicketAbsent(Ticket ticket) {
        long waitTime = ChronoUnit.MINUTES.between(
                ticket.getCreatedAt(),
                ticket.getCalledAt()
        );

        TicketAbsentEvent event = TicketAbsentEvent.builder()
                .eventType("TICKET_ABSENT")
                .ticketId(ticket.getId())
                .queueId(ticket.getQueueId())
                .userId(ticket.getUserId())
                .counterNumber(ticket.getCounterNumber())
                .calledAt(ticket.getCalledAt())
                .markedAbsentAt(LocalDateTime.now())
                .waitTimeMinutes(waitTime)
                .timestamp(LocalDateTime.now())
                .build();

        sendEvent(ticket.getId().toString(), event);
    }
    public void publishTicketCancelled(Ticket ticket) {
        LocalDateTime now = LocalDateTime.now();
        Integer estimatedWaitTime = ticket.getEstimatedWaitTime();
        boolean isLateCancellation = false;

        if (estimatedWaitTime != null && ticket.getCreatedAt() != null) {
            LocalDateTime estimatedCallTime = ticket.getCreatedAt().plusMinutes(estimatedWaitTime);
            long minutesBeforeTurn = ChronoUnit.MINUTES.between(now, estimatedCallTime);
            isLateCancellation = minutesBeforeTurn < 2;
        }

        TicketCancelledEvent event = TicketCancelledEvent.builder()
                .eventType("TICKET_CANCELLED")
                .ticketId(ticket.getId())
                .queueId(ticket.getQueueId())
                .userId(ticket.getUserId())
                .previousPosition(ticket.getPosition())
                .estimatedWaitTimeMinutes(estimatedWaitTime)
                .isLateCancellation(isLateCancellation)
                .createdAt(ticket.getCreatedAt())
                .cancelledAt(now)
                .timestamp(now)
                .build();

        sendEvent(ticket.getId().toString(), event);
    }
    private void sendEvent(String key, Object event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(ticketEventsTopic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info(" Événement publié avec succès : {} | Topic: {} | Partition: {} | Offset: {}",
                            event.getClass().getSimpleName(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error(" Échec publication événement : {} | Raison: {}",
                            event.getClass().getSimpleName(),
                            ex.getMessage());
                }
            });

        } catch (Exception e) {
            log.error(" Erreur lors de l'envoi Kafka : {}", e.getMessage(), e);
        }

    }
}
