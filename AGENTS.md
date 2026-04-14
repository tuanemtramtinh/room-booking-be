# Repository Guidelines

## Project Structure & Module Organization
This is a Spring Boot backend (`Java 21`, Maven) under `src/main/java/com/hcmut/roombookingbe`.
- `controllers/`: REST endpoints (`/api/*`)
- `services/`: business logic
- `entities/`: JPA models (`BaseEntity` provides auditing fields)
- `dtos/`: request/response transfer models
- `enums/`: domain constants
- `config/`: security and framework configuration
- `src/main/resources/application.properties`: runtime config
- `src/test/java/...`: tests (currently basic bootstrap test)

Keep new packages aligned with this layered structure (for example, add `repositories/` for Spring Data interfaces).

## Build, Test, and Development Commands
Use the Maven wrapper from repository root:
- `./mvnw clean package`: compile, run tests, and build artifact
- `./mvnw spring-boot:run`: run the API locally
- `./mvnw test`: run all tests
- `./mvnw test -Dtest=RoomServiceTest`: run one test class
- `./mvnw clean`: remove build outputs (`target/`)

## Coding Style & Naming Conventions
- Follow standard Java style: 4-space indentation, no tabs.
- Class names: `PascalCase` (`BookingService`), methods/fields: `camelCase`, constants: `UPPER_SNAKE_CASE`.
- Keep controllers thin; place business logic in services.
- Prefer DTOs for API boundaries; avoid exposing entities directly.
- Use Lombok/MapStruct where already adopted in the module.

## Testing Guidelines
- Framework: Spring Boot test starters with JUnit 5 (via `spring-boot-starter-*-test`).
- Location mirrors source package structure under `src/test/java`.
- Name tests `*Test` (unit) or `*IT` (integration) for clarity.
- Add tests for service logic and controller behavior when changing business rules.

## Commit & Pull Request Guidelines
Current history is minimal (`First commit`), so adopt a clear, consistent format now:
- Commit messages: imperative, concise subject (e.g., `Add booking status validation`).
- Keep commits focused by concern (API, service, schema, tests).
- PRs should include: purpose, key changes, test evidence (`./mvnw test` output summary), and linked issue/task.

## Security & Configuration Tips
`application.properties` currently contains a real database host and credentials. Do not commit new secrets.
- Move sensitive values to environment variables or profile-specific local overrides.
- Review `SecurityConfig` changes carefully; authentication is evolving and affects all routes.
