package ${basePackage}.${entityNameLower}.service;

import ${basePackage}.${entityNameLower}.dto.${entityName}RequestDTO;
import ${basePackage}.${entityNameLower}.dto.${entityName}ResponseDTO;

import java.util.List;

public interface ${entityName}Service {

    ${entityName}ResponseDTO create(${entityName}RequestDTO requestDTO);

    ${entityName}ResponseDTO findById(Long id);

    List<${entityName}ResponseDTO> findAll();

    ${entityName}ResponseDTO update(Long id, ${entityName}RequestDTO requestDTO);

    void delete(Long id);
}
