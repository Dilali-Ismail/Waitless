package com.waitless.ticket.dto.request;

import jakarta.validation.constraints.NotNull;

public class CallTicketRequest {

    @NotNull(message = "Queue ID est obligatoire")
    private Long queueId;

    @NotNull(message = "Counter number est obligatoire")
    private Integer counterNumber;
}
