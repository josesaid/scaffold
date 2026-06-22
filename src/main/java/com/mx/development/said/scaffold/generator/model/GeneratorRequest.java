package com.mx.development.said.scaffold.generator.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratorRequest {

    // ── Metadata del microservicio ──
    @NotBlank
    private String name;
    @NotBlank
    private String version;
    @NotBlank
    private String author;
    private String description;
    private String comments;
    private Long appId;       // DB id of the already-created Application record
    private Long parentId;

    // ── Configuración del proyecto ──
    @NotBlank
    private String springBootVersion;   // 3.4.1 | 3.3.7 | 3.2.12
    @NotBlank
    private String javaVersion;         // 21 | 17 | 11
    @NotBlank
    private String basePackage;         // com.mx.development.said
    @NotBlank
    private String packaging;           // jar | war
    @NotBlank
    private String configFormat;        // properties | yml
    @NotBlank
    private String architecture;        // modular | flat
    @NotBlank
    private String database;            // h2 | mysql | postgresql | mongodb

    // ── Dependencias opcionales ──
    private boolean includeSecurity;
    private boolean includeCache;
    private boolean includeActuator;
    private boolean includeSwagger;
    private boolean includeLombok;
    private boolean includeMapstruct;

    // ── Testing y docs ──
    private boolean generateUnitTests;
    private boolean generateIntegrationTests;
    private boolean generateDockerfile;
    private boolean generateReadme;
    private boolean generatePostmanCollection;

    // ── Kafka ──
    private boolean kafkaProducer;
    private boolean kafkaConsumer;
    private String kafkaTopic;

    // ── Atributos de la entidad ──
    @NotEmpty
    private List<AttributeDefinition> attributes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttributeDefinition {
        private String name;
        private String type;
        private List<String> validations;
        private boolean nullable;
        private boolean unique;
        private boolean pk;
        private Integer maxLen;
    }
}
