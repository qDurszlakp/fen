# Project Context

> Java based project, designed for education purposes

## Purpose / Description

Education Sandbox

## Features / Scope

- **Server** module - main Spring Boot application:
  - REST API (`/rest`): cookies, file write, external posts proxy, passphrase, risk endpoints
  - DB API (`/db`): accounts, cards, countries (CRUD over PostgreSQL)
  - Basic authentication with seeded admin user and role-based access
  - Login rate limiting (Bucket4j + servlet filter, counts 401 responses)
  - Request auditing (servlet filter + persisted audit log)
  - Kafka consumer of logs produced by the Foo module
  - Static web UI (`/index.html`)
  - OpenAPI / Swagger UI documentation
  - (WIP) MCP tools integration (`mcp` package, currently commented out)
- **Foo** module - secondary Spring Boot service exposing a `/secret` passphrase endpoint and producing log messages to Kafka
- **Util** module - shared library (Kafka topic definitions)

## Tech Stack

- **Language:** Java (Spring Boot 3.5.x parent)
- **Framework:** Spring Boot - Web, WebFlux, Data JPA, JDBC, Security, AOP, Actuator
- **Messaging:** Apache Kafka (spring-kafka)
- **Database:** PostgreSQL, schema migrations via Liquibase
- **Build:** Maven (multi-module: `Server`, `Foo`, `Util`)
- **Libraries:** Lombok, MapStruct, Bucket4j (rate limiting), Reactor, springdoc-openapi
- **Testing:** JUnit, Spring Security Test, Spring Kafka Test, Testcontainers (PostgreSQL, Kafka)
- **Infrastructure:** Docker, Kubernetes (k8s), Terraform, Nginx (rate limiter)

## Architecture

Maven multi-module project:

- `Server` - main app; layered structure: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`, `client`, `aspect`, `filter`, `security`, `kafka`, `exception`, `mcp`
- `Foo` - auxiliary service (`controller`, `service`, `kafka`)
- `Util` - shared code (`kafka`)

Data flow: clients call `Server` REST/DB APIs → JPA persists to PostgreSQL. `Server` also calls external `Foo` service (`http://foo:8090/secret`) and an external JSONPlaceholder API. `Foo` produces log events to Kafka which `Server` consumes (`FooLogConsumer`). Cross-cutting concerns handled by servlet filters (audit, login rate limit) and AOP aspects.

## Setup / Run

- Build: `mvn clean install` (from repo root)
- Local (Docker): `./run.sh` - build and run on local Docker
- Stop: `./remove_containers.sh` - remove containers
- Kubernetes: `./k8s_run.sh` - build and run on local k8s cluster
- Terraform (k8s): `terraform apply -var 'registry_password=<DOCKER_HUB_PASSWORD>' -auto-approve`
- Server runs on port `8080`; Swagger UI available via springdoc; static UI at `/index.html`

## Configuration

- `Server/src/main/resources/application.yaml` - default config
- `Server/src/main/resources/application-deployment.yaml` - deployment profile
- Key settings:
  - Datasource: `jdbc:postgresql://db:5432/dev_user` (user `dev_user`)
  - Liquibase change-log: `classpath:liquibase-changeLog.xml`, migrations in `resources/migration/`
  - `files.baseDir` - file write path (`/app/data/Data.txt`)
  - `data.exampleUri` - external posts API; `data.passphraseUrl` - Foo service
  - `app.security.seed` - seeded admin credentials
- Terraform config: `terraform/` (`app.tf`, `db.tf`, `config-map.tf`, `secrets.tf`, `variables.tf`, `host.tf`, `main.tf`)

## External Dependencies

- PostgreSQL database
- Apache Kafka broker
- Foo service - `http://foo:8090/secret`
- JSONPlaceholder - `https://jsonplaceholder.typicode.com/posts`
- Docker Hub (image registry for k8s deployment)

## Conventions / Rules

- Multi-module Maven layout; module names capitalized (`Server`, `Foo`, `Util`)
- Package base: `com.sandbox.<module>`
- Lombok for boilerplate, MapStruct for DTO ↔ entity mapping
- Liquibase for versioned DB migrations (`V1_`, `V2_`, ... naming)
- API testing collection in `collection/` (Bruno)
- CI: GitHub Actions integration tests (`.github/workflows/maven.yml`)
