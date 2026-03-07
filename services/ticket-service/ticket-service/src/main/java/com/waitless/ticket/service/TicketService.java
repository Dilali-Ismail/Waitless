package com.waitless.ticket.service;


import com.waitless.ticket.dto.request.CallTicketRequest;
import com.waitless.ticket.dto.request.CreateTicketRequest;
import com.waitless.ticket.dto.response.TicketResponse;
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

    public TicketResponse creatTicket(CreateTicketRequest request) throws IllegalAccessException {

        boolean hasActiveTicket = ticketRepository.existsByUserIdAndQueueIdAndStatus(
                request.getUserId(),
                request.getQueueId(),
                TicketStatus.WAITING
        );

        if(hasActiveTicket){
            throw new IllegalAccessException("Vous avez déjà un ticket actif dans cette file d'attente");
        }

        long waitingCount = ticketRepository.countByQueueIdAndStatus(
                request.getQueueId(),
                TicketStatus.WAITING
        );

        int position = (int) (waitingCount + 1);

        Ticket ticket = Ticket.builder()
                .queueId(request.getQueueId())
                .userId(request.getUserId())
                .status("WAITING")
                .position(position)
                .clientName(request.getClientName())
                .scoringPriority(0)
                .estimatedWaitTime(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Ticket saveTivket = ticketRepository.save(ticket);

        eventPublisher.publishTicketCreated(saveTivket);

        TicketResponse response = ticketMapper.toResponse(saveTivket);

        return response;

    }

    public TicketResponse callNextTicket(CallTicketRequest request) throws IllegalAccessException {

        Ticket ticket = ticketRepository.findNextTicketInQueue (request.getQueueId())
                .orElseThrow(()-> {
                    return new IllegalAccessException("Auccun ticket en attente");
                });

        int previousPosition = ticket.getPosition();

        ticket.setStatus("CALLED");
        ticket.setCalledAt(LocalDateTime.now());
        ticket.setCounterNumber(request.getCounterNumber());
        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket updatedTicket = ticketRepository.save(ticket);
        eventPublisher.publishTicketCalled(updatedTicket,previousPosition);
        return ticketMapper.toResponse(updatedTicket);
    }

    public TicketResponse completeTicket(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(()-> new IllegalArgumentException("Ticket n'existe pas"));

        if(!ticket.getStatus().equals(TicketStatus.CALLED)){
            throw new IllegalArgumentException("ticket doit etre called");
        }

        ticket.setStatus("COMPLETED");
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

        ticket.setStatus("ABSENT");
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

        ticket.setStatus("CANCELLED");
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
            ticket.setUpdatedAt(LocalDateTime.now());
            newPosition++;
        }
        ticketRepository.saveAll(waitingTickets);

    }
}
