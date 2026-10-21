package com.mx.development.jose.said.scaffold.generator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ProcessManagerService {

    // Map: applicationId → running Process
    private final Map<Long, Process> processes = new ConcurrentHashMap<>();
    // Map: applicationId → assigned port
    private final Map<Long, Integer> ports = new ConcurrentHashMap<>();
    // Map: applicationId → output directory
    private final Map<Long, String> directories = new ConcurrentHashMap<>();

    public record ProcessStatus(String state, Integer port, String directory, String pid) {}

    /**
     * Start a Spring Boot project via: mvn spring-boot:run
     */
    public ProcessStatus start(Long appId, String projectDirectory, int port) {
        if (processes.containsKey(appId)) {
            Process existing = processes.get(appId);
            if (existing.isAlive()) {
                log.warn("Process for app {} is already running (pid:{})", appId, existing.pid());
                return new ProcessStatus("running", port, projectDirectory, String.valueOf(existing.pid()));
            }
        }

        Path dir = Paths.get(projectDirectory);
        if (!Files.exists(dir)) {
            log.error("Project directory not found: {}", projectDirectory);
            return new ProcessStatus("error", null, projectDirectory, null);
        }

        try {
            // Fix 14: Use start script if available, fallback to mvn
            java.nio.file.Path scriptPath = dir.resolve("start_" + dir.getFileName().toString().toLowerCase() + ".sh");
            ProcessBuilder pb;
            if (java.nio.file.Files.exists(scriptPath)) {
                pb = new ProcessBuilder("/bin/bash", scriptPath.toAbsolutePath().toString())
                        .directory(dir.toFile())
                        .redirectErrorStream(true);
                log.info("Using start script: {}", scriptPath.getFileName());
            } else {
                pb = new ProcessBuilder("mvn", "spring-boot:run",
                        "-Dspring-boot.run.arguments=--server.port=" + port)
                        .directory(dir.toFile())
                        .redirectErrorStream(true);
                log.info("No start script found, using mvn spring-boot:run");
            }

            // Redirect output to logs/app.log
            java.nio.file.Path logsDir = dir.resolve("logs");
            java.nio.file.Files.createDirectories(logsDir);
            pb.redirectOutput(logsDir.resolve("app.log").toFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            processes.put(appId, process);
            ports.put(appId, port);
            directories.put(appId, projectDirectory);

            // Log output in background thread
            Thread logThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("[app-{}] {}", appId, line);
                    }
                } catch (IOException e) {
                    log.debug("Log stream closed for app {}", appId);
                }
            });
            logThread.setDaemon(true);
            logThread.start();

            log.info("Started process for app {} (pid:{}) on port {}", appId, process.pid(), port);
            return new ProcessStatus("starting", port, projectDirectory, String.valueOf(process.pid()));

        } catch (IOException e) {
            log.error("Failed to start process for app {}: {}", appId, e.getMessage());
            return new ProcessStatus("error", null, projectDirectory, null);
        }
    }

    /**
     * Stop a running Spring Boot process
     */
    public ProcessStatus stop(Long appId) {
        Process process = processes.get(appId);
        if (process == null || !process.isAlive()) {
            processes.remove(appId);
            return new ProcessStatus("stopped", ports.get(appId), directories.get(appId), null);
        }

        process.descendants().forEach(ProcessHandle::destroy);
        process.destroy();

        try {
            boolean exited = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            if (!exited) {
                process.destroyForcibly();
                log.warn("Force-killed process for app {}", appId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        processes.remove(appId);
        log.info("Stopped process for app {}", appId);
        return new ProcessStatus("stopped", ports.get(appId), directories.get(appId), null);
    }

    /**
     * Get current status of a process
     */
    public ProcessStatus status(Long appId) {
        Process process = processes.get(appId);
        Integer port = ports.get(appId);
        String dir = directories.get(appId);

        if (process == null) return new ProcessStatus("stopped", port, dir, null);
        if (process.isAlive()) return new ProcessStatus("running", port, dir, String.valueOf(process.pid()));
        processes.remove(appId);
        return new ProcessStatus("stopped", port, dir, null);
    }

    /**
     * Register known project info (from generator output)
     */
    public void registerProject(Long appId, String directory, int port) {
        directories.put(appId, directory);
        ports.put(appId, port);
    }

    public Map<Long, Process> getProcesses() { return processes; }
    public Map<Long, Integer> getPorts() { return ports; }
    public Map<Long, String> getDirectories() { return directories; }
}
