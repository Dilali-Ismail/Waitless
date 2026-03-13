package com.waitless.user.kafka.handler;

import com.waitless.user.kafka.event.TicketAbsentEvent;
import com.waitless.user.kafka.event.TicketCancelledEvent;
import com.waitless.user.kafka.event.TicketCompletedEvent;
import com.waitless.user.kafka.event.TicketCreatedEvent;
import com.waitless.user.repository.UserRepository;
import com.waitless.user.service.RestrictionService;
import com.waitless.user.service.ScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventHandler {
    private final RestrictionService restrictionService;
    private final ScoringService scoringService;
    private final UserRepository userRepository;

    private static final int WARNING_MINUTES_THRESHOLD = 2;

    public void handleTicketCreation(TicketCreatedEvent event) {
        log.info("Processing ticket creation: ticketId={}, userId={}",
                event.getTicketId(), event.getUserId());
        restrictionService.incrementTicketsCreated(event.getUserId());

        scoringService.addPoints(event.getUserId(), 5.0, "Ticket créé");
    }

    public void handleTicketCompletion(TicketCompletedEvent event) {
        log.info("Processing ticket completion: ticketId={}, userId={}",
                event.getTicketId(), event.getUserId());
        restrictionService.incrementTicketsServed(event.getUserId());

        scoringService.addPoints(event.getUserId(), 10.0, "Ticket complété (utilisateur présent)");
    }

    public void handleTicketAbsence(TicketAbsentEvent event) {
        log.info("Processing ticket absence (No-Show): ticketId={}, userId={}",
                event.getTicketId(), event.getUserId());
        String reason = "User marked as absent at counter";
        restrictionService.applyNoShow(event.getUserId(), event.getTicketId(), reason);
    }

    public void handleTicketCancellation(TicketCancelledEvent event) {
        log.info("Processing ticket cancellation: ticketId={}, userId={}",
                event.getTicketId(), event.getUserId());

        LocalDateTime estimatedCallTime = event.getEstimatedCallTime();

        if (estimatedCallTime == null || event.getCancelledAt() == null) {
            log.warn("Missing time info (cancelledAt or estimatedCallTime) for ticketId={}. Only incrementing cancellation count.",
                    event.getTicketId());
            restrictionService.incrementTicketsCancelled(event.getUserId());
            return;
        }

        Duration timeDiff = Duration.between(event.getCancelledAt(), estimatedCallTime);
        long minutesDiff = timeDiff.toMinutes();

        log.debug("Time difference: {} minutes", minutesDiff);

        if (minutesDiff > WARNING_MINUTES_THRESHOLD) {
            log.info("Cancellation acceptable (>30min): no penalty for userId={}", event.getUserId());
            restrictionService.incrementTicketsCancelled(event.getUserId());
            return;
        }

        if (minutesDiff > 0 && minutesDiff <= WARNING_MINUTES_THRESHOLD) {
            String reason = String.format("Cancelled %d minutes before estimated call time", minutesDiff);
            log.warn("Applying WARNING: userId={}, reason={}", event.getUserId(), reason);
            restrictionService.applyWarning(event.getUserId(), event.getTicketId(), reason);
            // On incrémente aussi le compteur de tickets annulés
            restrictionService.incrementTicketsCancelled(event.getUserId());
            return;
        }

        if (minutesDiff <= 0) {
            String reason = String.format("No-show: cancelled %d minutes AFTER estimated call time",
                    Math.abs(minutesDiff));
            log.warn("Applying NO_SHOW: userId={}, reason={}", event.getUserId(), reason);
            restrictionService.applyNoShow(event.getUserId(), event.getTicketId(), reason);
            // On incrémente aussi le compteur de tickets annulés
            restrictionService.incrementTicketsCancelled(event.getUserId());
            return;
        }
    }
}
