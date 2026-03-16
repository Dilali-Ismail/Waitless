package com.waitless.ticket.service;


import com.waitless.ticket.client.EstimationClient;
import com.waitless.ticket.client.UserClient;
import com.waitless.ticket.dto.request.CallTicketRequest;
import com.waitless.ticket.dto.request.CreateTicketRequest;
import com.waitless.ticket.dto.response.EstimationResponse;
import com.waitless.ticket.dto.response.TicketResponse;
import com.waitless.ticket.dto.response.UserResponse;
import com.waitless.ticket.entity.Ticket;
import com.waitless.ticket.enums.TicketStatus;
import com.waitless.ticket.mapper.TicketMapper;
import com.waitless.ticket.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final EventPublisher eventPublisher;

    private final UserClient userClient;
    private final EstimationClient estimationClient;

    public TicketResponse creatTicket(CreateTicketRequest request) {

        UserResponse user = userClient.getUserById(request.getUserId());
        if ("SUSPENDED".equals(user.getStatus()) || "BANNED".equals(user.getStatus())) {
            throw new IllegalStateException("Création de ticket refusée : Utilisateur " + user.getStatus());
        }

        boolean hasActiveTicket = ticketRepository.existsByUserIdAndQueueIdAndStatus(
                request.getUserId(),
                request.getQueueId(),
                TicketStatus.WAITING
        );

        if(hasActiveTicket){
            throw new IllegalStateException("Vous avez déjà un ticket actif dans cette file d'attente");
        }

        long waitingCount = ticketRepository.countByQueueIdAndStatus(
                request.getQueueId(),
                TicketStatus.WAITING
        );



        int position = (int) (waitingCount + 1);
        EstimationResponse estimation = estimationClient.calculateEstimation(request.getQueueId(),position);

        Ticket ticket = Ticket.builder()
                .queueId(request.getQueueId())
                .userId(request.getUserId())
                .status(TicketStatus.WAITING)
                .position(position)
                .clientName(request.getClientName())
                .scoringPriority(0)
                .estimatedWaitTime(estimation.getEstimatedWaitMinutes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Ticket saveTivket = ticketRepository.save(ticket);

        eventPublisher.publishTicketCreated(saveTivket);

        TicketResponse response = ticketMapper.toResponse(saveTivket);

        return response;

    }

    public TicketResponse callNextTicket(CallTicketRequest request) {

            Ticket ticket = ticketRepository.findFirstByQueueIdAndStatusOrderByPositionAsc(request.getQueueId(), TicketStatus.WAITING)
                .orElseThrow(() -> new IllegalStateException("Aucun ticket en attente"));

        int previousPosition = ticket.getPosition();

        ticket.setStatus(TicketStatus.CALLED);
        ticket.setCalledAt(LocalDateTime.now());
        ticket.setCounterNumber(request.getCounterNumber());
        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket updatedTicket = ticketRepository.save(ticket);
        eventPublisher.publishTicketCalled(updatedTicket, previousPosition);
        recalculatePositions(ticket.getQueueId());
        return ticketMapper.toResponse(updatedTicket);
    }

    public TicketResponse completeTicket(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()-> new IllegalArgumentException("Ticket n'existe pas"));

        if(!ticket.getStatus().equals(TicketStatus.CALLED)){
            throw new IllegalArgumentException("ticket doit etre called");
        }

        ticket.setStatus(TicketStatus.COMPLETED);
        ticket.setCompletedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updatedTicket = ticketRepository.save(ticket);
        eventPublisher.publishTicketCompleted(updatedTicket);
        return ticketMapper.toResponse(updatedTicket);
    }

    public TicketResponse markTicketAbsent(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()-> new IllegalArgumentException("Ticket n'existe pas"));

        if(!ticket.getStatus().equals(TicketStatus.CALLED)){
            throw new IllegalArgumentException("ticket doit etre called");
        }

        ticket.setStatus(TicketStatus.ABSENT);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updatedTicket = ticketRepository.save(ticket);
        eventPublisher.publishTicketAbsent(updatedTicket);
        recalculatePositions(ticket.getQueueId());
        return ticketMapper.toResponse(updatedTicket);
    }

    public TicketResponse cancelTicket(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()-> new IllegalArgumentException("Ticket n'existe pas"));

        if(!ticket.getStatus().equals(TicketStatus.WAITING)){
            throw new IllegalArgumentException("ticket doit etre waiting");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updatedTicket = ticketRepository.save(ticket);
        eventPublisher.publishTicketCancelled(updatedTicket);
        recalculatePositions(ticket.getQueueId());
        return ticketMapper.toResponse(updatedTicket);
    }

    public List<TicketResponse> getUserTickets(String userId) {

        List<Ticket> tickets = ticketRepository.findByUserId(userId);

        return tickets.stream()
                .map(ticketMapper::toResponse)
                .collect(Collectors.toList());
    }

    public TicketResponse getTicketById(Long ticketId) {

    Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new IllegalArgumentException("Ticket non trouvé"));
        return ticketMapper.toResponse(ticket);
    }

    private void recalculatePositions(Long queueId){

        List<Ticket> waitingTickets = ticketRepository.findWaitingTicketsByQueueId(queueId);

        int newPosition = 1 ;

        for(Ticket ticket : waitingTickets){
            ticket.setPosition(newPosition);

            try {
                EstimationResponse estimation = estimationClient.calculateEstimation(queueId, newPosition);
                ticket.setEstimatedWaitTime(estimation.getEstimatedWaitMinutes());
            } catch (Exception e) {
                log.error("Erreur lors du recalcul de l'estimation pour le ticket {}: {}", ticket.getId(), e.getMessage());
            }

            ticket.setUpdatedAt(LocalDateTime.now());
            newPosition++;
        }
        ticketRepository.saveAll(waitingTickets);

    }


}
