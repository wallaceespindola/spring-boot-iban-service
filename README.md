![Java](https://cdn.icon-icons.com/icons2/2699/PNG/512/java_logo_icon_168609.png)

# Banking IBAN Generator & Validator (Spring Boot, Java 21)

![Apache 2.0 License](https://img.shields.io/badge/License-Apache2.0-orange)
![Java](https://img.shields.io/badge/Built_with-Java21-blue)
![Junit5](https://img.shields.io/badge/Tested_with-Junit5-teal)
![Spring](https://img.shields.io/badge/Structured_by-SpringBoot-lemon)
![Maven](https://img.shields.io/badge/Powered_by-Maven-pink)
![Swagger](https://img.shields.io/badge/Docs_by-Swagger-yellow)
![OpenAPI](https://img.shields.io/badge/Specs_by-OpenAPI-purple)
[![CI](https://github.com/wallaceespindola/spring-boot-iban-service/actions/workflows/ci.yml/badge.svg)](https://github.com/wallaceespindola/spring-boot-iban-service/actions/workflows/ci.yml)

## Overview

A small Spring Boot service that **generates** structurally valid IBANs per country and **validates** IBANs with the
ISO 13616 **mod-97** check. It includes a dedicated **Belgium (BE)** generator that also computes a 2-digit BBAN check,
a static test page, a Postman collection and **Swagger UI / OpenAPI** docs.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Docker](#docker)
- [Docker Compose](#docker-compose)
- [API Endpoints](#api-endpoints)
- [Usage Examples](#usage-examples)
- [Running Tests](#running-tests)
- [Project Structure](#project-structure)
- [Notes and Known Limitations](#notes-and-known-limitations)

## Features

- IBAN validation: whitespace stripping, character check, per-country length check and mod-97 checksum
- Random IBAN generation for **74 countries** (correct length and check digits, numeric BBAN)
- Belgium-specific generator (3-digit bank code + 7-digit account + 2-digit BBAN check)
- Every API response carries a `timestamp` field
- Actuator `health` (with an extra `timestamp` detail) and `info` (app, build, Java and OS details)
- Swagger UI and OpenAPI 3.1 spec via springdoc
- Static test page at `/` and a [Postman collection](postman/IBAN%20Service.postman_collection.json)

## Tech Stack

| Component       | Version / Tool                                  |
|-----------------|-------------------------------------------------|
| Java            | 21                                              |
| Spring Boot     | 3.5.16 (Web, Actuator, DevTools)                |
| API docs        | springdoc-openapi-starter-webmvc-ui 2.9.1       |
| Build           | Maven                                           |
| Tests           | JUnit 5, Spring Boot Test (MockMvc)             |
| Container       | Multi-stage Dockerfile (Maven 3.9 / Temurin 21) |
| CI              | GitHub Actions (build + test), CodeQL           |

## Prerequisites

- JDK 21
- Maven 3.9+
- Docker (optional, for container runs)
- `make` (optional, for the Makefile shortcuts)

## Quick Start

```bash
mvn spring-boot:run
# or
mvn -DskipTests package && java -jar target/spring-boot-iban-service-0.0.1-SNAPSHOT.jar
```

The same tasks are available through the [Makefile](Makefile):

| Command             | What it does                                   |
|---------------------|------------------------------------------------|
| `make run`          | `mvn spring-boot:run`                          |
| `make build`        | Package the jar, skipping tests                |
| `make test`         | Run the unit tests                             |
| `make docker-build` | Build the `spring-boot-iban-service:latest` image |
| `make docker-run`   | Run the image on port 8080                     |
| `make compose-up`   | `docker compose up --build`                    |
| `make compose-down` | `docker compose down`                          |
| `make clean`        | `mvn clean`                                    |

Open:

| URL                                      | Description          |
|------------------------------------------|----------------------|
| `http://localhost:8080/`                 | Test UI (index.html) |
| `http://localhost:8080/swagger-ui.html`  | Swagger UI           |
| `http://localhost:8080/v3/api-docs`      | OpenAPI JSON         |
| `http://localhost:8080/actuator/health`  | Health check         |
| `http://localhost:8080/actuator/info`    | Build and app info   |

## Docker

Build the image (multi-stage Dockerfile, runs as a non-root user):

```bash
docker build -t spring-boot-iban-service:latest .
```

Run the container:

```bash
docker run --rm -p 8080:8080 --name iban-service \
  -e JAVA_OPTS="-XX:MaxRAMPercentage=75 -Djava.security.egd=file:/dev/./urandom" \
  spring-boot-iban-service:latest
```

Open:

- `http://localhost:8080/`
- `http://localhost:8080/swagger-ui.html`

## Docker Compose

Use the provided [docker-compose.yml](docker-compose.yml) to build and run:

```bash
docker compose up --build
# or (detached)
docker compose up --build -d
```

Stop and remove resources:

```bash
docker compose down
```

The compose service `iban-service` maps port `8080`, restarts `unless-stopped` and has a 512M memory limit.

Environment variables:

- `JAVA_OPTS` to adjust JVM settings.
- `SPRING_PROFILES_ACTIVE` to select Spring profile (e.g., `prod`).

## API Endpoints

All endpoints are `GET` and return JSON.

| Endpoint                           | Description                                                     | Response fields                                  |
|------------------------------------|-----------------------------------------------------------------|--------------------------------------------------|
| `/api/iban/{country}/generate`     | Random valid IBAN for the country (length + mod-97)             | `country`, `iban`, `bban`, `message`, `timestamp` |
| `/api/iban/be/generate`            | Belgium-specific generator (BBAN check + IBAN check digits)     | `country`, `iban`, `bban`, `timestamp`           |
| `/api/iban/validate?iban=...`      | Validates an IBAN (spaces allowed, case-insensitive)            | `valid`, `iban`, `message`, `timestamp`          |
| `/api/iban/countries`              | Supported country codes                                         | `supported`, `timestamp`                         |
| `/actuator/health`                 | Standard health plus an extra `timestamp` detail                | `status`, `components`                           |
| `/actuator/info`                   | App, build, Java and OS info                                    | `app`, `build`, `java`, `os`, ...                |

Both `/api/iban/be/generate` and `/api/iban/BE/generate` return Belgian IBANs whose BBAN passes the national
checksum. An unsupported country code returns HTTP 400 with a `message` and `timestamp`.

## Usage Examples

Generate an IBAN for Germany:

```bash
curl -s http://localhost:8080/api/iban/DE/generate
```

```json
{"iban":"DE11213006861214088597","country":"DE","bban":"213006861214088597","message":"OK","timestamp":"2026-09-26T17:50:29.242078Z"}
```

Generate a Belgian IBAN:

```bash
curl -s http://localhost:8080/api/iban/be/generate
```

Validate an IBAN (valid, then wrong checksum):

```bash
curl -s "http://localhost:8080/api/iban/validate?iban=BE71096123456769"
curl -s "http://localhost:8080/api/iban/validate?iban=BE71096123456768"
```

```json
{"valid":true,"iban":"BE71096123456769","message":"OK","timestamp":"2026-09-26T17:50:29.252811Z"}
{"valid":false,"iban":null,"message":"mod97 != 1 (71)","timestamp":"2026-09-26T17:50:29.265094Z"}
```

IBANs with spaces must be URL-encoded (`%20`), e.g. `?iban=BE71%200961%202345%206769`.

List supported countries:

```bash
curl -s http://localhost:8080/api/iban/countries
```

Health check:

```bash
curl -s http://localhost:8080/actuator/health
```

## Running Tests

```bash
mvn test
# or
make test
```

CI ([.github/workflows/ci.yml](.github/workflows/ci.yml)) runs `mvn -B clean verify` on JDK 21 and publishes the
JUnit report. The suite covers:

- `IbanValidatorTests` - valid BE/DE/FR/NL samples, wrong checksum, unknown country, and 1,000 generated Belgian IBANs
  checked against the national BBAN rule
- `IbanControllerTests` - MockMvc tests for the BE generator, the country generator, the validate endpoint and the
  HTTP 400 for unsupported countries

## Project Structure

```text
spring-boot-iban-service/
├── src/main/java/com/example/iban/
│   ├── IbanApplication.java
│   ├── controller/IbanController.java         # REST endpoints under /api/iban
│   ├── service/IbanService.java               # generic per-country generator
│   ├── service/BelgiumIbanGenerator.java      # Belgium-specific generator
│   ├── util/IbanValidator.java                # country lengths + mod-97 validation
│   └── health/TimestampHealthIndicator.java   # adds timestamp to /actuator/health
├── src/main/resources/
│   ├── application.yml                        # port, actuator, springdoc config
│   └── static/index.html                      # test UI
├── src/test/java/com/example/iban/            # JUnit 5 + MockMvc tests
├── postman/IBAN Service.postman_collection.json
├── Dockerfile
├── docker-compose.yml
├── Makefile
└── pom.xml
```

## Notes and Known Limitations

- Generic generator uses numeric BBAN for broad compatibility; it guarantees **structural** validity (correct length +
  check digits). Real bank/branch patterns vary by country and are **not** enforced.
- The Belgian BBAN check follows the national rule: first 10 digits `mod 97`, with `0` mapped to `97`.
- Health endpoint exposes details with `management.endpoint.health.show-details=always` and adds a `timestamp` via a
  custom `HealthIndicator`.

## Author

- Wallace Espindola, Sr. Software Engineer / Solution Architect / Java & Python Dev
- **LinkedIn:** [linkedin.com/in/wallaceespindola/](https://www.linkedin.com/in/wallaceespindola/)
- **GitHub:** [github.com/wallaceespindola](https://github.com/wallaceespindola)
- **E-mail:** [wallace.espindola@gmail.com](mailto:wallace.espindola@gmail.com)
- **Twitter:** [@wsespindola](https://twitter.com/wsespindola)
- **Gravatar:** [gravatar.com/wallacese](https://gravatar.com/wallacese)
- **Dev Community:** [dev.to/wallaceespindola](https://dev.to/wallaceespindola)
- **DZone Articles:** [DZone Profile](https://dzone.com/users/1254611/wallacese.html)
- **Pulse Articles:** [LinkedIn Articles](https://www.linkedin.com/in/wallaceespindola/recent-activity/articles/)
- **Website:** [W-Tech IT Solutions](https://www.wtechitsolutions.com/)
- **Presentation Slides:** [Speakerdeck](https://speakerdeck.com/wallacese)

## License

- This project is released under the Apache 2.0 License.
- See the [LICENSE](LICENSE) file for details.
- Copyright © 2025 [Wallace Espindola](https://github.com/wallaceespindola/).
