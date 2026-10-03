# ReleasePilot Development Log

## What We're Building

ReleasePilot is a **feature flag and progressive rollout platform** — a production-oriented SaaS with a React dashboard and Spring Boot backend that helps teams safely release, control, and monitor application features.

### Target Architecture

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

---

## Project Status

**Overall: ✅ 100% Complete**

ReleasePilot is feature-complete for all core and advanced scope. Includes targeting rules engine, percentage rollouts, AI insights, audit logging, and modern React management dashboard.

| Area | Status |
|------|--------|
| Backend API | ✅ Complete |
| Database (Flyway V1–V4) | ✅ Complete |
| JWT + SDK API key auth | ✅ Complete |
| Feature flags & evaluation engine | ✅ Complete |
| Targeting & Rollout Rule Editor | ✅ Complete |
| Audit logging (all mutations) | ✅ Complete |
| Redis caching | ✅ Complete (opt-in) |

| OpenAPI / Swagger UI | ✅ Complete |
| Actuator health checks | ✅ Complete |
| Integration tests (Testcontainers) | ✅ Complete |
| React dashboard (CRUD + Rule Editor) | ✅ Complete |
| Playwright E2E tests | ✅ Complete |
| Docker + CI | ✅ Complete |
| README & docs | ✅ Complete |

---

## Technology Stack

| Layer | Stack |
|-------|-------|
| Backend | Java 17/21, Spring Boot 4.1, Security, JPA, Flyway, Redis, Actuator, springdoc-openapi 3.1 |
| Frontend | React 19, TypeScript, Tailwind CSS 4, Vite, React Router, Playwright |
| Data | PostgreSQL 17 |
| Infra | Docker Compose, GitHub Actions, multi-stage Dockerfile |

---

## API Surface

| Module | Base Path | Auth |
|--------|-----------|------|
| Auth | `/api/v1/auth` | Public |
| Organizations | `/api/v1/organizations` | JWT |
| Projects | `/api/v1/projects` | JWT |
| Feature Flags | `/api/v1/flags` | JWT |
| SDK Evaluation | `/api/v1/eval` | API Key |
| Audit Logs | `/api/v1/audit-logs` | JWT |
| Insights | `/api/v1/insights` | JWT |
| OpenAPI | `/swagger-ui.html` | Public |
| Health | `/actuator/health` | Public |

---

## Frontend Pages

| Page | Route | Capabilities |
|------|-------|-------------|
| Dashboard | `/` | Stats, quick-start guide |
| Projects | `/projects` | Create projects, view/regenerate API keys |
| Flags | `/flags` | Create, toggle, delete flags |
| Flag Detail | `/flags/:id` | Per-environment toggles, targeting rules editor, percentage rollout slider, release insights |
| Sandbox | `/sandbox` | Live SDK evaluation |
| Audit Log | `/audit` | Organization audit trail |
| SDK Code | `/code` | curl / JS / Java snippets |
| Login | `/login` | Register & sign-in |

---

## Changelog

### 2026-08-10 — Foundation & Backend Core

- Environment setup (Java 17/21, Spring Boot 4.1, PostgreSQL 17, Flyway V1–V4).
- Full backend domain layer: auth, orgs, projects, flags, evaluation, audit.
- JWT security, SDK evaluation API, deterministic percentage rollouts.
- Timezone centralized in `application.yaml`.

### 2026-08-19 — Infrastructure & Platform Hardening

- Centralized `AuditLogWriter` for all mutation types.
- Testcontainers integration tests (auth + evaluation flows).
- React frontend scaffold, GitHub Actions CI, Docker full stack.

### 2026-08-19 — MVP & Rules Engine Completion

- **OpenAPI / Swagger UI** — springdoc-openapi 3.1, `OpenApiConfig` with JWT + API key schemes.
- **Actuator** — `/actuator/health` and `/actuator/info` exposed.
- **Flag update API** — `PATCH /api/v1/flags/{id}` for name, enabled, default value.
- **In-UI Targeting & Rollout Rule Editor** — Full management of user attribute targeting rules and percentage rollout sliders in `FlagDetail.tsx`.
- **Full frontend CRUD** — Projects page, flag create/edit/delete, flag detail with env toggles and rule modals.
- **Test Compatibility** — Java 17 pom compatibility and `spring-boot-resttestclient` for integration tests.
- **Playwright E2E** — login page smoke tests.
- **Documentation & CI** — README rewritten, `.env.example`, `.java-version`, GitHub Actions CI.

---

## Deployment

```bash
# Infrastructure only
docker compose up -d postgres redis

# Full backend stack
docker compose up -d --build

# Frontend (dev)
cd frontend && npm install && npm run dev
```

Copy [`.env.example`](.env.example) and set `JWT_SECRET` before any production deployment.

---

## Future Enhancements (Post-Release)

- Role-based access control (viewer/editor/admin per org)
- Webhook notifications on flag changes
- Grafana/Prometheus metrics export
- Multi-region deployment guide
- Official SDK packages (npm, Maven)
