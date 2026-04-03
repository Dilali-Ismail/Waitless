package com.waitless.ticket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTicketStatusRequest {

    @NotBlank(message = "Status est obligatoire")
    @Pattern(
            regexp = "COMPLETED|ABSENT|CANCELLED",
            message = "Status doit être COMPLETED, ABSENT ou CANCELLED"
    )
    private String status;

}
