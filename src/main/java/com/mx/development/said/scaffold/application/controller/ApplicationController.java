package com.mx.development.said.scaffold.application.controller;

import com.mx.development.said.scaffold.application.dto.ApplicationRequestDTO;
import com.mx.development.said.scaffold.application.dto.ApplicationResponseDTO;
import com.mx.development.said.scaffold.application.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponseDTO> create(@Valid @RequestBody ApplicationRequestDTO requestDTO) {
        log.info("POST /api/v1/applications");
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.create(requestDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> findById(@PathVariable Long id) {
        log.info("GET /api/v1/applications/{}", id);
        return ResponseEntity.ok(applicationService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponseDTO>> findAll() {
        log.info("GET /api/v1/applications");
        return ResponseEntity.ok(applicationService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationRequestDTO requestDTO) {
        log.info("PUT /api/v1/applications/{}", id);
        return ResponseEntity.ok(applicationService.update(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/v1/applications/{}", id);
        applicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
