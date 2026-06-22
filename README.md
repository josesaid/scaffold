# Scaffold — Spring Boot Project Generator

**Scaffold** is a full-stack code generation platform built with Spring Boot. It allows developers to define a microservice by name, entity attributes, and configuration options through a web UI, and instantly receive a ready-to-run, production-structured Spring Boot project — complete with source code, Dockerfile, docker-compose, start scripts, and unit tests.

---

## Author

**Jose Said Olano Garcia**
Senior Software Engineer · Java & Microservices
[![LinkedIn](https://img.shields.io/badge/LinkedIn-josesaid-0A66C2?style=flat&logo=linkedin)](https://www.linkedin.com/in/josesaid)
[![GitHub](https://img.shields.io/badge/GitHub-josesaid-181717?style=flat&logo=github)](https://github.com/josesaid)

---

## Technology Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.4.1 |
| Persistence | Spring Data JPA + H2 (dev) / MySQL (prod) |
| Template engine | Apache FreeMarker |
| Mapping | MapStruct 1.6.3 |
| Boilerplate reduction | Lombok 1.18.36 |
| Validation | Jakarta Bean Validation |
| Build | Maven |
| Containerization | Docker / Docker Compose |

---

## What Is Already Implemented

### 1. Code Generation Engine

The core of the platform: a FreeMarker-based engine that generates a complete, compilable Spring Boot project from a single HTTP request or a UI form submission.

**Generated artifacts per project:**

| Artifact | Description |
|---|---|
| `Entity.java` | JPA entity with all custom attributes, annotations, and imports resolved per type |
| `RequestDTO.java` | Input DTO with Bean Validation annotations |
| `ResponseDTO.java` | Output DTO |
| `Mapper.java` | MapStruct mapper interface (Entity ↔ DTO) |
| `Repository.java` | Spring Data JPA repository |
| `Service.java` | Service interface |
| `ServiceImpl.java` | Full CRUD service implementation |
| `Controller.java` | REST controller with standard endpoints |
| `ResourceNotFoundException.java` | Domain exception |
| `GlobalExceptionHandler.java` | `@RestControllerAdvice` with HTTP 404 handling |
| `<Entity>Application.java` | Spring Boot main class |
| `OpenApiConfig.java` | Springdoc OpenAPI configuration (when Swagger is enabled) |
| `application.properties` or `application.yml` | Fully configured with DB, JPA, and port settings |
| `Dockerfile` | Multi-stage build (auto-generated when MySQL or Kafka is selected) |
| `docker-compose.yml` | Includes MySQL and/or Kafka/Zookeeper services |
| `index.html` | Landing page for the generated microservice |
| `README.md` | Project-specific documentation |
| `start_<name>.sh` | Executable start script (Maven or Docker Compose path) |
| `.dockerignore` | Optimized to exclude `target/`, `.git/`, `logs/` |
| `logs/.gitkeep` | Pre-created log directory |
| `<Entity>KafkaProducer.java` | Kafka producer class (when enabled) |
| `<Entity>KafkaConsumer.java` | Kafka consumer with `@KafkaListener` (when enabled) |
| `<Entity>ServiceTest.java` | JUnit 5 + Mockito unit test skeleton (when enabled) |
| `<name>.zip` | Full project packaged as a downloadable ZIP |

**Generation options exposed through the API and UI:**

- **Spring Boot version:** 3.4.1 · 3.3.7 · 3.2.12
- **Java version:** 21 · 17 · 11
- **Database:** H2 · MySQL
- **Packaging:** `jar` · `war`
- **Config format:** `application.properties` · `application.yml`
- **Architecture style:** modular · flat
- **Optional dependencies:** Lombok · MapStruct · Spring Security · Spring Cache · Spring Actuator · Springdoc OpenAPI
- **Kafka:** producer · consumer · custom topic name
- **Artifacts:** Dockerfile · docker-compose · README · unit tests

---

### 2. Entity Attribute System

Custom entity fields are defined per application and persisted in the database. Each attribute supports:

| Property | Description |
|---|---|
| `name` | Field name |
| `type` | `String`, `Long`, `Integer`, `Double`, `BigDecimal`, `Boolean`, `LocalDate`, `LocalDateTime`, `UUID` |
| `validations` | Bean Validation annotations: `@NotBlank`, `@NotNull`, `@Email`, `@Size`, etc. |
| `nullable` | Maps to JPA `@Column(nullable = ...)` |
| `unique` | Maps to JPA `@Column(unique = true)` |
| `pk` | Marks field as primary key with `@Id` + `@GeneratedValue` |
| `maxLen` | Maps to `@Size(max = ...)` and `@Column(length = ...)` |
| `sortOrder` | Preserves the order of fields as defined in the UI |

Java import resolution is automatic: `LocalDate`, `LocalDateTime`, `BigDecimal`, and `UUID` are conditionally imported only when present in the attribute list.

---

### 3. Application Registry (CRUD API)

A full REST API to manage application definitions before generation.

**Endpoint base:** `/api/v1/applications`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/applications` | Create a new application definition |
| `GET` | `/api/v1/applications` | List all registered applications |
| `GET` | `/api/v1/applications/{id}` | Retrieve a specific application |
| `PUT` | `/api/v1/applications/{id}` | Update application metadata and attributes |
| `DELETE` | `/api/v1/applications/{id}` | Remove an application and its attributes |

The `Application` entity supports a **two-level self-referential hierarchy**: a root application can contain child microservices. Child microservices cannot be nested further.

---

### 4. Generator API

**Endpoint base:** `/api/v1/generator`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/generator/generate` | Generate a complete Spring Boot project |
| `GET` | `/api/v1/generator/ports` | List all ports assigned by the registry |

---

### 5. Process Manager

Allows starting and stopping generated Spring Boot projects directly from the Scaffold UI, without leaving the browser.

**Endpoint base:** `/api/v1/processes`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/processes/{appId}/start` | Start a generated project (runs its start script or `mvn spring-boot:run`) |
| `POST` | `/api/v1/processes/{appId}/stop` | Stop a running project (graceful, then forcible if needed) |
| `GET` | `/api/v1/processes/{appId}/status` | Get process state (`starting`, `running`, `stopped`, `error`) |
| `GET` | `/api/v1/processes` | List all registered application ports |
| `POST` | `/api/v1/processes/{appId}/register` | Register a project's directory and port |

Process output is redirected to `logs/app.log` inside the generated project directory. A background daemon thread streams the log output to Scaffold's own logger.

---

### 6. Port Registry

Automatic port assignment for every generated project, starting from `8101`. The registry:

- Reads the current state from `ports-used.properties` in the parent directory.
- Increments from the highest assigned port.
- Validates that the chosen port is actually available on the OS via `ServerSocket` before assigning it.
- Resets on Scaffold startup to clear stale entries from previous sessions.

---

### 7. Docker Auto-Startup

On application startup, `DockerStartupRunner` automatically:

1. Checks whether the Docker daemon is running (`docker info`).
2. If not, launches Docker Desktop (macOS) or starts the service via `systemctl` (Linux).
3. Waits up to 60 seconds for the daemon to become available.
4. Executes `start_docker_containers.sh` from the classpath to bring up any required containers.

---

### 8. Web UI

A single-page application served at `http://localhost:8080`, built with vanilla HTML/CSS/JS (no external framework dependency). Features:

- **Three themes:** Dark · Light · Enterprise
- **Left panel:** Application registry — create, select, and manage applications and their microservices.
- **Generator form:** All generation options exposed as form controls (database, Spring Boot version, Java version, optional dependencies, Kafka, output artifacts).
- **Attribute editor:** Dynamic table to define entity fields with type, validations, and constraints.
- **Process controls:** Start / Stop / Status buttons per application.
- **Port overview:** Live status indicator showing whether the Scaffold backend is reachable.

---

### 9. Data Seeder

On first startup, a root `Application` record named **"Scaffold"** is automatically inserted into the database, serving as the top-level container for all generated microservice definitions.

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker Desktop (optional, required for MySQL/Kafka-based generation)

### Run locally

```bash
git clone https://github.com/josesaid/scaffold.git
cd scaffold
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

The H2 console is available at **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:scaffolddb`).

### Switch to MySQL (production profile)

Uncomment and configure the MySQL datasource block in `src/main/resources/application.properties`, then update `spring.jpa.hibernate.ddl-auto` to `validate` or `update`.

---

## Project Structure

```
scaffold/
├── src/main/java/.../scaffold/
│   ├── application/           # Application registry domain (CRUD)
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── mapper/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   ├── generator/             # Code generation engine
│   │   ├── controller/
│   │   ├── model/
│   │   └── service/
│   └── infrastructure/        # Docker startup runner
├── src/main/resources/
│   ├── static/index.html      # Web UI
│   ├── templates/             # FreeMarker templates for code generation
│   └── application.properties
└── pom.xml
```

---

## Roadmap

The following features are planned for upcoming versions:

### v1.1
- [ ] **PostgreSQL support** — add PostgreSQL datasource template and driver to the generation engine
- [ ] **MongoDB support** — generate `@Document`-based entities and Spring Data MongoDB repositories
- [ ] **Integration test generation** — generate `@SpringBootTest` integration tests with Testcontainers for MySQL/PostgreSQL
- [ ] **Postman collection generation** — export a ready-to-import Postman collection per generated project

### v1.2
- [ ] **Spring Security scaffolding** — generate JWT-based authentication boilerplate (filter chain, token provider, `UserDetailsService`)
- [ ] **Spring Cache scaffolding** — generate `@EnableCaching` configuration with Redis or Caffeine depending on project setup
- [ ] **Spring Actuator integration** — include health, metrics, and info endpoints with pre-configured `management` properties

### v1.3
- [ ] **Multi-module Maven project generation** — generate parent POM + submodules for larger microservice architectures
- [ ] **GitHub Actions CI/CD pipeline generation** — include a `.github/workflows/ci.yml` with build, test, and Docker push steps
- [ ] **Kubernetes manifests generation** — generate `Deployment`, `Service`, and `ConfigMap` YAML files per microservice

### v2.0
- [ ] **Cloud deployment** — one-click deployment of generated projects to AWS ECS or GCP Cloud Run directly from the UI
- [ ] **Project version control** — track generation history per application with the ability to regenerate or diff between versions
- [ ] **Plugin system** — allow custom FreeMarker templates to be uploaded and registered as generation plugins

---

## License

This project is private. All rights reserved by the author.

---

*Built with Spring Boot 3.4.1 · Java 21 · FreeMarker · MapStruct · Lombok*
