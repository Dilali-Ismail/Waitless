package com.waitless.ticket.repository;

import com.waitless.ticket.entity.Ticket;
import com.waitless.ticket.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUserId(String userId);
    List<Ticket> findByQueueIdAndStatus(Long queueId, TicketStatus status);
    boolean existsByUserIdAndQueueIdAndStatus(String userId, Long queueId, TicketStatus status);
    long countByQueueIdAndStatus(Long queueId, TicketStatus status);

    Optional<Ticket> findFirstByQueueIdAndStatusOrderByPositionAsc(Long queueId, TicketStatus status);

    @Query("SELECT t FROM Ticket t " +
            "WHERE t.queueId = :queueId " +
            "AND t.status = 'WAITING' " +
            "ORDER BY t.position ASC")
    List<Ticket> findWaitingTicketsByQueueId(@Param("queueId") Long queueId);

    long countByQueueIdAndStatusAndCompletedAtBetween(
            Long queueId,
            TicketStatus status,
            LocalDateTime start,
            LocalDateTime end
    );
}
