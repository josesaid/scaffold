package com.mx.development.said.scaffold.generator.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratorResponse {

    private boolean success;
    private String projectName;
    private String outputDirectory;
    private String zipPath;
    private int assignedPort;
    private String message;
    private long durationMs;
}
