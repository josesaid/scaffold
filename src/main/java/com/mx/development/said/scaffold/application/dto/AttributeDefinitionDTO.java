package com.mx.development.said.scaffold.application.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeDefinitionDTO {

    private String name;
    private String type;
    private List<String> validations;
    private boolean nullable;
    private boolean unique;
    private boolean pk;
    private Integer maxLen;
    private int sortOrder;
}
