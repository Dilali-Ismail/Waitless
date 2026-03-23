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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private TicketMapper ticketMapper;
    @Mock private EventPublisher eventPublisher;
    @Mock private UserClient userClient;
    @Mock private EstimationClient estimationClient;

    @InjectMocks
    private TicketService ticketService;

    // ─── createTicket ──────────────────────────────────────────────────────────

    @Test
    void creatTicket_ShouldReturnTicketResponse_WhenValidRequest() {
        CreateTicketRequest request = CreateTicketRequest.builder()
                .userId("user123").queueId(1L).clientName("Test Client").build();

        UserResponse userResponse = UserResponse.builder()
                .userId("user123").status("ACTIVE").build();

        EstimationResponse estimationResponse = EstimationResponse.builder()
                .estimatedWaitMinutes(15).build();

        Ticket ticket = Ticket.builder()
                .id(1L).userId("user123").queueId(1L)
                .status(TicketStatus.WAITING).build();

        TicketResponse expectedResponse = TicketResponse.builder()
                .id(1L).userId("user123").status("WAITING").build();

        when(userClient.getUserById(request.getUserId())).thenReturn(userResponse);
        when(ticketRepository.existsByUserIdAndQueueIdAndStatus(anyString(), anyLong(), any())).thenReturn(false);
        when(ticketRepository.countByQueueIdAndStatus(anyLong(), any())).thenReturn(0L);
        when(estimationClient.calculateEstimation(anyLong(), anyInt())).thenReturn(estimationResponse);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(expectedResponse);

        TicketResponse result = ticketService.creatTicket(request);

        assertNotNull(result);
        assertEquals("WAITING", result.getStatus());
        verify(userClient).getUserById(request.getUserId());
        verify(ticketRepository).save(any(Ticket.class));
        verify(eventPublisher).publishTicketCreated(any(Ticket.class));
    }

    @Test
    void creatTicket_ShouldThrowException_WhenUserIsSuspended() {
        CreateTicketRequest request = CreateTicketRequest.builder()
                .userId("user123").build();

        UserResponse userResponse = UserResponse.builder()
                .userId("user123").status("SUSPENDED").build();

        when(userClient.getUserById(request.getUserId())).thenReturn(userResponse);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ticketService.creatTicket(request));
        assertTrue(exception.getMessage().contains("SUSPENDED"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void creatTicket_ShouldThrowException_WhenUserIsBanned() {
        CreateTicketRequest request = CreateTicketRequest.builder()
                .userId("bannedUser").build();

        UserResponse userResponse = UserResponse.builder()
                .userId("bannedUser").status("BANNED").build();

        when(userClient.getUserById(request.getUserId())).thenReturn(userResponse);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ticketService.creatTicket(request));
        assertTrue(exception.getMessage().contains("BANNED"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void creatTicket_ShouldThrowException_WhenUserAlreadyHasActiveTicket() {
        CreateTicketRequest request = CreateTicketRequest.builder()
                .userId("user123").queueId(1L).build();

        UserResponse userResponse = UserResponse.builder()
                .userId("user123").status("ACTIVE").build();

        when(userClient.getUserById(request.getUserId())).thenReturn(userResponse);
        when(ticketRepository.existsByUserIdAndQueueIdAndStatus(anyString(), anyLong(), any())).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ticketService.creatTicket(request));
        assertTrue(exception.getMessage().contains("ticket actif"));
        verify(ticketRepository, never()).save(any());
    }

    // ─── callNextTicket ────────────────────────────────────────────────────────

    @Test
    void callNextTicket_ShouldReturnTicketResponse_WhenTicketExists() {
        CallTicketRequest request = CallTicketRequest.builder()
                .queueId(1L).counterNumber(3).build();

        Ticket ticket = Ticket.builder()
                .id(1L).queueId(1L).userId("user1")
                .status(TicketStatus.WAITING).position(1).build();

        TicketResponse expectedResponse = TicketResponse.builder()
                .id(1L).status("CALLED").build();

        when(ticketRepository.findFirstByQueueIdAndStatusOrderByPositionAsc(1L, TicketStatus.WAITING))
                .thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(expectedResponse);
        when(ticketRepository.findWaitingTicketsByQueueId(1L)).thenReturn(List.of());

        TicketResponse result = ticketService.callNextTicket(request);

        assertNotNull(result);
        assertEquals("CALLED", result.getStatus());
        verify(eventPublisher).publishTicketCalled(any(Ticket.class), anyInt());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void callNextTicket_ShouldThrow_WhenNoWaitingTicket() {
        CallTicketRequest request = CallTicketRequest.builder()
                .queueId(99L).counterNumber(1).build();

        when(ticketRepository.findFirstByQueueIdAndStatusOrderByPositionAsc(99L, TicketStatus.WAITING))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> ticketService.callNextTicket(request));
        verify(ticketRepository, never()).save(any());
    }

    // ─── completeTicket ────────────────────────────────────────────────────────

    @Test
    void completeTicket_ShouldCompleteTicket_WhenStatusIsCalled() {
        Ticket ticket = Ticket.builder()
                .id(1L).status(TicketStatus.CALLED)
                .calledAt(LocalDateTime.now().minusMinutes(5)).build();

        TicketResponse expectedResponse = TicketResponse.builder()
                .id(1L).status("COMPLETED").build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(expectedResponse);

        TicketResponse result = ticketService.completeTicket(1L);

        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        verify(eventPublisher).publishTicketCompleted(any(Ticket.class));
    }

    @Test
    void completeTicket_ShouldThrow_WhenTicketNotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ticketService.completeTicket(99L));
        verify(eventPublisher, never()).publishTicketCompleted(any());
    }

    @Test
    void completeTicket_ShouldThrow_WhenStatusIsNotCalled() {
        Ticket ticket = Ticket.builder()
                .id(1L).status(TicketStatus.WAITING).build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(IllegalArgumentException.class, () -> ticketService.completeTicket(1L));
        verify(eventPublisher, never()).publishTicketCompleted(any());
    }

    // ─── markTicketAbsent ─────────────────────────────────────────────────────

    @Test
    void markTicketAbsent_ShouldMarkAbsent_WhenStatusIsCalled() {
        Ticket ticket = Ticket.builder()
                .id(1L).queueId(1L).status(TicketStatus.CALLED)
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .calledAt(LocalDateTime.now().minusMinutes(2)).build();

        TicketResponse expectedResponse = TicketResponse.builder()
                .id(1L).status("ABSENT").build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(expectedResponse);
        when(ticketRepository.findWaitingTicketsByQueueId(1L)).thenReturn(List.of());

        TicketResponse result = ticketService.markTicketAbsent(1L);

        assertNotNull(result);
        assertEquals("ABSENT", result.getStatus());
        verify(eventPublisher).publishTicketAbsent(any(Ticket.class));
    }

    @Test
    void markTicketAbsent_ShouldThrow_WhenTicketNotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ticketService.markTicketAbsent(99L));
    }

    @Test
    void markTicketAbsent_ShouldThrow_WhenStatusIsNotCalled() {
        Ticket ticket = Ticket.builder()
                .id(1L).status(TicketStatus.WAITING).build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(IllegalArgumentException.class, () -> ticketService.markTicketAbsent(1L));
    }

    // ─── cancelTicket ─────────────────────────────────────────────────────────

    @Test
    void cancelTicket_ShouldCancelTicket_WhenStatusIsWaiting() {
        Ticket ticket = Ticket.builder()
                .id(1L).queueId(1L).status(TicketStatus.WAITING)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .estimatedWaitTime(10).build();

        TicketResponse expectedResponse = TicketResponse.builder()
                .id(1L).status("CANCELLED").build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(expectedResponse);
        when(ticketRepository.findWaitingTicketsByQueueId(1L)).thenReturn(List.of());

        TicketResponse result = ticketService.cancelTicket(1L);

        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
        verify(eventPublisher).publishTicketCancelled(any(Ticket.class));
    }

    @Test
    void cancelTicket_ShouldThrow_WhenTicketNotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ticketService.cancelTicket(99L));
    }

    @Test
    void cancelTicket_ShouldThrow_WhenStatusIsNotWaiting() {
        Ticket ticket = Ticket.builder()
                .id(1L).status(TicketStatus.CALLED).build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(IllegalArgumentException.class, () -> ticketService.cancelTicket(1L));
    }

    // ─── getUserTickets ───────────────────────────────────────────────────────

    @Test
    void getUserTickets_ShouldReturnList_WhenUserHasTickets() {
        Ticket ticket1 = Ticket.builder().id(1L).userId("user123").build();
        Ticket ticket2 = Ticket.builder().id(2L).userId("user123").build();

        TicketResponse r1 = TicketResponse.builder().id(1L).userId("user123").build();
        TicketResponse r2 = TicketResponse.builder().id(2L).userId("user123").build();

        when(ticketRepository.findByUserId("user123")).thenReturn(List.of(ticket1, ticket2));
        when(ticketMapper.toResponse(ticket1)).thenReturn(r1);
        when(ticketMapper.toResponse(ticket2)).thenReturn(r2);

        List<TicketResponse> result = ticketService.getUserTickets("user123");

        assertEquals(2, result.size());
        verify(ticketRepository).findByUserId("user123");
    }

    @Test
    void getUserTickets_ShouldReturnEmptyList_WhenUserHasNoTickets() {
        when(ticketRepository.findByUserId("user999")).thenReturn(List.of());

        List<TicketResponse> result = ticketService.getUserTickets("user999");

        assertTrue(result.isEmpty());
    }

    // ─── getTicketById ────────────────────────────────────────────────────────

    @Test
    void getTicketById_ShouldReturnTicket_WhenFound() {
        Ticket ticket = Ticket.builder().id(1L).userId("user123").build();
        TicketResponse expectedResponse = TicketResponse.builder().id(1L).build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(expectedResponse);

        TicketResponse result = ticketService.getTicketById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getTicketById_ShouldThrow_WhenTicketNotFound() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ticketService.getTicketById(99L));
    }
}
