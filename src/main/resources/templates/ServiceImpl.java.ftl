package ${basePackage}.${entityNameLower}.service.impl;

import ${basePackage}.${entityNameLower}.dto.${entityName}RequestDTO;
import ${basePackage}.${entityNameLower}.dto.${entityName}ResponseDTO;
import ${basePackage}.${entityNameLower}.exception.ResourceNotFoundException;
import ${basePackage}.${entityNameLower}.mapper.${entityName}Mapper;
import ${basePackage}.${entityNameLower}.model.${entityName};
import ${basePackage}.${entityNameLower}.repository.${entityName}Repository;
import ${basePackage}.${entityNameLower}.service.${entityName}Service;
<#if includeLombok>
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
<#else>
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
</#if>
import org.springframework.stereotype.Service;
<#if database != "mongodb">
import org.springframework.transaction.annotation.Transactional;
</#if>

import java.util.List;

<#if includeLombok>
@Slf4j
</#if>
@Service
<#if includeLombok>
@RequiredArgsConstructor
</#if>
public class ${entityName}ServiceImpl implements ${entityName}Service {

<#if !includeLombok>
    private static final Logger log = LoggerFactory.getLogger(${entityName}ServiceImpl.class);

</#if>
    private final ${entityName}Repository ${entityNameLower}Repository;
    private final ${entityName}Mapper ${entityNameLower}Mapper;

<#if !includeLombok>
    public ${entityName}ServiceImpl(${entityName}Repository ${entityNameLower}Repository, ${entityName}Mapper ${entityNameLower}Mapper) {
        this.${entityNameLower}Repository = ${entityNameLower}Repository;
        this.${entityNameLower}Mapper = ${entityNameLower}Mapper;
    }

</#if>
    @Override
<#if database != "mongodb">
    @Transactional
</#if>
    public ${entityName}ResponseDTO create(${entityName}RequestDTO requestDTO) {
        log.debug("Creating ${entityNameLower}...");
        ${entityName} entity = ${entityNameLower}Mapper.toEntity(requestDTO);
        ${entityName} saved = ${entityNameLower}Repository.save(entity);
        return ${entityNameLower}Mapper.toResponseDTO(saved);
    }

    @Override
<#if database != "mongodb">
    @Transactional(readOnly = true)
</#if>
    public ${entityName}ResponseDTO findById(Long id) {
        log.debug("Finding ${entityNameLower} by id: {}", id);
        ${entityName} entity = ${entityNameLower}Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("${entityName} not found with id: " + id));
        return ${entityNameLower}Mapper.toResponseDTO(entity);
    }

    @Override
<#if database != "mongodb">
    @Transactional(readOnly = true)
</#if>
    public List<${entityName}ResponseDTO> findAll() {
        log.debug("Finding all ${entityNameLower?ends_with("s")?then(entityNameLower, entityNameLower + "s")}");
        return ${entityNameLower}Repository.findAll()
                .stream()
                .map(${entityNameLower}Mapper::toResponseDTO)
                .toList();
    }

    @Override
<#if database != "mongodb">
    @Transactional
</#if>
    public ${entityName}ResponseDTO update(Long id, ${entityName}RequestDTO requestDTO) {
        log.debug("Updating ${entityNameLower} id: {}", id);
        ${entityName} existing = ${entityNameLower}Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("${entityName} not found with id: " + id));
        ${entityNameLower}Mapper.updateEntityFromDTO(requestDTO, existing);
        return ${entityNameLower}Mapper.toResponseDTO(${entityNameLower}Repository.save(existing));
    }

    @Override
<#if database != "mongodb">
    @Transactional
</#if>
    public void delete(Long id) {
        log.debug("Deleting ${entityNameLower} id: {}", id);
        if (!${entityNameLower}Repository.existsById(id))
            throw new ResourceNotFoundException("${entityName} not found with id: " + id);
        ${entityNameLower}Repository.deleteById(id);
    }
}
