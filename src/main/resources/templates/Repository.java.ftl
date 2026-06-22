package ${basePackage}.${entityNameLower}.repository;

import ${basePackage}.${entityNameLower}.model.${entityName};
<#if database == "mongodb">
import org.springframework.data.mongodb.repository.MongoRepository;
<#else>
import org.springframework.data.jpa.repository.JpaRepository;
</#if>
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
<#if database == "mongodb">
public interface ${entityName}Repository extends MongoRepository<${entityName}, Long> {
<#else>
public interface ${entityName}Repository extends JpaRepository<${entityName}, Long> {
</#if>

<#list attributes as attr>
<#if !attr.pk && (attr.type == "String") && attr.unique>
    Optional<${entityName}> findBy${attr.name?cap_first}(${attr.type} ${attr.name});

    boolean existsBy${attr.name?cap_first}(${attr.type} ${attr.name});

</#if>
</#list>
}
