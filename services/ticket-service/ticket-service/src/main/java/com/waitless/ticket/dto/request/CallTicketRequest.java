package com.waitless.ticket.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CallTicketRequest {

    @NotNull(message = "Queue ID est obligatoire")
    private Long queueId;

    @NotNull(message = "Counter number est obligatoire")
    private Integer counterNumber;
}
