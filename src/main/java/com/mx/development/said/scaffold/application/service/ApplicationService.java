package com.mx.development.said.scaffold.application.service;

import com.mx.development.said.scaffold.application.dto.ApplicationRequestDTO;
import com.mx.development.said.scaffold.application.dto.ApplicationResponseDTO;

import java.util.List;

public interface ApplicationService {

    ApplicationResponseDTO create(ApplicationRequestDTO requestDTO);

    ApplicationResponseDTO findById(Long id);

    List<ApplicationResponseDTO> findAll();

    ApplicationResponseDTO update(Long id, ApplicationRequestDTO requestDTO);

    void delete(Long id);
}
