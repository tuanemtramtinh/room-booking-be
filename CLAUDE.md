# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Clean build artifacts
./mvnw clean
```

## Architecture

**Stack**: Spring Boot 4.0.0, Java 21, PostgreSQL, Spring Data JPA, Spring Security, JWT (JJWT 0.12.7), MapStruct, Lombok, SpringDoc OpenAPI.

**Base package**: `com.hcmut.roombookingbe`

**Layered structure**:
- `controllers/` — REST API layer, routes prefixed with `/api/`
- `services/` — business logic
- `dtos/` — request/response objects (mapped to/from entities via MapStruct)
- `entities/` — JPA entities; all extend `BaseEntity` which provides `createdAt`/`updatedAt` via `@EnableJpaAuditing`
- `enums/` — shared enumerations
- `config/` — Spring configuration (Security, etc.)
- `repositories/` — Spring Data JPA repositories (not yet created, to be added here)

**Domain model**:
- `User`: email/googleId (unique), role (`ADMIN`/`STAFF`), status (`ACTIVE`/`INACTIVE`)
- `Room`: name, location, capacity, status (`AVAILABLE`/`MAINTENANCE`/`INACTIVE`)
- `Booking`: references `User` (requester) and `Room`; status lifecycle is `PENDING → APPROVED/REJECTED/CANCELLED`; has `reviewedBy`, `reviewedAt`, `rejectReason`
- `BookingHistory`: audit trail for booking status transitions — `fromStatus`, `toStatus`, `changedBy`, `changedAt`, `note`

**API surface** (controllers define these routes):
- `POST /api/auth/*` — authentication
- `GET|POST|PUT|DELETE /api/rooms` — room management
- `GET|POST|PUT|DELETE /api/bookings` — booking management
- `GET|POST|PUT|DELETE /api/users` — user management
- `GET /api/statistics` — reporting

**Security**: Spring Security is configured in `SecurityConfig`; CSRF disabled. JWT dependencies are included (JJWT) but authentication filter is not yet implemented — currently all routes are permitted.

**Database**: PostgreSQL at `103.77.241.6:6432`, database `room_booking`. `ddl-auto=update` so schema evolves with entities automatically. Credentials are currently hardcoded in `application.properties`.

**API docs**: SpringDoc OpenAPI is included; Swagger UI available at `/swagger-ui.html` when running.
