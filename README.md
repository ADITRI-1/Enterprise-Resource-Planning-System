# University Enterprise Resource Planning System

Spring Boot | REST APIs | PostgreSQL

## Overview

This repository contains the backend implementation of a University ERP system, built using Spring Boot and RESTful APIs with PostgreSQL as the database. The backend is fully implemented, tested, and functional, providing all core services, business logic, and persistence required for an ERP system.

The frontend is currently under development and will consume these APIs for user interaction.

## Project Status

 Backend: Spring Boot REST API, tested against real PostgreSQL

 Frontend: React + TypeScript web app in [`frontend/`](frontend/)

## Quick start (no database install needed)

Needs **Java 17+** and **Node 20+**.

    # terminal 1: backend + embedded PostgreSQL with demo data (http://localhost:8080)
    ./mvnw spring-boot:test-run -Dspring-boot.run.main-class=com.iiitd.erp.DevServer

    # terminal 2: web app (http://localhost:5173)
    cd frontend
    npm install
    npm run dev

Open http://localhost:5173 and use a demo account (buttons on the login page). The dev
database lives in `target/dev-postgres`; delete it to start over from the seed data.
To run against your own PostgreSQL instead, see *How to Run the Backend* below.

## Run with Docker

    docker compose up --build

Then open http://localhost:3000 (API on :8080). This runs PostgreSQL 16, the Spring Boot API and the
web app behind nginx. Schema and demo data are created on first start. Set `DB_PASSWORD` (environment
or a `.env` file) before using it anywhere but your own machine.

## Features

**Students**: dashboard with CGPA and today's classes, course catalog (live seats, schedule,
prerequisites with ✓/✗), register / drop, weekly timetable, grades with SGPA/CGPA by term,
transcript as PDF or CSV.

**Instructors**: inline gradebook with CSV import/export, assessment weights (must total 100%),
compute final grades, class statistics with grade distribution.

**Admins**: dashboard and maintenance switch, analytics (section fill, grade distribution,
enrollments by department, daily registrations), audit log, accounts (unlock / reset password),
create/edit/delete for students, instructors, courses (with prerequisites), sections (with a
schedule editor that only offers free rooms), rooms, departments and programs.

**Rules enforced by the API**: capacity (with row locking), duplicates, deadlines, prerequisites
(passed = any final grade but F, cycles rejected), timetable clashes, instructor owns the section,
maintenance mode read-only, password policy + history, lockout after 5 failed logins.

**Everyone**: notification bell (registration, grades published, maintenance), maintenance
banner, forced password change after a temporary password, dark mode, phone layouts.

Grades: total = Σ(score ÷ out of × weight) × 100 → A ≥ 90, A- ≥ 80, B ≥ 70, B- ≥ 60, C ≥ 50,
C- ≥ 40, D ≥ 30, else F. Grade points A 10, A- 9, B 8, B- 7, C 6, C- 5, D 4, F 0;
SGPA/CGPA = Σ(points × credits) ÷ Σ credits over graded courses.

## Frontend

React 18, TypeScript, Vite, Tailwind CSS, TanStack Query, React Router, Recharts.

The dev server proxies `/api` to `localhost:8080`, so the session cookie stays same-origin.
API types in `frontend/src/api/schema.d.ts` are generated from the backend's OpenAPI spec:
run `npm run gen:api` while the backend is running after changing the API.

## Tests

    ./mvnw test                      # backend: unit + integration tests on embedded PostgreSQL
    cd frontend
    npx playwright install chromium  # once
    npm run test:e2e                 # browser tests; starts its own backend (:8081) and web app (:5174)

The browser tests use a fresh database each run, so they never touch your dev data. GitHub Actions
(`.github/workflows/ci.yml`) runs all of these plus `docker compose build` on every push.

## Tech Stack

* Java

* Spring Boot

* Spring Web (REST APIs)

* Spring Data JPA

* PostgreSQL

* Maven

## Database

* Database Used: PostgreSQL

* Used for persistent storage of ERP entities such as users, courses, departments, assessments, and maintenance state.

* Database configuration is managed via Spring Boot properties.

* Schema handling is supported via DDL scripts and JPA entities.

