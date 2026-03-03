package com.waitless.queueservice.mapper;




import com.waitless.queueservice.dto.QueueDTO;
import com.waitless.queueservice.entity.Queue;

import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QueueMapper {
    @Mapping(source = "company.id", target = "companyId")
    @Mapping(source = "company.name", target = "companyName")
    QueueDTO toDTO(Queue queue);

    // DTO → Entity
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "counters", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Queue toEntity(QueueDTO dto);

    // Update Entity from DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "counters", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(QueueDTO dto, @MappingTarget Queue queue);
}
