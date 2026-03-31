package com.waitless.ticket.controller;


import com.waitless.ticket.dto.request.CallTicketRequest;
import com.waitless.ticket.dto.request.CreateTicketRequest;
import com.waitless.ticket.dto.request.UpdateTicketStatusRequest;
import com.waitless.ticket.dto.response.TicketResponse;
import com.waitless.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@Slf4j
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request)
    {
        TicketResponse ticketResponse = ticketService.creatTicket(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ticketResponse);
    }

    @PostMapping("/call")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<TicketResponse> callNextTicket(
            @Valid @RequestBody CallTicketRequest request) {

        log.info("📥 POST /api/tickets/call - Queue: {}, Guichet: {}",
                request.getQueueId(), request.getCounterNumber());

        TicketResponse response = ticketService.callNextTicket(request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request) {

        log.info("📥 PUT /api/tickets/{}/status - Status: {}", id, request.getStatus());

        TicketResponse response;

        switch (request.getStatus()) {
            case "COMPLETED":
                response = ticketService.completeTicket(id);
                log.info(" Ticket complété : ID {}", id);
                break;

            case "ABSENT":
                response = ticketService.markTicketAbsent(id);
                log.info(" Ticket marqué absent : ID {}", id);
                break;

            case "CANCELLED":
                response = ticketService.cancelTicket(id);
                log.info(" Ticket annulé : ID {}", id);
                break;

            default:
                log.warn(" Status invalide : {}", request.getStatus());
                throw new IllegalArgumentException("Status invalide : " + request.getStatus());
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<TicketResponse> cancelTicket(
            @PathVariable Long id) {

        log.info(" DELETE /api/tickets/{} - Raison: {}", id);

        TicketResponse response = ticketService.cancelTicket(id);

        log.info(" Ticket annulé : ID {}", id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<TicketResponse>> getMyTickets(
            @RequestParam String userId) {

        log.info("GET /api/tickets/me - User: {}", userId);

        List<TicketResponse> tickets = ticketService.getUserTickets(userId);

        log.info(" {} tickets trouvés", tickets.size());

        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/queue/{queueId}/waiting")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'COMPANY_ADMIN')")
    public ResponseEntity<List<TicketResponse>> getWaitingTickets(@PathVariable Long queueId) {
        List<TicketResponse> tickets = ticketService.getWaitingTicketsByQueue(queueId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/queue/{queueId}/served-today/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'COMPANY_ADMIN', 'CO_ADMIN')")
    public ResponseEntity<Long> getServedTodayCount(@PathVariable Long queueId) {
        long count = ticketService.getServedTodayCountByQueue(queueId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT', 'COMPANY_ADMIN')")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {

        log.info("📥 GET /api/tickets/{}", id);

        TicketResponse response = ticketService.getTicketById(id);

        log.info("✅ Ticket trouvé : ID {}", id);

        return ResponseEntity.ok(response);
    }

}
