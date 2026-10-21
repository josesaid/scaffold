package com.mx.development.jose.said.scaffold.application.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "application_attributes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @ElementCollection
    @CollectionTable(name = "attribute_validations", joinColumns = @JoinColumn(name = "attribute_id"))
    @Column(name = "validation")
    private List<String> validations;

    @Column(name = "is_nullable", nullable = false)
    private boolean nullable = true;

    @Column(name = "is_unique", nullable = false)
    private boolean unique = false;

    @Column(name = "is_pk", nullable = false)
    private boolean pk = false;

    @Column
    private Integer maxLen;

    @Column(nullable = false)
    private int sortOrder = 0;
}
