package com.waitless.notificationservice.consumer;

import com.waitless.notificationservice.client.UserClient;
import com.waitless.notificationservice.dto.UserDTO;
import com.waitless.notificationservice.event.TicketCalledEvent;
import com.waitless.notificationservice.event.TicketCancelledEvent;
import com.waitless.notificationservice.event.TicketCreatedEvent;
import com.waitless.notificationservice.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final SmsService smsService;
    private final UserClient userClient;

    @KafkaListener(topics = "${app.kafka.topic.ticket-events}", groupId = "notification-group")
    public void handleTicketCreated(TicketCreatedEvent event) {
        log.info("Événement reçu: TICKET_CREATED pour ticket {}", event.getTicketId());
        
        UserDTO user = userClient.getUserById(event.getUserId());
        if (user != null && user.getPhoneNumber() != null) {
            String message = String.format("Bonjour %s, votre ticket #%d a été créé. Vous êtes en position %d.", 
                    user.getName(), event.getTicketId(), event.getPosition());
            smsService.sendSms(user.getPhoneNumber(), message);
        } else {
            log.warn("Impossible d'envoyer la notification : numéro de téléphone absent pour l'utilisateur {}", event.getUserId());
        }
    }

    @KafkaListener(topics = "${app.kafka.topic.ticket-events}", groupId = "notification-group")
    public void handleTicketCalled(TicketCalledEvent event) {
        log.info("Événement reçu: TICKET_CALLED pour ticket {}", event.getTicketId());

        UserDTO user = userClient.getUserById(event.getUserId());
        if (user != null && user.getPhoneNumber() != null) {
            String message = String.format("Bonjour %s, c'est votre tour ! Veuillez vous présenter au guichet %s pour votre ticket #%d.", 
                    user.getName(), event.getCounterNumber(), event.getTicketId());
            smsService.sendSms(user.getPhoneNumber(), message);
        }
    }

    @KafkaListener(topics = "${app.kafka.topic.ticket-events}", groupId = "notification-group")
    public void handleTicketCancelled(TicketCancelledEvent event) {
        log.info("Événement reçu: TICKET_CANCELLED pour ticket {}", event.getTicketId());

        UserDTO user = userClient.getUserById(event.getUserId());
        if (user != null && user.getPhoneNumber() != null) {
            String message = String.format("Bonjour %s, votre ticket #%d a été annulé.", 
                    user.getName(), event.getTicketId());
            smsService.sendSms(user.getPhoneNumber(), message);
        }
    }
}
