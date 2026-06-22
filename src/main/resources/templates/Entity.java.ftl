package ${basePackage}.${entityNameLower}.model;

<#if database == "mongodb">
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
<#else>
import jakarta.persistence.*;
</#if>
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

<#if database == "mongodb">
@Document(collection = "${tableName}")
<#else>
@Entity
@Table(name = "${tableName}")
</#if>
<#if includeLombok>
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
</#if>
public class ${entityName} {

<#list attributes as attr>
<#if attr.pk>
    @Id
<#if database != "mongodb">
    @GeneratedValue(strategy = GenerationType.IDENTITY)
</#if>
    private ${attr.type} ${attr.name};

<#else>
<#if attr.validations?has_content>
<#list attr.validations as v>
<#if !(v == "@NotNull" && attr.validations?seq_contains("@NotBlank"))>
    ${v}
</#if>
</#list>
</#if>
<#if database != "mongodb">
<#assign colParts = []>
<#if !attr.nullable><#assign colParts = colParts + ["nullable = false"]></#if>
<#if attr.unique><#assign colParts = colParts + ["unique = true"]></#if>
<#if attr.maxLen?? && attr.type == "String"><#assign colParts = colParts + ["length = " + attr.maxLen]></#if>
<#if colParts?has_content>
    @Column(${colParts?join(", ")})
</#if>
</#if>
    private ${attr.type} ${attr.name};

</#if>
</#list>
<#if !includeLombok>
    public ${entityName}() {}

    public ${entityName}(<#list attributes as attr>${attr.type} ${attr.name}<#if attr?has_next>, </#if></#list>) {
<#list attributes as attr>
        this.${attr.name} = ${attr.name};
</#list>
    }

<#list attributes as attr>
    public ${attr.type} get${attr.name?cap_first}() { return ${attr.name}; }
    public void set${attr.name?cap_first}(${attr.type} ${attr.name}) { this.${attr.name} = ${attr.name}; }

</#list>
</#if>
}
