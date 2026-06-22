package com.mx.development.said.scaffold.generator.controller;

import com.mx.development.said.scaffold.application.model.Application;
import com.mx.development.said.scaffold.application.repository.ApplicationRepository;
import com.mx.development.said.scaffold.generator.service.ProcessManagerService;
import com.mx.development.said.scaffold.generator.service.ProcessManagerService.ProcessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/processes")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessManagerService processManager;
    private final ApplicationRepository applicationRepository;

    @PostMapping("/{appId}/start")
    public ResponseEntity<ProcessStatus> start(
            @PathVariable Long appId,
            @RequestParam(required = false) String directory,
            @RequestParam(required = false) Integer port) {

        // If directory/port not provided, load from DB
        String resolvedDir = directory;
        int resolvedPort = port != null ? port : 8101;

        if (resolvedDir == null || resolvedDir.isBlank()) {
            Optional<Application> app = applicationRepository.findById(appId);
            if (app.isPresent()) {
                resolvedDir = app.get().getOutputDirectory();
                if (app.get().getAssignedPort() != null) resolvedPort = app.get().getAssignedPort();
            }
        }

        if (resolvedDir == null || resolvedDir.isBlank()) {
            return ResponseEntity.badRequest().body(
                new ProcessStatus("error", null, null, null));
        }

        log.info("POST /api/v1/processes/{}/start — dir:{} port:{}", appId, resolvedDir, resolvedPort);
        ProcessStatus status = processManager.start(appId, resolvedDir, resolvedPort);
        return ResponseEntity.ok(status);
    }

    @PostMapping("/{appId}/stop")
    public ResponseEntity<ProcessStatus> stop(@PathVariable Long appId) {
        log.info("POST /api/v1/processes/{}/stop", appId);
        ProcessStatus status = processManager.stop(appId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/{appId}/status")
    public ResponseEntity<ProcessStatus> status(@PathVariable Long appId) {
        return ResponseEntity.ok(processManager.status(appId));
    }

    @GetMapping
    public ResponseEntity<Map<Long, Integer>> listPorts() {
        return ResponseEntity.ok(processManager.getPorts());
    }

    @PostMapping("/{appId}/register")
    public ResponseEntity<Void> register(
            @PathVariable Long appId,
            @RequestParam String directory,
            @RequestParam int port) {
        processManager.registerProject(appId, directory, port);
        return ResponseEntity.ok().build();
    }
}
