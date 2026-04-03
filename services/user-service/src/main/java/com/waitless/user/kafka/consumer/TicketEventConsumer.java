package com.waitless.user.kafka.consumer;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waitless.user.kafka.event.*;
import com.waitless.user.kafka.handler.TicketEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventConsumer {

    private final TicketEventHandler ticketEventHandler;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "ticket-events", groupId = "user-service-group")
    public void consumeTicketEvent(String message) {
        log.info("Received ticket event from Kafka: {}", message);

        try {
            JsonNode node = objectMapper.readTree(message);
            String eventType = node.get("eventType").asText();

            switch (eventType) {
                case "TICKET_CREATED":
                    TicketCreatedEvent createdEvent = objectMapper.treeToValue(node, TicketCreatedEvent.class);
                    ticketEventHandler.handleTicketCreation(createdEvent);
                    break;
                case "TICKET_CANCELLED":
                    TicketCancelledEvent cancelledEvent = objectMapper.treeToValue(node, TicketCancelledEvent.class);
                    ticketEventHandler.handleTicketCancellation(cancelledEvent);
                    break;
                case "TICKET_COMPLETED":
                    TicketCompletedEvent completedEvent = objectMapper.treeToValue(node, TicketCompletedEvent.class);
                    ticketEventHandler.handleTicketCompletion(completedEvent);
                    break;
                case "TICKET_ABSENT":
                    TicketAbsentEvent absentEvent = objectMapper.treeToValue(node, TicketAbsentEvent.class);
                    ticketEventHandler.handleTicketAbsence(absentEvent);
                    break;
                default:
                    log.debug("Ignored event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing ticket event: {}", message, e);
        }
    }



}
