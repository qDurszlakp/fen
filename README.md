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

