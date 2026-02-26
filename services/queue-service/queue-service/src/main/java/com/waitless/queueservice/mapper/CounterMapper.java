package com.waitless.queueservice.mapper;


import com.waitless.queueservice.dto.CounterDTO;
import com.waitless.queueservice.entity.Counter;
import org.mapstruct.Mapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CounterMapper {

    @Mapping(source = "queue.id", target = "queueId")
    @Mapping(source = "queue.name", target = "queueName")
    CounterDTO toDTO(Counter counter);

    @Mapping(target = "queue", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Counter toEntity(CounterDTO dto);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "queue", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(CounterDTO dto, @MappingTarget Counter counter);
}
