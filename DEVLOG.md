# ReleasePilot Development Log

## Project Overview

ReleasePilot is a feature flag and progressive rollout platform designed to provide controlled feature releases, targeted rollouts, and safer deployments.

---

## Step 1 — Project Setup

### What We Built

- Created the ReleasePilot Spring Boot project using Spring Initializr.
- Configured Java 21 LTS.
- Configured Maven as the build tool.
- Configured Spring Boot 4.1.0.
- Added Spring Web.
- Added Spring Data JPA.
- Added Spring Security.
- Added Validation.
- Added PostgreSQL Driver.
- Added Lombok.
- Added Spring Boot DevTools.
- Configured the project to use `application.yml`.

### Use Case

Provides the initial backend foundation for ReleasePilot, including web APIs, database access, security, validation, and development tooling.

---

## Step 2 — PostgreSQL + Docker

### What We Built

- Added PostgreSQL 17 using Docker Compose.
- Created the `releasepilot` PostgreSQL database.
- Created the `releasepilot` database user.
- Exposed PostgreSQL on port `5432`.
- Added a persistent Docker volume for PostgreSQL data.
- Configured Spring Boot to connect to PostgreSQL.
- Configured Hibernate to validate the database schema instead of modifying it automatically.
- Verified the Spring Boot application can connect to PostgreSQL successfully.

### Use Case

Provides the persistent relational database required to store ReleasePilot application data such as users, organizations, projects, environments, feature flags, rollout configurations, and audit information.

---

## Next

- Make the JVM timezone configuration reproducible at the project level.
- Complete database setup.
- Add Flyway database migrations.