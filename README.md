# University Enterprise Resource Planning System

Spring Boot | REST APIs | PostgreSQL

## Overview

This repository contains the backend implementation of a University ERP system, built using Spring Boot and RESTful APIs with PostgreSQL as the database. The backend is fully implemented, tested, and functional, providing all core services, business logic, and persistence required for an ERP system.

The frontend is currently under development and will consume these APIs for user interaction.

## Project Status

 Backend: Ready and working

 Frontend: In progress (API integration pending)

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
