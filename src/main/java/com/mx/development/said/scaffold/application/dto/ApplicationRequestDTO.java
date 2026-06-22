package com.mx.development.said.scaffold.application.dto;

import com.mx.development.said.scaffold.application.dto.AttributeDefinitionDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private String comments;

    private String notes;

    @NotBlank(message = "Version is required")
    private String version;

    @NotBlank(message = "Author is required")
    private String author;

    /**
     * ID de la Application raíz a la que pertenece este microservicio.
     * Si es null, se crea como Application raíz.
     */
    private Long parentId;
    private List<AttributeDefinitionDTO> attributes;
}
