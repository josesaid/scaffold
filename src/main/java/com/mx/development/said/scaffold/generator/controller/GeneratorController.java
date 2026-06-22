package com.mx.development.said.scaffold.generator.controller;

import com.mx.development.said.scaffold.generator.model.GeneratorRequest;
import com.mx.development.said.scaffold.generator.model.GeneratorResponse;
import com.mx.development.said.scaffold.generator.service.impl.GeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/generator")
@RequiredArgsConstructor
public class GeneratorController {

    private final GeneratorService generatorService;

    @PostMapping("/generate")
    public ResponseEntity<GeneratorResponse> generate(@Valid @RequestBody GeneratorRequest request) {
        log.info("POST /api/v1/generator/generate — entity: {}", request.getName());
        GeneratorResponse response = generatorService.generate(request);
        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.internalServerError().body(response);
    }

    @GetMapping("/ports")
    public ResponseEntity<java.util.Map<String, String>> getPorts() {
        return ResponseEntity.ok(generatorService.getPortsUsed());
    }
}
