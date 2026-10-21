package com.mx.development.jose.said.scaffold.generator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.*;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

@Slf4j
@Service
public class PortRegistryService {

    private static final int BASE_PORT = 8101;
    private final Path registryPath;

    public PortRegistryService() {
        // Place ports-used.properties at the parent of the scaffold project directory
        String userDir = System.getProperty("user.dir");
        Path parentDir = Paths.get(userDir).getParent();
        if (parentDir == null) parentDir = Paths.get(userDir);
        this.registryPath = parentDir.resolve("ports-used.properties");
        initRegistry();
    }

    private void initRegistry() {
        try {
            // Always reset on startup so stale ports from previous runs are cleared
            Properties props = new Properties();
            props.setProperty("scaffold", "8080");
            try (OutputStream out = Files.newOutputStream(registryPath,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                props.store(out, "Scaffold Generator - Port Registry\nDo not edit manually");
            }
            log.info("\u2713 ports-used.properties reset at: {}", registryPath);
        } catch (IOException e) {
            log.error("Could not reset ports-used.properties: {}", e.getMessage());
        }
    }

    public int assignNextPort(String projectName) {
        Properties props = loadRegistry();
        int maxPort = props.values().stream()
                .mapToInt(v -> {
                    try { return Integer.parseInt(v.toString()); }
                    catch (NumberFormatException ex) { return 0; }
                })
                .max().orElse(BASE_PORT - 1);

        // Find next port that is both above maxPort and actually free on the OS
        int nextPort = Math.max(maxPort + 1, BASE_PORT);
        while (!isPortAvailable(nextPort)) {
            log.warn("Port {} is already in use \u2014 trying {}", nextPort, nextPort + 1);
            nextPort++;
        }

        props.setProperty(projectName.toLowerCase(), String.valueOf(nextPort));
        saveRegistry(props);
        log.info("Assigned port {} to project '{}'", nextPort, projectName);
        return nextPort;
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public int getPort(String projectName) {
        Properties props = loadRegistry();
        String val = props.getProperty(projectName.toLowerCase());
        return val != null ? Integer.parseInt(val) : -1;
    }

    private Properties loadRegistry() {
        Properties props = new Properties();
        if (Files.exists(registryPath)) {
            try (InputStream in = Files.newInputStream(registryPath)) {
                props.load(in);
            } catch (IOException e) {
                log.error("Could not read ports-used.properties: {}", e.getMessage());
            }
        }
        return props;
    }

    private void saveRegistry(Properties props) {
        try (OutputStream out = Files.newOutputStream(registryPath)) {
            props.store(out, "Scaffold Generator - Port Registry");
        } catch (IOException e) {
            log.error("Could not save ports-used.properties: {}", e.getMessage());
        }
    }

    public Path getRegistryPath() { return registryPath; }

    public java.util.Map<String, String> getAllPorts() {
        Properties props = loadRegistry();
        java.util.Map<String, String> result = new java.util.LinkedHashMap<>();
        props.forEach((k, v) -> result.put(k.toString(), v.toString()));
        return result;
    }
}
