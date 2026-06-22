package com.mx.development.said.scaffold.application.mapper;

import com.mx.development.said.scaffold.application.dto.ApplicationRequestDTO;
import com.mx.development.said.scaffold.application.dto.ApplicationResponseDTO;
import com.mx.development.said.scaffold.application.model.Application;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApplicationMapper {

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "microservices", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    Application toEntity(ApplicationRequestDTO requestDTO);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "microservices", source = "microservices")
    @Mapping(target = "attributes", ignore = true)
    ApplicationResponseDTO toResponseDTO(Application application);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "version", source = "version")
    ApplicationResponseDTO.MicroserviceSummary toSummary(Application application);

    List<ApplicationResponseDTO.MicroserviceSummary> toSummaryList(List<Application> applications);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "microservices", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    void updateEntityFromDTO(ApplicationRequestDTO requestDTO, @MappingTarget Application application);
}
