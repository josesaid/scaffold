package com.mx.development.jose.said.scaffold.application.service.impl;

import com.mx.development.jose.said.scaffold.application.dto.ApplicationRequestDTO;
import com.mx.development.jose.said.scaffold.application.dto.ApplicationResponseDTO;
import com.mx.development.jose.said.scaffold.application.dto.AttributeDefinitionDTO;
import com.mx.development.jose.said.scaffold.application.exception.ResourceNotFoundException;
import com.mx.development.jose.said.scaffold.application.mapper.ApplicationMapper;
import com.mx.development.jose.said.scaffold.application.model.Application;
import com.mx.development.jose.said.scaffold.application.model.ApplicationAttribute;
import com.mx.development.jose.said.scaffold.application.repository.ApplicationAttributeRepository;
import com.mx.development.jose.said.scaffold.application.repository.ApplicationRepository;
import com.mx.development.jose.said.scaffold.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationAttributeRepository attributeRepository;
    private final ApplicationMapper applicationMapper;

    @Override
    @Transactional
    public ApplicationResponseDTO create(ApplicationRequestDTO requestDTO) {
        log.debug("Creating application: {}", requestDTO.getName());

        Application application = applicationMapper.toEntity(requestDTO);

        // Handle parent
        if (requestDTO.getParentId() != null) {
            Application parent = applicationRepository.findById(requestDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent application not found with id: " + requestDTO.getParentId()));
            if (parent.getParent() != null) {
                throw new IllegalArgumentException("Cannot add a microservice to another microservice.");
            }
            application.setParent(parent);
        }

        // saveAndFlush ensures ID is generated before saving attributes
        Application saved = applicationRepository.saveAndFlush(application);

        // Persist attributes using a fresh reference with confirmed ID
        if (requestDTO.getAttributes() != null && !requestDTO.getAttributes().isEmpty()) {
            final Application appRef = applicationRepository.getReferenceById(saved.getId());
            List<ApplicationAttribute> attrEntities = toAttributeEntities(requestDTO.getAttributes(), appRef);
            attributeRepository.saveAllAndFlush(attrEntities);
        }

        log.debug("Application created with id: {}", saved.getId());
        ApplicationResponseDTO response = applicationMapper.toResponseDTO(saved);
        response.setAttributes(toAttributeDTOs(attributeRepository.findByApplicationIdOrderBySortOrderAsc(saved.getId())));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponseDTO findById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        ApplicationResponseDTO response = applicationMapper.toResponseDTO(application);
        response.setAttributes(toAttributeDTOs(attributeRepository.findByApplicationIdOrderBySortOrderAsc(id)));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> findAll() {
        return applicationRepository.findAll().stream().map(app -> {
            ApplicationResponseDTO dto = applicationMapper.toResponseDTO(app);
            dto.setAttributes(toAttributeDTOs(attributeRepository.findByApplicationIdOrderBySortOrderAsc(app.getId())));
            return dto;
        }).toList();
    }

    @Override
    @Transactional
    public ApplicationResponseDTO update(Long id, ApplicationRequestDTO requestDTO) {
        Application existing = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        applicationMapper.updateEntityFromDTO(requestDTO, existing);
        Application updated = applicationRepository.save(existing);

        // Update attributes if provided
        if (requestDTO.getAttributes() != null) {
            attributeRepository.deleteByApplicationId(id);
            List<ApplicationAttribute> attrs = toAttributeEntities(requestDTO.getAttributes(), updated);
            attributeRepository.saveAll(attrs);
        }

        ApplicationResponseDTO response = applicationMapper.toResponseDTO(updated);
        response.setAttributes(toAttributeDTOs(attributeRepository.findByApplicationIdOrderBySortOrderAsc(id)));
        return response;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Application not found with id: " + id);
        }
        attributeRepository.deleteByApplicationId(id);
        applicationRepository.deleteById(id);
    }

    // ── Helpers ──

    private List<ApplicationAttribute> toAttributeEntities(List<AttributeDefinitionDTO> dtos, Application app) {
        return IntStream.range(0, dtos.size()).mapToObj(i -> {
            AttributeDefinitionDTO dto = dtos.get(i);
            return ApplicationAttribute.builder()
                    .application(app)
                    .name(dto.getName())
                    .type(dto.getType())
                    .validations(dto.getValidations() != null ? dto.getValidations() : new ArrayList<>())
                    .nullable(dto.isNullable())
                    .unique(dto.isUnique())
                    .pk(dto.isPk())
                    .maxLen(dto.getMaxLen())
                    .sortOrder(i)
                    .build();
        }).toList();
    }

    private List<AttributeDefinitionDTO> toAttributeDTOs(List<ApplicationAttribute> entities) {
        if (entities == null) return new ArrayList<>();
        return entities.stream().map(e -> AttributeDefinitionDTO.builder()
                .name(e.getName())
                .type(e.getType())
                .validations(new ArrayList<>(e.getValidations() != null ? e.getValidations() : List.of()))
                .nullable(e.isNullable())
                .unique(e.isUnique())
                .pk(e.isPk())
                .maxLen(e.getMaxLen())
                .sortOrder(e.getSortOrder())
                .build()).toList();
    }
}
