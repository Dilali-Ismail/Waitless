package com.waitless.queueservice.mapper;

import com.waitless.queueservice.dto.CompanyDTO;
import com.waitless.queueservice.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper{
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "logoUrl", source = "logoUrl")
    CompanyDTO toDto(Company company);

    @Mapping(target = "queues", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "logoUrl", source = "logoUrl")
    Company toEntity(CompanyDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "queues", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "logoUrl", source = "logoUrl")
    void updateEntityFromDTO(CompanyDTO dto, @MappingTarget Company company);
}
