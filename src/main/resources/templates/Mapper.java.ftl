package ${basePackage}.${entityNameLower}.mapper;

import ${basePackage}.${entityNameLower}.dto.${entityName}RequestDTO;
import ${basePackage}.${entityNameLower}.dto.${entityName}ResponseDTO;
import ${basePackage}.${entityNameLower}.model.${entityName};
<#if includeMapstruct>
import org.mapstruct.*;
<#else>
import org.springframework.stereotype.Component;
</#if>
<#assign nonPkAttrs = attributes?filter(a -> !a.pk) />

<#if includeMapstruct>
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ${entityName}Mapper {

    ${entityName} toEntity(${entityName}RequestDTO requestDTO);

    ${entityName}ResponseDTO toResponseDTO(${entityName} entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(${entityName}RequestDTO requestDTO, @MappingTarget ${entityName} entity);
}
<#else>
@Component
public class ${entityName}Mapper {

    public ${entityName} toEntity(${entityName}RequestDTO dto) {
        if (dto == null) return null;
        ${entityName} entity = new ${entityName}();
<#list nonPkAttrs as attr>
<#if includeLombok && attr.type == "Boolean">
        entity.set${attr.name?cap_first}(dto.is${attr.name?cap_first}());
<#else>
        entity.set${attr.name?cap_first}(dto.get${attr.name?cap_first}());
</#if>
</#list>
        return entity;
    }

    public ${entityName}ResponseDTO toResponseDTO(${entityName} entity) {
        if (entity == null) return null;
        ${entityName}ResponseDTO dto = new ${entityName}ResponseDTO();
<#list attributes as attr>
<#if attr.pk>
        dto.setId(entity.get${attr.name?cap_first}());
<#elseif includeLombok && attr.type == "Boolean">
        dto.set${attr.name?cap_first}(entity.is${attr.name?cap_first}());
<#else>
        dto.set${attr.name?cap_first}(entity.get${attr.name?cap_first}());
</#if>
</#list>
        return dto;
    }

    public void updateEntityFromDTO(${entityName}RequestDTO dto, ${entityName} entity) {
        if (dto == null || entity == null) return;
<#list nonPkAttrs as attr>
<#if includeLombok && attr.type == "Boolean">
        entity.set${attr.name?cap_first}(dto.is${attr.name?cap_first}());
<#else>
        if (dto.get${attr.name?cap_first}() != null) entity.set${attr.name?cap_first}(dto.get${attr.name?cap_first}());
</#if>
</#list>
    }
}
</#if>
