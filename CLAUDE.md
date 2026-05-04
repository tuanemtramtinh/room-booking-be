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

**Stack**: Spring Boot 4.0.5, Java 21, PostgreSQL, Spring Data JPA, Spring Security, JWT (JJWT 0.12.7), MapStruct 1.5.5, Lombok, SpringDoc OpenAPI 3.0.2.

**Base package**: `com.hcmut.roombookingbe`

**Layered structure**:
- `controllers/` — REST API layer, routes prefixed with `/api/`; `@CrossOrigin` applied where needed
- `services/` — business logic
- `dtos/` — request/response objects (to be mapped to/from entities via MapStruct)
- `entities/` — JPA entities; all extend `BaseEntity` which provides `createdAt`/`updatedAt`; `@EnableJpaAuditing` is on the main application class
- `enums/` — shared enumerations
- `config/` — Spring configuration (Security, etc.)
- `repositories/` — Spring Data JPA repositories (not yet created)
- `mappers/` — MapStruct mappers (not yet created)

**Domain model**:
- `User`: `email`/`googleId` (both unique), `fullName`, `avatarUrl`, `role` (`ADMIN`/`STAFF`), `status` (`ACTIVE`/`INACTIVE`)
- `Room`: `name`, `location`, `capacity`, `description`, `status` (`AVAILABLE`/`MAINTENANCE`/`INACTIVE`)
- `Booking`: `title`, `description`, `attendeeCount`, `startTime`/`endTime`; ManyToOne to `User` (requester) and `Room`; status lifecycle `PENDING → APPROVED/REJECTED/CANCELLED`; `reviewedBy` (User FK), `reviewedAt`, `rejectReason`
- `BookingHistory`: audit trail — ManyToOne to `Booking` and `changedBy` (User); `fromStatus`/`toStatus`, `changedAt` (auto-set on insert, not updatable), `note`

**Implementation status**: All controllers and services are scaffolded but mostly empty. Only `GET /api/rooms` returns a test response. Repositories and MapStruct mappers do not yet exist.

**API surface** (planned routes per controllers):
- `POST /api/auth/*` — authentication (Google OAuth + JWT)
- `GET|POST|PUT|DELETE /api/rooms` — room management
- `GET|POST|PUT|DELETE /api/bookings` — booking management
- `GET|POST|PUT|DELETE /api/users` — user management
- `GET /api/statistics` — reporting

**Security**: `SecurityConfig` has CSRF disabled and all routes permitted. JWT filter not yet implemented. Google OAuth integration is planned (branch: `google-auth`).

**Database**: PostgreSQL at `103.77.241.6:6432`, database `room_booking`. `ddl-auto=update` so schema evolves with entities automatically. Credentials are hardcoded in `application.properties`.

**API docs**: Swagger UI available at `/swagger-ui.html` when running.
