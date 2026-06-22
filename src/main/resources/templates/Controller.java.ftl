package ${basePackage}.${entityNameLower}.controller;

import ${basePackage}.${entityNameLower}.dto.${entityName}RequestDTO;
import ${basePackage}.${entityNameLower}.dto.${entityName}ResponseDTO;
import ${basePackage}.${entityNameLower}.service.${entityName}Service;
import jakarta.validation.Valid;
<#if includeLombok>
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
<#else>
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
</#if>
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

<#if includeLombok>
@Slf4j
</#if>
@RestController
@RequestMapping("/api/v1/${entityNameLower?ends_with("s")?then(entityNameLower, entityNameLower + "s")}")
<#if includeLombok>
@RequiredArgsConstructor
</#if>
public class ${entityName}Controller {

<#if !includeLombok>
    private static final Logger log = LoggerFactory.getLogger(${entityName}Controller.class);

</#if>
    private final ${entityName}Service ${entityNameLower}Service;

<#if !includeLombok>
    public ${entityName}Controller(${entityName}Service ${entityNameLower}Service) {
        this.${entityNameLower}Service = ${entityNameLower}Service;
    }

</#if>
    @PostMapping
    public ResponseEntity<${entityName}ResponseDTO> create(@Valid @RequestBody ${entityName}RequestDTO requestDTO) {
        log.info("POST /api/v1/${entityNameLower?ends_with("s")?then(entityNameLower, entityNameLower + "s")}");
        return ResponseEntity.status(HttpStatus.CREATED).body(${entityNameLower}Service.create(requestDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<${entityName}ResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(${entityNameLower}Service.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<${entityName}ResponseDTO>> findAll() {
        return ResponseEntity.ok(${entityNameLower}Service.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<${entityName}ResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ${entityName}RequestDTO requestDTO) {
        return ResponseEntity.ok(${entityNameLower}Service.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ${entityNameLower}Service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
