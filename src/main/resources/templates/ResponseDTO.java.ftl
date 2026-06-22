package ${basePackage}.${entityNameLower}.dto;

<#if includeLombok>
import lombok.*;
</#if>
<#if hasLocalDate>
import java.time.LocalDate;
</#if>
<#if hasLocalDateTime>
import java.time.LocalDateTime;
</#if>
<#if hasBigDecimal>
import java.math.BigDecimal;
</#if>
<#if hasUUID>
import java.util.UUID;
</#if>

<#if includeLombok>
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
</#if>
public class ${entityName}ResponseDTO {

    private Long id;
<#list attributes as attr>
<#if !attr.pk>
    private ${attr.type} ${attr.name};
</#if>
</#list>

<#if !includeLombok>
    public ${entityName}ResponseDTO() {}

    public ${entityName}ResponseDTO(Long id<#list attributes as attr><#if !attr.pk>, ${attr.type} ${attr.name}</#if></#list>) {
        this.id = id;
<#list attributes as attr>
<#if !attr.pk>
        this.${attr.name} = ${attr.name};
</#if>
</#list>
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

<#list attributes as attr>
<#if !attr.pk>
    public ${attr.type} get${attr.name?cap_first}() { return ${attr.name}; }
    public void set${attr.name?cap_first}(${attr.type} ${attr.name}) { this.${attr.name} = ${attr.name}; }

</#if>
</#list>
</#if>
}
