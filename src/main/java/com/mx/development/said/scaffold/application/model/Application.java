package com.mx.development.said.scaffold.application.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @NotBlank(message = "Version is required")
    @Column(nullable = false)
    private String version;

    @NotBlank(message = "Author is required")
    @Column(nullable = false)
    private String author;

    @Column
    private String outputDirectory;

    @Column
    private Integer assignedPort;

    // --- Relación auto-referencial (dos niveles: raíz → microservicios) ---

    /**
     * Padre de este microservicio.
     * Si es null, esta Application ES la raíz (la aplicación contenedora).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Application parent;

    /**
     * Microservicios que pertenecen a esta Application raíz.
     * Solo la raíz (parent == null) tendrá elementos aquí.
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Application> microservices = new ArrayList<>();

    @OneToMany(mappedBy = "application", fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<ApplicationAttribute> attributes = new ArrayList<>();
}
