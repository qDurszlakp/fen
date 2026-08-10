# Project Context

> Java based project, designed for education purposes

## Purpose / Description

Education Sandbox

## Features / Scope

- **Server** module - main Spring Boot application:
  - REST API (`/rest`): cookies, file write, external posts proxy, passphrase, risk endpoints
  - Banking API (`/accounts`, `/cards`, `/countries`) - backed by PostgreSQL
  - Order API (`/orders`) - backed by MongoDB, address and items embedded in the document

  Endpoint paths name the domain resource, never the store behind it. `/db` and `/mongo`
  were renamed for exactly this reason - the storage engine is not part of the contract.
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

- **Language:** Java 21 (Spring Boot 4.0.x parent)
- **Framework:** Spring Boot - Web, WebFlux, Data JPA, Data MongoDB, JDBC, Security, AOP, Actuator
- **Messaging:** Apache Kafka (spring-kafka)
- **Databases:** PostgreSQL (JPA, schema migrations via Liquibase), MongoDB (Spring Data MongoDB)
- **Build:** Maven (multi-module: `Server`, `Foo`, `Util`)
- **Libraries:** Lombok, MapStruct, Bucket4j (rate limiting), Reactor, springdoc-openapi
- **Testing:** JUnit, Spring Security Test, Spring Kafka Test, Testcontainers (PostgreSQL, Kafka, MongoDB)
- **Infrastructure:** Docker, Nginx (rate limiter)

## Architecture

Maven multi-module project:

- `Server` - main app, **package by feature**: each business area owns its own layers.
  - `banking` - accounts, cards, countries over PostgreSQL (`controller`, `service`, `mapper`, `entity`, `repository`, `dto`)
  - `order` - orders over MongoDB (`controller`, `service`, `document`, `repository`, `dto`)
  - `audit` - request auditing (`filter`, `entity`, `repository`)
  - `demo` - the `/rest` playground endpoints (`controller`, `service`, `client`, `dto`)
  - `security` - authentication, seeded user, filter chain
  - `mcp` - (WIP) MCP tools
  - `playground` - standalone educational classes, unrelated to the running app
  - `common` - cross-cutting only: `exception` (incl. the global `ExnHandler`), `aspect`, `kafka`, `ratelimit`

  Package names describe the domain, never the storage engine - the same rule the
  endpoint paths follow. Hence `order`, not `mongo`.
- `Foo` - auxiliary service (`controller`, `service`, `kafka`)
- `Util` - shared code (`kafka`)

Data flow: clients call `Server` REST/DB APIs → JPA persists to PostgreSQL. `Server` also calls external `Foo` service (`http://foo:8090/secret`) and an external JSONPlaceholder API. `Foo` produces log events to Kafka which `Server` consumes (`FooLogConsumer`). Cross-cutting concerns handled by servlet filters (audit, login rate limit) and AOP aspects.

Polyglot persistence: JPA and MongoDB coexist in `Server`. The two stores live in separate feature packages - JPA in `banking.entity` + `banking.repository`, Mongo in `order.document` + `order.repository`. Spring Data resolves store assignment from the annotations on the domain types (`@Entity` vs `@Document`) and logs a "Could not safely identify store assignment" warning for each foreign candidate; adding explicit `@EnableJpaRepositories` / `@EnableMongoRepositories` with `basePackages` on `ServerApp` silences it.

## Setup / Run

- Build: `mvn clean install` (from repo root)
- Local (Docker): `build/run.sh` - build and run on local Docker
- Stop: `build/remove_containers.sh` - remove containers
- Server runs on port `8080`; Swagger UI available via springdoc; static UI at `/index.html`

## Configuration

- `Server/src/main/resources/application.yaml` - default config
- `Server/src/main/resources/application-deployment.yaml` - deployment profile
- Key settings:
  - Datasource: `jdbc:postgresql://postgres:5432/dev_user` (user `dev_user`)
  - MongoDB: `spring.mongodb.uri` - `mongodb://dev_user:dev@mongo:27017/sandbox?authSource=admin`
  - Liquibase change-log: `classpath:liquibase-changeLog.xml`, migrations in `resources/migration/`
  - `files.baseDir` - file write path (`/app/data/Data.txt`)
  - `data.exampleUri` - external posts API; `data.passphraseUrl` - Foo service
  - `app.security.seed` - seeded admin credentials
- Compose stack: `build/docker-compose.yml` (services: `gateway`, `server`, `server-debug`, `foo`, `postgres`, `mongo`, `zookeeper`, `kafka`)

Version gotchas worth remembering:

- Spring Boot 4 renamed the Mongo config prefix from `spring.data.mongodb` to **`spring.mongodb`**. The old prefix is silently ignored and the app falls back to `localhost:27017`.
- `mongo:8.0` refuses to start on Linux kernel 6.19+ (SERVER-121912). The stack pins `mongo:8.2`.

## External Dependencies

- PostgreSQL database
- MongoDB
- Apache Kafka broker
- Foo service - `http://foo:8090/secret`
- JSONPlaceholder - `https://jsonplaceholder.typicode.com/posts`

## Conventions / Rules

- Multi-module Maven layout; module names capitalized (`Server`, `Foo`, `Util`)
- Package base: `com.sandbox.<module>`
- Lombok for boilerplate, MapStruct for DTO ↔ entity mapping
- Liquibase for versioned DB migrations (`V1_`, `V2_`, ... naming)
- API testing collection in `collection/` (Bruno)
- CI: GitHub Actions integration tests (`.github/workflows/maven.yml`)
