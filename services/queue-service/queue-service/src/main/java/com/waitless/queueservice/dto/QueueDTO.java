package com.waitless.queueservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueDTO {

    public interface OnCreate {}
    public interface OnUpdate {}

    private Long id;

    @NotBlank(message = "Le nom de la file est obligatoire", groups = OnCreate.class)
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères", groups = {OnCreate.class, OnUpdate.class})
    private String name;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères", groups = {OnCreate.class, OnUpdate.class})
    private String description;

    @NotNull(message = "La capacité est obligatoire", groups = OnCreate.class)
    @Min(value = 10, message = "La capacité minimum est de 10", groups = {OnCreate.class, OnUpdate.class})
    @Max(value = 500, message = "La capacité maximum est de 500", groups = {OnCreate.class, OnUpdate.class})
    private Integer capacity;

    @NotNull(message = "Le temps moyen de service est obligatoire", groups = OnCreate.class)
    @Min(value = 1, message = "Le temps minimum est de 1 minute", groups = {OnCreate.class, OnUpdate.class})
    @Max(value = 60, message = "Le temps maximum est de 60 minutes", groups = {OnCreate.class, OnUpdate.class})
    private Integer averageServiceTime;

    private Boolean isActive;

    private LocalTime openingTime; // ex: 08:00
    private LocalTime closingTime; // ex: 17:00

    @NotNull(message = "L'ID de l'entreprise est obligatoire", groups = OnCreate.class)
    private Long companyId;

    private String companyName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
