# Adventure Book API

Java 21 · Spring Boot 4.1 · Maven · PostgreSQL 17 · Flyway

```bash
docker compose up -d      # start Postgres
./mvnw spring-boot:run    # run the API
./mvnw test               # tests (Testcontainers, needs Docker)
```

App log level defaults to `INFO`; set `LOGGING_LEVEL_COM_ADVENTUREBOOK=DEBUG` (e.g. `LOGGING_LEVEL_COM_ADVENTUREBOOK=DEBUG ./mvnw spring-boot:run`, or as a container env var) to see more detail.

Swagger UI: http://localhost:8080/swagger-ui.html
