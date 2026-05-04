# Calendar System

A web application for scheduling meetings between users, with conflict-aware time slot suggestions.

## Tech Stack

- Java 21
- Spring Boot 3.4
- Spring Data JPA + H2
- Thymeleaf + Bootstrap 5
- JUnit 5 + MockMvc

## Features

- **Book a meeting** — set a title, time range, and participants; the system validates the slot
- **Cancel a meeting** — marks a scheduled meeting as cancelled
- **List meetings** — view all upcoming scheduled meetings
- **Suggest time slots** — finds free 1-hour windows where two users have no conflicting meetings, starting from a given date

## How to Run

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`. The H2 database is seeded automatically with two users (`alice` and `bob`) on startup.

To run the tests:

```bash
./mvnw test
```
