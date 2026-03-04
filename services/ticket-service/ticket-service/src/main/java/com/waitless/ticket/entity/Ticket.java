package com.waitless.ticket.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_queue_id", columnList = "queue_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_queue_status", columnList = "queue_id,status")
})
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

    @Column(name = "client_name", nullable = false, length = 100)
    private String clientName;

    @Column(name = "client_phone", length = 20)
    private String clientPhone;

    @Column(name = "client_email", length = 100)
    private String clientEmail;

    @Column(nullable = false)
    private Integer position;
    // Position dans la file (1 = premier, 2 = deuxième, etc.)

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "WAITING";
    // WAITING, CALLED, SERVING, SERVED, CANCELLED, NO_SHOW

    @Column(name = "scoring_priority")
    @Builder.Default
    private Integer scoringPriority = 0;

    @Column(name = "called_at")
    private LocalDateTime calledAt;
    // Quand le client a été appelé

    @Column(name = "served_at")
    private LocalDateTime servedAt;
    // Quand le service a commencé

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    // Quand le service est terminé

    @Column(name = "counter_number")
    private Integer counterNumber;



}
