package com.waitless.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_restrictions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    /**
     * Type de restriction appliquée.
     *
     * Valeurs possibles:
     * - WARNING: Annulation tardive (< 30min avant)
     * - NO_SHOW: Annulation après heure prévue
     * - SUSPENDED: Compte suspendu 24h
     * - BANNED: Compte banni définitivement
     */

    @Column(name = "restriction_type", nullable = false, length = 20)
    private String restrictionType;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @PrePersist
    protected void onCreate() {
        this.appliedAt = LocalDateTime.now();
    }

}



