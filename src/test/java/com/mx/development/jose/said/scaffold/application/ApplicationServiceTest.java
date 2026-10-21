package com.mx.development.jose.said.scaffold.application;

import com.mx.development.jose.said.scaffold.application.dto.ApplicationRequestDTO;
import com.mx.development.jose.said.scaffold.application.dto.ApplicationResponseDTO;
import com.mx.development.jose.said.scaffold.application.exception.ResourceNotFoundException;
import com.mx.development.jose.said.scaffold.application.mapper.ApplicationMapper;
import com.mx.development.jose.said.scaffold.application.model.Application;
import com.mx.development.jose.said.scaffold.application.repository.ApplicationAttributeRepository;
import com.mx.development.jose.said.scaffold.application.repository.ApplicationRepository;
import com.mx.development.jose.said.scaffold.application.service.impl.ApplicationServiceImpl;
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
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationAttributeRepository attributeRepository;

    @Mock
    private ApplicationMapper applicationMapper;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private Application application;
    private ApplicationRequestDTO requestDTO;
    private ApplicationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        application = Application.builder()
                .id(1L).name("Test App").version("1.0.0").author("Said").build();

        requestDTO = ApplicationRequestDTO.builder()
                .name("Test App").version("1.0.0").author("Said").build();

        responseDTO = ApplicationResponseDTO.builder()
                .id(1L).name("Test App").version("1.0.0").author("Said").build();
    }

    @Test
    void create_ShouldReturnResponseDTO_WhenValidRequest() {
        when(applicationMapper.toEntity(requestDTO)).thenReturn(application);
        when(applicationRepository.saveAndFlush(application)).thenReturn(application);
        when(applicationMapper.toResponseDTO(application)).thenReturn(responseDTO);

        ApplicationResponseDTO result = applicationService.create(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test App");
        verify(applicationRepository, times(1)).saveAndFlush(application);
    }

    @Test
    void findById_ShouldReturnResponseDTO_WhenExists() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationMapper.toResponseDTO(application)).thenReturn(responseDTO);
        when(attributeRepository.findByApplicationIdOrderBySortOrderAsc(1L)).thenReturn(List.of());

        ApplicationResponseDTO result = applicationService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_ShouldReturnList() {
        when(applicationRepository.findAll()).thenReturn(List.of(application));
        when(applicationMapper.toResponseDTO(application)).thenReturn(responseDTO);
        when(attributeRepository.findByApplicationIdOrderBySortOrderAsc(1L)).thenReturn(List.of());

        List<ApplicationResponseDTO> results = applicationService.findAll();

        assertThat(results).hasSize(1);
    }

    @Test
    void delete_ShouldDelete_WhenExists() {
        when(applicationRepository.existsById(1L)).thenReturn(true);
        doNothing().when(applicationRepository).deleteById(1L);
        doNothing().when(attributeRepository).deleteByApplicationId(1L);

        assertThatCode(() -> applicationService.delete(1L)).doesNotThrowAnyException();
        verify(applicationRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_ShouldThrowException_WhenNotFound() {
        when(applicationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> applicationService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
