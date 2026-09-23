# WorkFlow — Employee Workflow Management System

## 1. Project Overview
WorkFlow is an employee workflow management platform demonstrating backend engineering best practices. It enables employees to manage leave requests and tasks, and provides administrative and managerial oversight functionality.

## 2. Objectives
Build a clean, robust, and understandable Spring Boot REST API application featuring secure authentication, role-based authorization, and basic business workflows (leaves and tasks).

## 3. Features
- Employee management and authentication
- Role-based authorization (EMPLOYEE, MANAGER, ADMIN)
- Leave request workflow management
- Task assignment and tracking workflow

## 4. Architecture
The application follows a standard layered architecture:
- Controller (REST API endpoints)
- Service (Business logic)
- Repository (Data access)
- JPA/Hibernate (ORM)
- PostgreSQL (Database)

## 5. Technology Stack
- Java 21
- Spring Boot 3
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- Maven

## 6. Project Structure
- `com.workflow.config`: Application and security configuration
- `com.workflow.controller`: REST APIs
- `com.workflow.dto`: Data Transfer Objects for API requests/responses
- `com.workflow.entity`: JPA Entities
- `com.workflow.exception`: Centralized exception handling
- `com.workflow.repository`: Data access interfaces
- `com.workflow.security`: Authentication and authorization logic
- `com.workflow.service`: Core business logic

## 7. Database Design
- **departments**: Core department details.
- **employees**: User credentials, roles, and department mapping.
- **leave_balances**: 1-to-1 mapping with employees tracking available leave days.
- **leave_requests**: Records of leave, associated with an employee and optionally a reviewing manager.
- **tasks**: Workflow items with assignees, creators, priorities, and statuses.

## 8. API Documentation
*(To be implemented in later phases)*

## 9. Authentication and Authorization
*(To be implemented in later phases)*

## 10. Business Workflows
*(To be implemented in later phases)*

## 11. Testing Strategy
*(To be implemented in later phases)*

## 12. Frontend
*(To be implemented in later phases)*

## 13. Setup and Installation
*(To be documented as project progresses)*

## 14. Environment Configuration
Configuration is managed via Spring profiles (e.g., `dev`) and environment variables:
- `DB_URL`: PostgreSQL JDBC URL
- `DB_USERNAME`: Database user
- `DB_PASSWORD`: Database password

## 15. Running the Application
Use Maven to start the application:
```bash
./mvnw spring-boot:run
```

## 16. Development Phases

### Phase 0 — Architecture and Project Foundation
- **Objective**: Establish the core Spring Boot project, define package structure, and configure database connections.
- **What was implemented**:
  - Initialized Spring Boot project with Java 21.
  - Configured PostgreSQL driver and Spring Data JPA.
  - Established base package structure.
  - Added a basic `/api/health` endpoint.
- **Important technical decisions**: 
  - Standard multi-layer architecture packages set up.
  - Externalized database configuration using environment variables.
- **Files/modules added or changed**:
  - Created base packages (config, controller, dto, entity, exception, repository, security, service).
  - Added `HealthController`.
  - Added `SecurityConfig`.
  - Created `application.yml` and `application-dev.yml`.
- **Database changes**: None yet. Configured connection structure.
- **APIs added/changed**: Added `GET /api/health`.
- **Tests added**: Default Spring Boot context load test.
- **Verification performed**: Maven compilation succeeds. Basic structure is in place.
- **Current status**: Complete.

### Phase 3 — Service Layer and Business Workflows
- **Objective**: Introduce application services and enforce core business rules for employees, leaves, and tasks.
- **What was implemented**:
  - Added service layer contracts and implementations for employee, leave, and task workflows.
  - Added business exceptions for missing resources, invalid requests, duplicate data, invalid task transitions, and insufficient leave balance.
  - Implemented leave creation, retrieval, approval, and rejection flows with transactional balance updates.
  - Implemented task creation, assignment, status transitions, and team/assigned task retrieval.
  - Implemented employee creation, lookup, updates, and department association logic.
  - Added service-level tests using TestNG and Mockito for the key business rules.
- **Important technical decisions**:
  - Used constructor-based dependency injection for all services.
  - Kept business rules in the service layer so controllers can remain thin in later phases.
  - Enforced valid leave and task state transitions in code rather than relying on callers.
  - Used transactional methods for operations that update more than one record.
- **Files/modules added or changed**:
  - Created `com.workflow.service` interfaces and `com.workflow.service.impl` implementations.
  - Created `com.workflow.exception` business exception hierarchy.
  - Added TestNG/Mockito service tests under `src/test/java/com/workflow/service`.
  - Updated `pom.xml` to support TestNG execution.
- **Database changes**: No new tables or columns; this phase uses the existing entity model and repository layer.
- **APIs added/changed**: None yet. This phase prepares the business logic that future controllers will expose.
- **Tests added**: Service tests covering valid leave creation, invalid leave dates, leave approval, leave rejection, insufficient leave balance, and invalid task transitions.
- **Verification performed**: Targeted Maven test run for the new service tests passed.
- **Current status**: Complete.

## 17. Future Enhancements
*(To be defined)*
