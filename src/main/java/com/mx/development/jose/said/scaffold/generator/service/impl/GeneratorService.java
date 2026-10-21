package com.mx.development.jose.said.scaffold.generator.service.impl;

import com.mx.development.jose.said.scaffold.generator.model.GeneratorRequest;
import com.mx.development.jose.said.scaffold.generator.model.GeneratorResponse;
import com.mx.development.jose.said.scaffold.generator.service.PortRegistryService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import com.mx.development.jose.said.scaffold.application.dto.AttributeDefinitionDTO;
import com.mx.development.jose.said.scaffold.application.model.ApplicationAttribute;
import com.mx.development.jose.said.scaffold.application.repository.ApplicationAttributeRepository;
import com.mx.development.jose.said.scaffold.application.repository.ApplicationRepository;
import com.mx.development.jose.said.scaffold.generator.service.ProcessManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratorService {

    private final Configuration freemarkerConfig;
    private final PortRegistryService portRegistryService;
    private final ProcessManagerService processManagerService;
    private final ApplicationRepository applicationRepository;
    private final ApplicationAttributeRepository attributeRepository;

    public GeneratorResponse generate(GeneratorRequest req) {
        long start = System.currentTimeMillis();
        // Ensure singular entity name (remove trailing 's' if present)
        String rawName = req.getName().trim();
        // Normalize entity name: use as-is (singular), capitalize
        // Do NOT strip trailing 's' — user should provide singular form
        String entityName = capitalize(rawName);
        String entityNameLower = rawName.toLowerCase();
        String projectDir = entityNameLower;

        // Output directory: sibling of scaffold project
        String userDir = System.getProperty("user.dir");
        Path parentDir = Paths.get(userDir).getParent();
        if (parentDir == null) parentDir = Paths.get(userDir);
        Path outputPath = parentDir.resolve(projectDir);

        try {
            // Assign port
            int port = portRegistryService.assignNextPort(entityNameLower);

            // Build template model
            Map<String, Object> model = buildModel(req, entityName, entityNameLower, port);

            // Clean output dir
            deleteDir(outputPath);
            Files.createDirectories(outputPath);

            // Generate project structure
            String basePkg = req.getBasePackage() + "." + entityNameLower;
            String pkgPath = basePkg.replace(".", "/");
            // basePkg used in templates is req.getBasePackage() only — entityNameLower added per template
            Path srcMain = outputPath.resolve("src/main/java").resolve(pkgPath);
            Path srcTest = outputPath.resolve("src/test/java").resolve(pkgPath);
            Path resources = outputPath.resolve("src/main/resources");

            Files.createDirectories(srcMain.resolve("controller"));
            Files.createDirectories(srcMain.resolve("service/impl"));
            Files.createDirectories(srcMain.resolve("repository"));
            Files.createDirectories(srcMain.resolve("model"));
            Files.createDirectories(srcMain.resolve("dto"));
            Files.createDirectories(srcMain.resolve("mapper"));
            Files.createDirectories(srcMain.resolve("exception"));
            Files.createDirectories(srcTest);
            Files.createDirectories(resources);
            Files.createDirectories(resources.resolve("static"));

            // Generate files
            render("pom.xml.ftl",        model, outputPath.resolve("pom.xml"));
            render("Entity.java.ftl",    model, srcMain.resolve("model/" + entityName + ".java"));
            render("RequestDTO.java.ftl", model, srcMain.resolve("dto/" + entityName + "RequestDTO.java"));
            render("ResponseDTO.java.ftl",model, srcMain.resolve("dto/" + entityName + "ResponseDTO.java"));
            render("Mapper.java.ftl",    model, srcMain.resolve("mapper/" + entityName + "Mapper.java"));
            render("Repository.java.ftl",model, srcMain.resolve("repository/" + entityName + "Repository.java"));
            render("Service.java.ftl",   model, srcMain.resolve("service/" + entityName + "Service.java"));
            render("ServiceImpl.java.ftl",model, srcMain.resolve("service/impl/" + entityName + "ServiceImpl.java"));
            render("Controller.java.ftl",model, srcMain.resolve("controller/" + entityName + "Controller.java"));

            // Exception classes (static)
            writeException(srcMain.resolve("exception"), basePkg);

            // Main application class
            writeMainClass(srcMain, basePkg, entityName);
            if (req.isIncludeSwagger()) writeOpenApiConfig(srcMain, basePkg, entityName, req);

            // Config file
            if ("yml".equals(req.getConfigFormat())) {
                render("application.yml.ftl", model, resources.resolve("application.yml"));
            } else {
                render("application.properties.ftl", model, resources.resolve("application.properties"));
            }

            // Optional files
            // Always generate Dockerfile when MySQL selected; otherwise respect UI checkbox
            boolean needsDockerfile = req.isGenerateDockerfile() || "mysql".equals(req.getDatabase()) || req.isKafkaProducer() || req.isKafkaConsumer();
            if (needsDockerfile) render("Dockerfile.ftl", model, outputPath.resolve("Dockerfile"));
            // Generate docker-compose.yml when MySQL or Kafka is selected
            boolean needsCompose = "mysql".equals(req.getDatabase()) || req.isKafkaProducer() || req.isKafkaConsumer();
            if (needsCompose) render("docker-compose.yml.ftl", model, outputPath.resolve("docker-compose.yml"));
            if (req.isGenerateReadme())     render("README.md.ftl",  model, outputPath.resolve("README.md"));
            // Always generate index.html landing page
            render("index.html.ftl", model, resources.resolve("static/index.html"));
            // Kafka producer/consumer classes
            if (req.isKafkaProducer() || req.isKafkaConsumer()) {
                Path kafkaDir = srcMain.resolve("kafka");
                Files.createDirectories(kafkaDir);
                if (req.isKafkaProducer()) writeKafkaProducer(kafkaDir, basePkg, entityName, req.getKafkaTopic() != null ? req.getKafkaTopic() : "my-topic");
                if (req.isKafkaConsumer()) writeKafkaConsumer(kafkaDir, basePkg, entityName, req.getKafkaTopic() != null ? req.getKafkaTopic() : "my-topic");
            }
            // Fix 9: Unit tests
            if (req.isGenerateUnitTests()) {
                Path testDir = outputPath.resolve("src/test/java").resolve(basePkg.replace(".", "/") + "/service");
                Files.createDirectories(testDir);
                render("ServiceTest.java.ftl", model, testDir.resolve(entityName + "ServiceTest.java"));
            }
            // Fix 14: Create logs directory
            Path logsDir = outputPath.resolve("logs");
            Files.createDirectories(logsDir);
            Files.writeString(logsDir.resolve(".gitkeep"), "");
            // Generate .dockerignore to speed up Docker builds
            Files.writeString(outputPath.resolve(".dockerignore"),
                "target/\n.git/\nlogs/\n*.log\n.idea/\n*.iml\n");
            // Fix 14: Generate start script
            boolean useCompose = "mysql".equals(req.getDatabase()) || req.isKafkaProducer() || req.isKafkaConsumer();
            generateStartScript(outputPath, entityNameLower, port, useCompose);

            // ZIP
            Path zipPath = parentDir.resolve(projectDir + ".zip");
            zipDirectory(outputPath, zipPath);

            long duration = System.currentTimeMillis() - start;
            log.info("Project '{}' generated in {}ms at {}", entityName, duration, outputPath);

            // Persist directory and port in Application record, then register with real app id
            // Prefer lookup by appId (passed from UI); fallback to name for backwards compat
            java.util.Optional<com.mx.development.jose.said.scaffold.application.model.Application> appOpt =
                    (req.getAppId() != null)
                            ? applicationRepository.findById(req.getAppId())
                            : applicationRepository.findByNameIgnoreCase(req.getName().trim());
            appOpt.ifPresent(app -> {
                app.setOutputDirectory(outputPath.toAbsolutePath().toString());
                app.setAssignedPort(port);
                applicationRepository.save(app);
                log.info("Persisted outputDirectory and assignedPort for '{}' (appId={})", entityName, app.getId());
                // Register using the real DB id so Start/Stop can find it
                processManagerService.registerProject(app.getId(), outputPath.toAbsolutePath().toString(), port);
                log.info("Registered project for Start/Stop: appId={} dir={} port={}", app.getId(), outputPath, port);
            });

            return GeneratorResponse.builder()
                    .success(true)
                    .projectName(entityName)
                    .outputDirectory(outputPath.toAbsolutePath().toString())
                    .zipPath(zipPath.toAbsolutePath().toString())
                    .assignedPort(port)
                    .message("Project generated successfully")
                    .durationMs(duration)
                    .build();

        } catch (Exception e) {
            log.error("Generation failed: {}", e.getMessage(), e);
            return GeneratorResponse.builder()
                    .success(false)
                    .projectName(entityName)
                    .message("Generation failed: " + e.getMessage())
                    .durationMs(System.currentTimeMillis() - start)
                    .build();
        }
    }

    private Map<String, Object> buildModel(GeneratorRequest req, String entityName, String entityNameLower, int port) {
        Map<String, Object> m = new HashMap<>();
        m.put("entityName", entityName);
        m.put("entityNameLower", entityNameLower);
        m.put("tableName", pluralize(entityNameLower));
        m.put("basePackage", req.getBasePackage());  // templates append entityNameLower themselves
        m.put("springBootVersion", req.getSpringBootVersion());
        m.put("javaVersion", req.getJavaVersion());
        m.put("projectVersion", req.getVersion());
        m.put("description", req.getDescription() != null ? req.getDescription() : entityName + " microservice");
        m.put("author", req.getAuthor());
        m.put("packaging", req.getPackaging());
        m.put("configFormat", req.getConfigFormat() != null ? req.getConfigFormat() : "properties");
        m.put("architecture", req.getArchitecture() != null ? req.getArchitecture() : "modular");
        m.put("database", req.getDatabase());
        m.put("assignedPort", String.valueOf(port));  // Fix 2: as plain string, no number formatting
        m.put("includeLombok", req.isIncludeLombok());
        m.put("includeMapstruct", req.isIncludeMapstruct());
        m.put("includeSecurity", req.isIncludeSecurity());
        m.put("includeCache", req.isIncludeCache());
        m.put("includeActuator", req.isIncludeActuator());
        m.put("includeSwagger", req.isIncludeSwagger());
        m.put("generatedDate", LocalDate.now().toString());
        // Load attributes from DB if not provided in request
        List<GeneratorRequest.AttributeDefinition> attrs = req.getAttributes();
        if ((attrs == null || attrs.isEmpty()) && req.getName() != null) {
            applicationRepository.findByNameIgnoreCase(req.getName().trim()).ifPresent(app -> {
                List<ApplicationAttribute> dbAttrs = attributeRepository.findByApplicationIdOrderBySortOrderAsc(app.getId());
                if (!dbAttrs.isEmpty()) {
                    req.setAttributes(dbAttrs.stream().map(a ->
                            GeneratorRequest.AttributeDefinition.builder()
                                    .name(a.getName()).type(a.getType())
                                    .validations(a.getValidations())
                                    .nullable(a.isNullable()).unique(a.isUnique()).pk(a.isPk())
                                    .maxLen(a.getMaxLen())
                                    .build()
                    ).toList());
                }
            });
            attrs = req.getAttributes();
        }
        if (attrs == null || attrs.isEmpty()) {
            attrs = List.of(GeneratorRequest.AttributeDefinition.builder()
                    .name("id").type("Long").validations(List.of()).nullable(false).unique(true).pk(true).build());
            req.setAttributes(attrs);
        }
        m.put("attributes", req.getAttributes());

        // Type flags for imports
        boolean hasLocalDate = req.getAttributes().stream().anyMatch(a -> "LocalDate".equals(a.getType()));
        boolean hasLocalDateTime = req.getAttributes().stream().anyMatch(a -> "LocalDateTime".equals(a.getType()));
        boolean hasBigDecimal = req.getAttributes().stream().anyMatch(a -> "BigDecimal".equals(a.getType()));
        boolean hasUUID = req.getAttributes().stream().anyMatch(a -> "UUID".equals(a.getType()));
        m.put("hasLocalDate", hasLocalDate);
        m.put("hasLocalDateTime", hasLocalDateTime);
        m.put("hasBigDecimal", hasBigDecimal);
        m.put("hasUUID", hasUUID);
        m.put("kafkaProducer", req.isKafkaProducer());
        m.put("kafkaConsumer", req.isKafkaConsumer());
        m.put("kafkaTopic", req.getKafkaTopic() != null ? req.getKafkaTopic() : "my-topic");
        m.put("includeKafka", req.isKafkaProducer() || req.isKafkaConsumer());
        return m;
    }

    private void render(String templateName, Map<String, Object> model, Path output) throws Exception {
        Template tpl = freemarkerConfig.getTemplate(templateName);
        Files.createDirectories(output.getParent());
        try (Writer w = new FileWriter(output.toFile())) {
            tpl.process(model, w);
        }
        log.debug("Generated: {}", output.getFileName());
    }

    private void writeOpenApiConfig(Path srcMain, String pkg, String entityName, GeneratorRequest req) throws IOException {
        String desc = req.getDescription() != null ? req.getDescription() : entityName + " microservice";
        String code =
                "package " + pkg + ";\n\n" +
                        "import io.swagger.v3.oas.models.OpenAPI;\n" +
                        "import io.swagger.v3.oas.models.info.Info;\n" +
                        "import org.springframework.context.annotation.Bean;\n" +
                        "import org.springframework.context.annotation.Configuration;\n\n" +
                        "@Configuration\n" +
                        "public class OpenApiConfig {\n\n" +
                        "    @Bean\n" +
                        "    public OpenAPI customOpenAPI() {\n" +
                        "        return new OpenAPI()\n" +
                        "            .info(new Info()\n" +
                        "                .title(\"" + entityName + " API\")\n" +
                        "                .version(\"" + req.getVersion() + "\")\n" +
                        "                .description(\"" + desc + "\"));\n" +
                        "    }\n" +
                        "}\n";
        Files.writeString(srcMain.resolve("OpenApiConfig.java"), code);
        log.debug("Generated: OpenApiConfig.java");
    }

    private void writeException(Path dir, String pkg) throws IOException {
        Files.writeString(dir.resolve("ResourceNotFoundException.java"),
                "package " + pkg + ".exception;\n\npublic class ResourceNotFoundException extends RuntimeException {\n    public ResourceNotFoundException(String message) { super(message); }\n}\n");
        Files.writeString(dir.resolve("GlobalExceptionHandler.java"),
                "package " + pkg + ".exception;\n\nimport io.swagger.v3.oas.annotations.Hidden;\nimport org.springframework.http.*;\nimport org.springframework.web.bind.annotation.*;\n\n@Hidden\n@RestControllerAdvice\npublic class GlobalExceptionHandler {\n    @ExceptionHandler(ResourceNotFoundException.class)\n    public ResponseEntity<String> handle(ResourceNotFoundException ex) {\n        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());\n    }\n}\n");
    }

    private void writeMainClass(Path dir, String pkg, String entityName) throws IOException {
        Files.createDirectories(dir);
        String className = entityName + "Application";
        Files.writeString(dir.resolve(className + ".java"),
                "package " + pkg + ";\n\nimport org.springframework.boot.SpringApplication;\nimport org.springframework.boot.autoconfigure.SpringBootApplication;\n\n@SpringBootApplication\npublic class " + className + " {\n    public static void main(String[] args) {\n        SpringApplication.run(" + className + ".class, args);\n    }\n}\n");
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void deleteDir(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }

    private void writeKafkaProducer(Path dir, String pkg, String entityName, String topic) throws IOException {
        String code =
                "package " + pkg + ".kafka;\n\n" +
                        "import lombok.RequiredArgsConstructor;\n" +
                        "import lombok.extern.slf4j.Slf4j;\n" +
                        "import org.springframework.kafka.core.KafkaTemplate;\n" +
                        "import org.springframework.stereotype.Service;\n\n" +
                        "@Slf4j\n" +
                        "@Service\n" +
                        "@RequiredArgsConstructor\n" +
                        "public class " + entityName + "KafkaProducer {\n\n" +
                        "    private static final String TOPIC = \"" + topic + "\";\n\n" +
                        "    private final KafkaTemplate<String, String> kafkaTemplate;\n\n" +
                        "    public void send(String message) {\n" +
                        "        log.info(\"Sending message to topic {}: {}\", TOPIC, message);\n" +
                        "        kafkaTemplate.send(TOPIC, message);\n" +
                        "    }\n" +
                        "}\n";
        Files.writeString(dir.resolve(entityName + "KafkaProducer.java"), code);
        log.debug("Generated: {}KafkaProducer.java", entityName);
    }

    private void writeKafkaConsumer(Path dir, String pkg, String entityName, String topic) throws IOException {
        String code =
                "package " + pkg + ".kafka;\n\n" +
                        "import lombok.extern.slf4j.Slf4j;\n" +
                        "import org.springframework.kafka.annotation.KafkaListener;\n" +
                        "import org.springframework.stereotype.Service;\n\n" +
                        "@Slf4j\n" +
                        "@Service\n" +
                        "public class " + entityName + "KafkaConsumer {\n\n" +
                        "    @KafkaListener(topics = \"" + topic + "\", groupId = \"" + entityName.toLowerCase() + "-group\")\n" +
                        "    public void consume(String message) {\n" +
                        "        log.info(\"Received message from topic " + topic + ": {}\", message);\n" +
                        "        // TODO: process message\n" +
                        "    }\n" +
                        "}\n";
        Files.writeString(dir.resolve(entityName + "KafkaConsumer.java"), code);
        log.debug("Generated: {}KafkaConsumer.java", entityName);
    }

    private void generateStartScript(Path outputPath, String projectName, int port, boolean useCompose) throws IOException {
        String scriptContent;
        if (useCompose) {
            // Docker Compose path: used standalone (not invoked by ProcessManagerService)
            scriptContent =
                    "#!/bin/bash\n" +
                            "# Start script for " + projectName + "\n" +
                            "# Generated by Spring Boot Scaffold Generator\n\n" +
                            "echo '==========================================='\n" +
                            "echo ' Starting " + projectName + " on port " + port + "'\n" +
                            "echo '==========================================='\n" +
                            "echo ''\n" +
                            "cd \"$(dirname \"$0\")\"\n" +
                            "mkdir -p logs\n" +
                            "echo 'Starting services with docker compose...'\n" +
                            "docker compose up --build -d\n" +
                            "echo 'Services started. App will be available at http://localhost:" + port + " once healthy.'\n";
        } else {
            // Simple path: ProcessManagerService invokes this script via /bin/bash.
            // Output is already redirected to logs/app.log by ProcessManagerService.
            // Use mvn spring-boot:run so no pre-build step is required.
            scriptContent =
                    "#!/bin/bash\n" +
                            "# Start script for " + projectName + "\n" +
                            "# Generated by Spring Boot Scaffold Generator\n\n" +
                            "echo '==========================================='\n" +
                            "echo ' Starting " + projectName + " on port " + port + "'\n" +
                            "echo '==========================================='\n" +
                            "echo ''\n" +
                            "cd \"$(dirname \"$0\")\"\n" +
                            "mkdir -p logs\n" +
                            "exec mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=" + port + "\n";
        }

        Path script = outputPath.resolve("start_" + projectName + ".sh");
        Files.writeString(script, scriptContent);
        script.toFile().setExecutable(true);
        log.info("Generated start script: {}", script.getFileName());
    }

    private String pluralize(String word) {
        if (word == null || word.isEmpty()) return word;
        String w = word.toLowerCase();
        if (w.endsWith("s")) return w;
        if (w.endsWith("y") && w.length() > 1 && "bcdfghjklmnpqrstvwxyz".indexOf(w.charAt(w.length() - 2)) >= 0) {
            return w.substring(0, w.length() - 1) + "ies";
        }
        if (w.endsWith("x") || w.endsWith("z") || w.endsWith("ch") || w.endsWith("sh")) {
            return w + "es";
        }
        return w + "s";
    }

    private void zipDirectory(Path sourceDir, Path zipPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));
             var paths = Files.walk(sourceDir)) {
            paths.filter(p -> !Files.isDirectory(p)).forEach(p -> {
                ZipEntry entry = new ZipEntry(sourceDir.relativize(p).toString());
                try { zos.putNextEntry(entry); Files.copy(p, zos); zos.closeEntry(); }
                catch (IOException e) { log.error("Zip error: {}", e.getMessage()); }
            });
        }
    }

    public java.util.Map<String, String> getPortsUsed() {
        return portRegistryService.getAllPorts();
    }
}