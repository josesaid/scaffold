package ${basePackage}.${entityNameLower}.service;

import ${basePackage}.${entityNameLower}.dto.${entityName}RequestDTO;
import ${basePackage}.${entityNameLower}.dto.${entityName}ResponseDTO;
import ${basePackage}.${entityNameLower}.exception.ResourceNotFoundException;
import ${basePackage}.${entityNameLower}.mapper.${entityName}Mapper;
import ${basePackage}.${entityNameLower}.model.${entityName};
import ${basePackage}.${entityNameLower}.repository.${entityName}Repository;
import ${basePackage}.${entityNameLower}.service.impl.${entityName}ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ${entityName}ServiceTest {

    @Mock
    private ${entityName}Repository ${entityNameLower}Repository;

    @Mock
    private ${entityName}Mapper ${entityNameLower}Mapper;

    @InjectMocks
    private ${entityName}ServiceImpl ${entityNameLower}Service;

    private ${entityName} entity;
    private ${entityName}RequestDTO requestDTO;
    private ${entityName}ResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        entity = new ${entityName}();
        entity.setId(1L);

        requestDTO = new ${entityName}RequestDTO();
        responseDTO = new ${entityName}ResponseDTO();
        responseDTO.setId(1L);
    }

    @Test
    void create_ShouldReturnResponseDTO_WhenValidRequest() {
        when(${entityNameLower}Mapper.toEntity(requestDTO)).thenReturn(entity);
        when(${entityNameLower}Repository.save(entity)).thenReturn(entity);
        when(${entityNameLower}Mapper.toResponseDTO(entity)).thenReturn(responseDTO);

        ${entityName}ResponseDTO result = ${entityNameLower}Service.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(${entityNameLower}Repository, times(1)).save(entity);
    }

    @Test
    void findById_ShouldReturnResponseDTO_WhenExists() {
        when(${entityNameLower}Repository.findById(1L)).thenReturn(Optional.of(entity));
        when(${entityNameLower}Mapper.toResponseDTO(entity)).thenReturn(responseDTO);

        ${entityName}ResponseDTO result = ${entityNameLower}Service.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_ShouldThrow_WhenNotFound() {
        when(${entityNameLower}Repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ${entityNameLower}Service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_ShouldReturnList() {
        when(${entityNameLower}Repository.findAll()).thenReturn(List.of(entity));
        when(${entityNameLower}Mapper.toResponseDTO(entity)).thenReturn(responseDTO);

        List<${entityName}ResponseDTO> results = ${entityNameLower}Service.findAll();

        assertThat(results).hasSize(1);
    }

    @Test
    void delete_ShouldDelete_WhenExists() {
        when(${entityNameLower}Repository.existsById(1L)).thenReturn(true);
        doNothing().when(${entityNameLower}Repository).deleteById(1L);

        assertThatCode(() -> ${entityNameLower}Service.delete(1L)).doesNotThrowAnyException();
        verify(${entityNameLower}Repository, times(1)).deleteById(1L);
    }

    @Test
    void delete_ShouldThrow_WhenNotFound() {
        when(${entityNameLower}Repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> ${entityNameLower}Service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
