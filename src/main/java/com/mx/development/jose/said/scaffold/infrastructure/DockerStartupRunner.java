package com.mx.development.jose.said.scaffold.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

@Slf4j
@Component
public class DockerStartupRunner implements ApplicationRunner {

    private static final String SCRIPT_NAME = "start_docker_containers.sh";
    private static final String DOCKER_SOCK = "/Users/" + System.getProperty("user.name") + "/.docker/run/docker.sock";
    private static final int DOCKER_WAIT_TIMEOUT_SECONDS = 60;

    @Override
    public void run(ApplicationArguments args) {
        log.info("╔══════════════════════════════════════════╗");
        log.info("║  Starting global Docker containers...    ║");
        log.info("╚══════════════════════════════════════════╝");

        try {
            ensureDockerIsRunning();

            ClassPathResource resource = new ClassPathResource(SCRIPT_NAME);
            if (!resource.exists()) {
                log.warn("[DockerStartup] {} not found in classpath — skipping", SCRIPT_NAME);
                return;
            }

            Path tempScript = Files.createTempFile("scaffold-docker-", ".sh");
            Files.copy(resource.getInputStream(), tempScript, StandardCopyOption.REPLACE_EXISTING);

            try {
                Set<PosixFilePermission> perms = Files.getPosixFilePermissions(tempScript);
                perms.add(PosixFilePermission.OWNER_EXECUTE);
                perms.add(PosixFilePermission.GROUP_EXECUTE);
                Files.setPosixFilePermissions(tempScript, perms);
            } catch (UnsupportedOperationException e) {
                tempScript.toFile().setExecutable(true);
            }

            ProcessBuilder pb = new ProcessBuilder("/bin/bash", tempScript.toAbsolutePath().toString());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("[ERROR]"))   log.error("[Docker] {}", line);
                    else if (line.contains("[WARN]")) log.warn("[Docker] {}", line);
                    else                              log.info("[Docker] {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("[DockerStartup] ✓ All containers checked/started successfully");
            } else {
                log.warn("[DockerStartup] Script exited with code {} — some containers may not have started", exitCode);
            }

            Files.deleteIfExists(tempScript);

        } catch (Exception e) {
            log.warn("[DockerStartup] Could not run Docker startup script: {} — continuing without it", e.getMessage());
        }
    }

    /**
     * Verifica si Docker está corriendo; si no, lanza Docker Desktop y espera
     * hasta que el daemon esté disponible (máx. DOCKER_WAIT_TIMEOUT_SECONDS).
     */
    private void ensureDockerIsRunning() throws Exception {
        if (isDockerRunning()) {
            log.info("[DockerStartup] Docker daemon already running ✓");
            return;
        }

        log.info("[DockerStartup] Docker daemon not detected — launching Docker Desktop...");
        launchDockerDesktop();

        log.info("[DockerStartup] Waiting for Docker daemon (timeout: {}s)...", DOCKER_WAIT_TIMEOUT_SECONDS);
        long deadline = System.currentTimeMillis() + (DOCKER_WAIT_TIMEOUT_SECONDS * 1000L);

        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(3000);
            if (isDockerRunning()) {
                log.info("[DockerStartup] Docker daemon is ready ✓");
                return;
            }
            log.info("[DockerStartup] Still waiting for Docker...");
        }

        throw new IllegalStateException(
                "Docker daemon did not start within " + DOCKER_WAIT_TIMEOUT_SECONDS + "s — aborting container startup"
        );
    }

    /**
     * Comprueba si el daemon responde ejecutando: docker info
     */
    private boolean isDockerRunning() {
        try {
            Process process = new ProcessBuilder("docker", "info")
                    .redirectErrorStream(true)
                    .start();
            // Consumir output para evitar bloqueo
            process.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
            int exit = process.waitFor();
            return exit == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Abre Docker Desktop en macOS usando 'open'.
     * En Linux intenta iniciar el servicio dockerd directamente.
     */
    private void launchDockerDesktop() throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase();

        if (os.contains("mac")) {
            new ProcessBuilder("open", "-a", "Docker")
                    .start();
        } else if (os.contains("linux")) {
            new ProcessBuilder("sudo", "systemctl", "start", "docker")
                    .start();
        } else {
            log.warn("[DockerStartup] OS '{}' not supported for auto-launch — start Docker manually", os);
        }
    }
}