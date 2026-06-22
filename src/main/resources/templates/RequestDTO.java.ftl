package ${basePackage}.${entityNameLower}.dto;

import jakarta.validation.constraints.*;
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
<#assign nonPkAttrs = attributes?filter(a -> !a.pk) />
<#if includeLombok>
@Getter
@Setter
@NoArgsConstructor
<#if nonPkAttrs?has_content>
@AllArgsConstructor
</#if>
</#if>
public class ${entityName}RequestDTO {

<#list nonPkAttrs as attr>
<#if attr.validations?has_content>
<#list attr.validations as v>
<#if !(v == "@NotNull" && attr.validations?seq_contains("@NotBlank"))>
    ${v}
</#if>
</#list>
</#if>
    private ${attr.type} ${attr.name};

</#list>
<#if !includeLombok>
    public ${entityName}RequestDTO() {}

<#if nonPkAttrs?has_content>
    public ${entityName}RequestDTO(<#list nonPkAttrs as attr>${attr.type} ${attr.name}<#if attr?has_next>, </#if></#list>) {
<#list nonPkAttrs as attr>
        this.${attr.name} = ${attr.name};
</#list>
    }

</#if>
<#list nonPkAttrs as attr>
    public ${attr.type} get${attr.name?cap_first}() { return ${attr.name}; }
    public void set${attr.name?cap_first}(${attr.type} ${attr.name}) { this.${attr.name} = ${attr.name}; }

</#list>
</#if>
}
