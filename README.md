# ReleasePilot

ReleasePilot is a **feature flag and progressive rollout platform** that helps teams safely release, control, and monitor application features across environments.

Built as a production-oriented SaaS with a Spring Boot backend, React dashboard, PostgreSQL, optional Redis caching, and OpenAI-powered release insights.

## Features

- **Authentication** — JWT register/login with auto-provisioned organization
- **Multi-tenancy** — organizations, projects, environments (dev/staging/prod auto-created)
- **Feature flags** — boolean, string, numeric, and JSON types
- **Targeting rules** — attribute-based rules with priority ordering
- **Percentage rollouts** — deterministic user bucketing via consistent hashing
- **SDK evaluation API** — public endpoints authenticated with per-environment API keys
- **Audit logging** — full trail of org/project/environment/flag mutations
- **Release insights** — traffic distribution analysis with optional OpenAI summaries
- **React dashboard** — manage projects, flags, sandbox evaluations, audit logs, SDK snippets
- **OpenAPI docs** — Swagger UI at `/swagger-ui.html`
- **Health checks** — Spring Actuator at `/actuator/health`

## Architecture

```text
                    ReleasePilot
                         │
              ┌──────────┴──────────┐
              │                     │
           Frontend              Backend
        React + TypeScript      Spring Boot 4
         (Vite + Tailwind)           │
              │              ┌──────┴──────┐
              │              │             │
              │           PostgreSQL     Redis
              │
              └────────────── API ─────────┘
```

## Tech Stack

| Layer | Technologies |
|-------|-------------|
| Backend | Java 21, Spring Boot 4.1, Spring Security, JPA, Flyway, Redis, Actuator, springdoc-openapi |
| Frontend | React 19, TypeScript, Tailwind CSS 4, Vite, React Router, Playwright |
| Data | PostgreSQL 17 |
| Infra | Docker Compose, GitHub Actions CI |

## Quick Start

### Prerequisites

- **Java 21** (required — see `.java-version`)
- **Node.js 20+** (for frontend)
- **Docker Desktop** (for PostgreSQL/Redis/full stack)

### 1. Start infrastructure

```bash
docker compose up -d postgres redis
```

### 2. Run backend

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

Backend: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`  
Health: `http://localhost:8080/actuator/health`

### 3. Run frontend

```bash
cd frontend
npm install
npm run dev
```

Dashboard: `http://localhost:5173`

### Full Docker stack

```bash
docker compose up -d --build
```

Runs postgres + redis + backend on `:8080`.

Copy `.env.example` to `.env` and customize secrets before production use.

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `REDIS_ENABLED` | `false` | Enable evaluation result caching |
| `OPENAI_ENABLED` | `false` | Enable AI release insight summaries |
| `OPENAI_API_KEY` | — | OpenAI API key |
| `JWT_SECRET` | dev default | JWT signing secret (**change in prod**) |

See [`application.yaml`](src/main/resources/application.yaml) and [`.env.example`](.env.example) for all options.

## API Overview

| Module | Path | Auth |
|--------|------|------|
| Auth | `/api/v1/auth` | Public |
| Organizations | `/api/v1/organizations` | JWT |
| Projects | `/api/v1/projects` | JWT |
| Feature Flags | `/api/v1/flags` | JWT |
| SDK Evaluation | `/api/v1/eval` | API Key (`X-API-Key`) |
| Audit Logs | `/api/v1/audit-logs` | JWT |
| Insights | `/api/v1/insights` | JWT |

## Testing

### Backend

```bash
.\mvnw.cmd test    # Windows
./mvnw test        # Linux/macOS
```

Uses Testcontainers (PostgreSQL) — Docker required for integration tests.

### Frontend E2E

```bash
cd frontend
npm install
npx playwright install chromium
npm run test:e2e
```

## Project Structure

```text
releasepilot/
├── src/main/java/com/releasepilot/   # Backend (feature-oriented packages)
├── src/test/                         # Unit + integration tests
├── src/main/resources/db/migration/  # Flyway migrations V1–V4
├── frontend/                         # React dashboard
├── docker-compose.yaml               # postgres + redis + app
├── Dockerfile                        # Backend container
├── .github/workflows/ci.yml          # CI pipeline
└── DEVLOG.md                         # Development log
```

## Development

Progress and decisions are tracked in [`DEVLOG.md`](./DEVLOG.md).

Workflow: design → implement → test → document → commit.

## License

License will be added later.
