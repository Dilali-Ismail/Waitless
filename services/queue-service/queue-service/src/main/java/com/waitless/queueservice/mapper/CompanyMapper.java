package com.waitless.queueservice.mapper;

import com.waitless.queueservice.dto.CompanyDTO;
import com.waitless.queueservice.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper{
    CompanyDTO toDto(Company company);

    @Mapping(target = "queues", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Company toEntity(CompanyDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "queues", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(CompanyDTO dto, @MappingTarget Company company);


}
