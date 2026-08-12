[![Integration Tests](https://github.com/qDurszlakp/fen/actions/workflows/maven.yml/badge.svg)](https://github.com/qDurszlakp/fen/actions/workflows/maven.yml)

````
Hey you!

Stack:
Java 21
Spring Boot 4
PostgreSQL + Liquibase
MongoDB
Kafka
Docker
Nginx
````

**Modules**
````
Server   - main application: REST API, banking, orders, audit, security, Kafka consumer
Foo      - auxiliary service: /secret endpoint, produces log messages to Kafka
Util     - shared library: Kafka topic definitions
````

**Commands**
````
build/run.sh                  - Build and run the project on local docker.
build/run.sh --run-tests      - Build with tests.
build/run.sh --push-images    - Tag and push images to the registry.
build/remove_containers.sh    - Remove containers.
build/swagger.sh              - Dump the OpenAPI spec from a running application.
mvn clean install             - Build everything, integration tests included.
````

**Authentication**
````
POST /auth/login    {username, password} -> {accessToken, refreshToken, tokenType, expiresIn}
POST /auth/refresh  {refreshToken}       -> new pair; the refresh token used is revoked (single use)
POST /auth/logout   {refreshToken}       -> revokes it server-side

Every other endpoint expects: Authorization: Bearer <accessToken>

Access tokens are signed RS256 JWTs (5 min TTL, see JwtKeyConfig / JwtService).
Refresh tokens are opaque, stored server-side as a SHA-256 hash (7 day TTL,
see RefreshTokenService) - that is what makes logout and revocation possible,
which a bare JWT alone cannot do.

Seeded users (AppUserDataInitializer): admin / admin (ROLE_ADMIN), user / user
(ROLE_USER). The two roles are identical everywhere except GET /audits, which is
ROLE_ADMIN only.
````

