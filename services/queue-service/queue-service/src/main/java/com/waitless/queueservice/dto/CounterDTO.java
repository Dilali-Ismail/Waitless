package com.waitless.queueservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounterDTO {

    private Long id;

    @NotNull(message = "Le numéro du guichet est obligatoire")
    @Min(value = 1, message = "Le numéro doit être >= 1")
    private Integer counterNumber;

    private Boolean isActive;

    @NotNull(message = "L'ID de la file est obligatoire")
    private Long queueId;

    private String queueName; // Nom de la queue (pour affichage)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
