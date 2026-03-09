package com.waitless.ticket.mapper;

import com.waitless.ticket.dto.request.CreateTicketRequest;
import com.waitless.ticket.dto.response.TicketResponse;
import com.waitless.ticket.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    TicketResponse toResponse(Ticket ticket);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "estimatedWaitTime", ignore = true)
    @Mapping(target = "counterNumber", ignore = true)
    @Mapping(target = "scoringPriority", ignore = true)
    @Mapping(target = "calledAt", ignore = true)
    @Mapping(target = "servedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Ticket toEntity(CreateTicketRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromResponse(TicketResponse response, @MappingTarget Ticket ticket);

}
