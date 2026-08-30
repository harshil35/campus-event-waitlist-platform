# Campus Event & Waitlist Platform

A Spring Boot REST API for managing campus events, seat reservations, waitlists, cancellations, and automatic waitlist promotion.

The project uses PostgreSQL as the primary source of truth and Redis for event caching and reservation rate limiting.

## Features

- Event CRUD operations
- Event capacity management
- Reservation creation and cancellation
- Duplicate reservation prevention
- Event-specific reservation lookup
- Waitlist management
- Automatic promotion of the earliest waitlisted attendee after a cancellation
- Request validation using Jakarta Bean Validation
- Centralized exception handling with Spring `@RestControllerAdvice`
- Standardized API errors using `ProblemDetail`
- PostgreSQL persistence with Spring Data JPA and Hibernate
- Transactional reservation and waitlist operations
- Row-level locking for event operations using repository queries designed for `FOR UPDATE`
- Redis-backed event caching
- Cache invalidation when events are created, updated, or deleted
- TTL-based Redis cache expiration
- Per-attendee reservation rate limiting
- HTTP `429 Too Many Requests` responses when the rate limit is exceeded
- JUnit and Mockito unit tests
- Spring integration testing with MockMvc
- Dockerfile and Docker Compose configuration for the application stack

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.1.0 |
| Web | Spring MVC / REST |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL 15 |
| Cache / Rate Limiting | Redis |
| Validation | Jakarta Bean Validation |
| Testing | JUnit, Mockito, Spring MockMvc |
| Build | Maven |
| Containerization | Docker, Docker Compose |

## Architecture

```text
                    REST Client
                 (Postman / Client)
                         |
                         v
              +-----------------------+
              |      Controllers      |
              | Event / Reservation  |
              | / Waitlist            |
              +-----------+-----------+
                          |
                          v
              +-----------------------+
              |       Services        |
              | EventService           |
              | ReservationService     |
              | WaitlistService        |
              | RateLimitService       |
              +-----------+-----------+
                          |
                +---------+---------+
                |                   |
                v                   v
       +----------------+   +----------------+
       |   PostgreSQL   |   |     Redis      |
       | Source of      |   | Event cache +  |
       | truth          |   | rate limiting  |
       +----------------+   +----------------+
```

## Project Structure

```text
src/
├── main/
│   ├── java/com/example/campus/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
│       └── application.properties
│
└── test/
    └── java/com/example/campus/
        ├── controller/
        └── service/
```

## Core Business Flows

### Event creation

```text
POST /api/v1/events
        |
        v
Validate request
        |
        v
Validate start/end time
        |
        v
Create Event entity
        |
        v
Persist with JPA
```

### Reservation

```text
POST /api/v1/events/{eventId}/reservations
        |
        v
Rate-limit check
        |
        v
Lock event row
        |
        v
Check event exists
        |
        v
Check duplicate attendee
        |
        v
Check capacity
        |
        v
Create reservation
        |
        v
PostgreSQL
```

### Waitlist promotion

```text
Reservation cancellation
        |
        v
Delete reservation
        |
        v
Find earliest waitlist entry
        |
        v
Create reservation for that attendee
        |
        v
Remove waitlist entry
```

## Redis Usage

Redis is used for two separate concerns.

### Event caching

The event list is cached to avoid repeatedly querying PostgreSQL for the same frequently requested data.

```text
GET /api/v1/events
        |
        v
Redis cache
   |          \
   | hit       \ miss
   v            v
return       PostgreSQL
               |
               v
          store in Redis
```

The current cache uses a 60-second TTL. Event create, update, and delete operations evict the `events` cache so stale event data is not retained after changes.

### Reservation rate limiting

Reservation attempts are counted in Redis using a key based on the attendee email:

```text
rate:reservation:<attendeeEmail>
```

Current policy:

```text
10 reservation attempts
within 60 seconds
per attendee
```

When the counter exceeds the limit, the service throws a custom rate-limit exception and the global exception handler returns HTTP `429 Too Many Requests`.

Example response:

```json
{
  "detail": "Too many reservation attempts. Try again later.",
  "instance": "/api/v1/events/16/reservations",
  "status": 429,
  "title": "Too Many Requests"
}
```

## Error Handling

