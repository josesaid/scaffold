package com.mx.development.said.scaffold.application.dto;

import com.mx.development.said.scaffold.application.dto.AttributeDefinitionDTO;
import lombok.*;
import java.util.List;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String comments;
    private String notes;
    private String version;
    private String author;

    /**
     * ID del padre. Null si esta Application es la raíz.
     */
    private Long parentId;
    private String outputDirectory;
    private Integer assignedPort;
    private List<AttributeDefinitionDTO> attributes;

    /**
     * Lista resumida de microservicios hijos (solo id + name).
     * Poblada únicamente cuando esta Application es la raíz.
     */
    private List<MicroserviceSummary> microservices;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MicroserviceSummary {
        private Long id;
        private String name;
        private String version;
    }
}
