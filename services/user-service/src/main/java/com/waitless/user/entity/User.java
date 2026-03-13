package com.waitless.user.entity;

import com.waitless.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 36)
    private String userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private Double score = 100.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "suspension_end_date")
    private LocalDateTime suspensionEndDate;

    @Column(name = "tickets_created", nullable = false)
    @Builder.Default
    private Integer ticketsCreated = 0;

    @Column(name = "tickets_served", nullable = false)
    @Builder.Default
    private Integer ticketsServed = 0;

    @Column(name = "tickets_cancelled", nullable = false)
    @Builder.Default
    private Integer ticketsCancelled = 0;

    @Column(name = "warning_count", nullable = false)
    @Builder.Default
    private Integer warningCount = 0;

    @Column(name = "no_show_count", nullable = false)
    @Builder.Default
    private Integer noShowCount = 0;

    @Column(name = "suspension_count", nullable = false)
    @Builder.Default
    private Integer suspensionCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canCreateTicket() {
        // Si SUSPENDED, vérifier si la suspension est expirée
        if (this.status == UserStatus.SUSPENDED && this.suspensionEndDate != null) {
            if (LocalDateTime.now().isAfter(this.suspensionEndDate)) {
                // Suspension expirée, mais ne pas modifier ici (principe CQS)
                // Le RestrictionService fera la mise à jour
                return true;
            }
            return false;
        }

        return this.status == UserStatus.ACTIVE;
    }

    public boolean isSuspended() {
        if (this.status != UserStatus.SUSPENDED) {
            return false;
        }

        if (this.suspensionEndDate == null) {
            return true;  // Suspension sans fin définie (anomalie)
        }

        return LocalDateTime.now().isBefore(this.suspensionEndDate);
    }
}