## Project Structure
    .mvn/
    src/
    └── main/
        ├── java/
        │   └── com/iiitd/erp/
        │       ├── Domain/            # Core domain models / entities
        │       ├── config/            # Application and Spring configuration
        │       ├── core/
        │       │   └── service/       # Service layer (business logic)
        │       │       ├── dto/        # Data Transfer Objects
        │       │       ├── impl/       # Service implementations
        │       │       ├── support/    # Helper and support classes
        │       │       ├── AccessService.java
        │       │       ├── AssessmentService.java
        │       │       ├── AuthSessionService.java
        │       │       ├── AvailabilityService.java
        │       │       ├── CatalogService.java
        │       │       ├── CourseService.java
        │       │       ├── DepartmentService.java
        │       │       ├── InstructorService.java
        │       │       ├── MaintenanceService.java
        │       │       └── MaintenanceState.java
        │       ├── exception/         # Custom exceptions & global error handling
        │       ├── io/                # Input/Output utilities
        │       ├── mappers/           # DTO ↔ Entity mappers
        │       ├── persistent/        # JPA repositories & persistence layer
        │       ├── ui/                # Placeholder for frontend integration
        │       └── UniversityErpApplication.java  # Application entry point
        └── resources/
    └── test/

## Backend Responsibilities

* Course, department, and catalog management

* Instructor and assessment handling

* Availability and access control checks

* Maintenance mode management

* Authentication session handling

* Clean service-layer abstraction for frontend consumption

## How to Run the Backend
### Prerequisites

* Java JDK 17+

* Maven

* PostgreSQL installed and running

### Database Setup

Create an empty PostgreSQL database:

    CREATE DATABASE "universityERP";

The schema and demo data are applied automatically on startup by Flyway
(`src/main/resources/db/migration`). Connection settings come from environment
variables, with local defaults:

| Variable      | Default                                         |
|---------------|-------------------------------------------------|
| `DB_URL`      | `jdbc:postgresql://localhost:5432/universityERP` |
| `DB_USERNAME` | `postgres`                                      |
| `DB_PASSWORD` | `12345`                                         |

The DB user needs permission to `CREATE EXTENSION btree_gist` (used to stop a room
being double-booked). If you have a database from the old `ddl.sql`, drop and
recreate it; the old schema doesn't match the code.

### Demo accounts

| Username | Password    | Role       |
|----------|-------------|------------|
| admin1   | `Admin@123` | Admin      |
| inst1    | `Inst@123`  | Instructor (INS001, teaches CSE101 + CSE201) |
| inst2    | `Inst@123`  | Instructor (INS002, teaches MTH101) |
| stu1     | `Stu@123`   | Student (enrolled in CSE101, CSE201) |
| stu2     | `Stu@123`   | Student (enrolled in CSE201) |

After logging in, call `GET /api/student/me` or `GET /api/instructor/me` to get the
user id / instructor code used in the other student and instructor URLs.

### API docs

With the app running, open **http://localhost:8080/swagger-ui.html**. Every endpoint is listed
and can be tried in the browser: run `POST /api/auth/login` first and the session cookie is reused.

### Account security

* Accounts created by an admin get a random temporary password, returned once in the create
  response. Until the user changes it (`POST /api/auth/change-password`), every endpoint outside
  `/api/auth/**` answers `403 PASSWORD_CHANGE_REQUIRED`.
* Passwords need 8+ characters with a letter and a digit, must not contain the username, and
  can't repeat any of the last 3.
* 5 wrong passwords lock the account for 15 minutes (`423`). A wrong-password response includes
  `details.remainingAttempts`. Admins can `POST /api/admin/users/{username}/unlock` or
  `/reset-password`.

### Lists

Collection endpoints (admin lists, catalog, accounts) return
`{ items, page, size, totalItems, totalPages }` and accept `q` (search), `sort` (`title` or
`title,desc`), `page` (from 0) and `size` (max 200).

### Tests

    ./mvnw test

Integration tests start an embedded PostgreSQL, so no database setup is needed.

### Build the Project
    ./mvnw clean install

### Run the Application
    ./mvnw spring-boot:run


OR

    java -jar target/*.jar

## API Usage

* The backend exposes RESTful APIs designed to be consumed by a frontend (web / desktop / mobile).

* APIs can be tested using:

    * Postman

    * cURL

    * Browser (GET endpoints)

Default base URL:

    http://localhost:8080

#### Frontend Integration

* Frontend development is currently in progress.

* The ui package is reserved for future UI-related components.

* All business operations are exposed via service-backed REST endpoints.

#### Design Highlights

   * Layered architecture (Domain → Service → Persistence)

   * DTO-based API contracts

   * Centralized exception handling

   * PostgreSQL-backed persistence

   * Modular, scalable codebase suitable for large ERP systems

#### Future Enhancements

* Frontend UI implementation

* Role-based authentication UI

* Swagger/OpenAPI documentation

* Dockerized deployment (Spring Boot + PostgreSQL)

* Performance and security hardening
