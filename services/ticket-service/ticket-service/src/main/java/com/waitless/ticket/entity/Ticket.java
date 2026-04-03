package com.waitless.ticket.entity;


import com.waitless.ticket.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_queue_id", columnList = "queue_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_queue_status", columnList = "queue_id,status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "queue_id", nullable = false)
    private Long queueId;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "client_name", nullable = false, length = 100)
    private String clientName;

    @Column(nullable = false)
    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.WAITING;

    @Column(name = "scoring_priority")
    @Builder.Default
    private Integer scoringPriority = 0;

    @Column(name = "estimated_wait_time")
    private Integer estimatedWaitTime;

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "served_at")
    private LocalDateTime servedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "counter_number")
    private Integer counterNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;



}
