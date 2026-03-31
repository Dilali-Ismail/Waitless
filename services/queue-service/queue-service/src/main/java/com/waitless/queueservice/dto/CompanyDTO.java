package com.waitless.queueservice.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDTO {

    private Long id;

    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String name;

    @NotBlank(message = "La catégorie est obligatoire")
    private String category;

    @Size(max = 200, message = "L'adresse ne peut pas dépasser 200 caractères")
    private String address;

    @Pattern(regexp = "^[0-9]{10}$", message = "Le numéro de téléphone doit contenir 10 chiffres")
    private String phoneNumber;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    /**
     * Requis au moment du register company (createCompany) pour créer le compte Keycloak.
     * Jamais renvoyé dans les réponses JSON.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String logoUrl;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