The API uses a global `@RestControllerAdvice` to translate application exceptions into consistent HTTP responses.

| Scenario | Status |
|---|---:|
| Invalid request / validation | 400 |
| Event or reservation not found | 404 |
| Business conflict | 409 |
| Rate limit exceeded | 429 |

Errors are represented using Spring's `ProblemDetail`.

Example:

```json
{
  "detail": "Event not found: 999",
  "instance": "/api/v1/events/999",
  "status": 404,
  "title": "Event Not Found"
}
```

## Testing

The project contains unit tests for the service layer using JUnit and Mockito.

### Unit tests

Service dependencies such as repositories and Redis are mocked so business logic can be tested in isolation.

Examples of tested scenarios include:

- Event creation and validation
- Event update and deletion rules
- Missing-event handling
- Reservation creation
- Duplicate reservation prevention
- Capacity enforcement
- Reservation cancellation
- Waitlist promotion
- Waitlist validation
- Rate-limit boundaries and TTL behavior

### Integration tests

The project also contains Spring integration testing using `@SpringBootTest` and `MockMvc`.

The integration tests exercise the real application flow:

```text
HTTP request
    |
    v
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
PostgreSQL
```

Run the full test suite with:

```bash
mvn clean test
```

## Local Setup

### Prerequisites

- Java 25
- Maven
- PostgreSQL 15
- Redis

### Database

Create a PostgreSQL database named `campus` and make sure PostgreSQL is running on port `5432`.

The current local configuration uses:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/campus
spring.datasource.username=hfpatel
```

Redis is expected on:

```text
localhost:6379
```

### Run the application

```bash
mvn spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

## API Examples

### Create an event

```http
POST /api/v1/events
Content-Type: application/json
```

```json
{
  "title": "Spring Boot Workshop",
  "description": "Introduction to Spring Boot",
  "startsAt": "2026-08-25T10:00:00",
  "endsAt": "2026-08-25T12:00:00",
  "venue": "Room 101",
  "capacity": 50,
  "organizerEmail": "teacher@example.com"
}
```

### Create a reservation

```http
POST /api/v1/events/{eventId}/reservations
Content-Type: application/json
```

```json
{
  "attendeeEmail": "student@example.com"
}
```

### Get reservations for an event

```http
GET /api/v1/events/{eventId}/reservations
```

### Cancel a reservation

```http
DELETE /api/v1/events/{eventId}/reservations/{reservationId}
```

### Get the waitlist

```http
GET /api/v1/events/{eventId}/waitlist
```

> Endpoint paths above reflect the project API structure; verify against the current controller mappings if the API is extended.

## Configuration

The application supports environment-variable overrides so the same Spring Boot application can run locally or in a containerized environment.

Example:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/campus}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:hfpatel}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
spring.data.redis.host=${SPRING_DATA_REDIS_HOST:localhost}
spring.data.redis.port=${SPRING_DATA_REDIS_PORT:6379}
```

For Docker Compose, environment-specific values are supplied through `.env`.

**Do not commit `.env` to GitHub.** Commit `.env.example` instead.

## Docker

The repository includes:

```text
Dockerfile
docker-compose.yml
.env.example
```

The intended containerized architecture is:

```text
Docker Compose
├── Spring Boot application
├── PostgreSQL 15
└── Redis 7
```

Inside the Compose network, the Spring application connects to:

```text
PostgreSQL → postgres:5432
Redis      → redis:6379
```

The Docker configuration should be validated on a machine where Docker is available before relying on the Compose setup in production or CI.

## GitHub Hygiene

The repository should not contain:

```text
.env
target/
.idea/
*.iml
```

Use `.env.example` to document the required environment variables without committing local secrets.

## Future Improvements

Potential production-oriented enhancements include:

- Testcontainers for isolated PostgreSQL and Redis integration tests
- Database migrations with Flyway
- Atomic Redis rate-limit updates using a Lua script or another atomic mechanism
- Authentication and authorization
- API documentation with OpenAPI / Swagger
- Metrics and observability
- CI/CD pipeline

## Status

Core backend functionality, Redis caching, rate limiting, unit tests, and Spring integration testing are implemented. Docker configuration is included as part of the project, with runtime verification dependent on an environment where Docker is permitted.
