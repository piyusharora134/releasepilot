# ReleasePilot

ReleasePilot is a feature flag and progressive rollout platform designed to help teams safely release, control, and monitor application features.

The project is being built as a production-oriented SaaS backend with a focus on clean architecture, secure APIs, database versioning, progressive delivery, and scalable infrastructure.

## Current Status

Under active development

### Currently Implemented

- Spring Boot 4.1.0
- Java 21 LTS
- Maven
- Spring Web
- Spring Data JPA
- Spring Security
- Jakarta Validation
- PostgreSQL 17
- Docker Compose
- Lombok
- Spring Boot DevTools
- GitHub-based version control

## Planned Architecture

```text
                    ReleasePilot
                         │
              ┌──────────┴──────────┐
              │                     │
           Frontend              Backend
        React + TypeScript      Spring Boot
              │                     │
              │              ┌──────┴──────┐
              │              │             │
              │           PostgreSQL     Redis
              │
              └────────────── API ─────────┘
```

## Core Features

The platform will eventually provide:

- User authentication and authorization
- Organizations and projects
- Multiple deployment environments
- Feature flag management
- Targeting rules
- Percentage-based rollouts
- Progressive releases
- Feature flag evaluation APIs
- Audit logging
- Release monitoring
- API access for applications
- Secure JWT-based authentication

## Technology Stack

### Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway *(planned)*
- Maven

### Infrastructure

- Docker
- Docker Compose
- Redis *(planned)*
- GitHub Actions *(planned)*

### Frontend *(planned)*

- React
- TypeScript
- Tailwind CSS

### Future AI Integration

- OpenAI API
- AI-assisted release analysis and insights

## Local Development

### Prerequisites

Make sure the following are installed:

- Java 21
- Docker Desktop
- Git

### Start PostgreSQL

```bash
docker compose up -d
```

Check the database container:

```bash
docker compose ps
```

### Run the application

Right now, the verified way to run ReleasePilot locally is from IntelliJ IDEA with this VM option set:

```text
-Duser.timezone=Asia/Kolkata
```

The app hasn't been confirmed to run correctly without it, so treat it as required for now.

The Maven wrapper commands below are expected to work too, but they haven't been verified as a standalone startup path yet:

Using Maven:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
.\mvnw.cmd spring-boot:run
```

This section will be updated once a final local-run configuration is settled (e.g. moving the timezone setting into `application.yaml`).

The application runs on `http://localhost:8080`.

### Run tests

Linux/macOS:

```bash
./mvnw test
```

Windows:

```bash
.\mvnw.cmd test
```

## Project Structure

The backend will follow a feature-oriented architecture as the project grows:

```text
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── releasepilot/
│   │           ├── auth/
│   │           ├── organization/
│   │           ├── project/
│   │           ├── featureflag/
│   │           ├── rollout/
│   │           ├── audit/
│   │           └── common/
│   │
│   └── resources/
│       ├── application.yaml
│       └── db/
│           └── migration/
│
└── test/
```

The exact structure will evolve as features are implemented.

## Development Approach

ReleasePilot is being developed incrementally using the following workflow:

1. Understand the requirement
2. Design the solution
3. Implement the feature
4. Test the implementation
5. Document the change
6. Commit the change

The project prioritizes:

- Clean code
- SOLID principles
- Separation of concerns
- DTO-based APIs
- Validation
- Centralized exception handling
- Database migrations
- Secure authentication
- Reproducible development environments
- Production-oriented design

## Documentation

Development progress is tracked in [`DEVLOG.md`](./DEVLOG.md).

Additional technical documentation will be added under `docs/` as the project grows.

## License

License will be added later.
