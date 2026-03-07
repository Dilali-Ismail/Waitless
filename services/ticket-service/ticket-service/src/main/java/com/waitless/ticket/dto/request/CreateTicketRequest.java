package com.waitless.ticket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTicketRequest {

    @NotNull(message = "Queue ID est obligatoire")
    private Long queueId;
    // ID de la file d'attente (vient de Queue Service)

    @NotBlank(message = "User ID est obligatoire")
    private String userId;
    // ID utilisateur Keycloak (extrait du JWT token)

    @NotBlank(message = "Nom du client est obligatoire")
    private String clientName;

}
