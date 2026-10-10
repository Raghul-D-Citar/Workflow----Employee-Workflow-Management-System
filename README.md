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
### Phase 4 — REST API Layer
- **Objective**: Expose standard REST endpoints and request/response DTOs for the Employee, Leave, and Task services with validation and centralized error handling.
- **What was implemented**:
  - Implemented REST controllers: `EmployeeController`, `LeaveController`, and `TaskController`.
  - Created encapsulated request and response DTOs under `com.workflow.dto` preventing JPA entity leaking.
  - Implemented Jakarta Bean Validation on incoming DTOs (`@NotNull`, `@NotBlank`, `@Size`, `@Email`).
  - Added centralized error handling via `GlobalExceptionHandler` (`@RestControllerAdvice`) returning uniform `ErrorResponse` payloads.
  - Added lookup methods `getLeaveRequest` and `getTask` to `LeaveService` and `TaskService` respectively.
  - Added controller unit tests using TestNG, MockMvc, and Mockito.
- **Endpoint List**:
  - **Employees**:
    - `GET /api/employees`: List all employees (Status `200 OK`, returns `List<EmployeeResponse>`).
    - `GET /api/employees/{id}`: Retrieve employee by ID (Status `200 OK`, returns `EmployeeResponse`, `404 Not Found` if missing).
    - `POST /api/employees`: Create new employee (Status `201 Created`, accepts `EmployeeRequest`, returns `EmployeeResponse`, `400 Bad Request` on validation failure, `409 Conflict` on duplicate).
    - `PUT /api/employees/{id}`: Update employee details (Status `200 OK`, accepts `EmployeeUpdateRequest`, returns `EmployeeResponse`).
  - **Leaves**:
    - `POST /api/leaves`: Submit new leave request (Status `201 Created`, accepts `LeaveCreateRequest`, returns `LeaveResponse`).
    - `GET /api/leaves/my`: Retrieve employee's leave requests (Status `200 OK`, accepts `employeeId` query/header, returns `List<LeaveResponse>`).
    - `GET /api/leaves/pending`: Retrieve all pending leave requests (Status `200 OK`, returns `List<LeaveResponse>`).
    - `GET /api/leaves/{id}`: Retrieve leave request by ID (Status `200 OK`, returns `LeaveResponse`, `404 Not Found` if missing).
    - `PUT /api/leaves/{id}/approve`: Approve pending leave request (Status `200 OK`, accepts `LeaveDecisionRequest`, returns `LeaveResponse`, `400 Bad Request` if insufficient balance or not pending).
    - `PUT /api/leaves/{id}/reject`: Reject pending leave request (Status `200 OK`, accepts `LeaveDecisionRequest`, returns `LeaveResponse`, `400 Bad Request` if not pending).
  - **Tasks**:
    - `POST /api/tasks`: Create a new unassigned task (Status `201 Created`, accepts `TaskCreateRequest`, returns `TaskResponse`).
    - `GET /api/tasks/my`: Retrieve tasks assigned to employee (Status `200 OK`, accepts `employeeId` query/header, returns `List<TaskResponse>`).
    - `GET /api/tasks/{id}`: Retrieve task by ID (Status `200 OK`, returns `TaskResponse`, `404 Not Found` if missing).
    - `GET /api/tasks/team`: Retrieve department tasks (Status `200 OK`, accepts `departmentId` query param, returns `List<TaskResponse>`).
    - `PUT /api/tasks/{id}/status`: Progress task through valid lifecycle states (Status `200 OK`, accepts `TaskStatusUpdateRequest`, returns `TaskResponse`, `400 Bad Request` on invalid state transition).
    - `PUT /api/tasks/{id}/assign`: Assign task to an employee (Status `200 OK`, accepts `assigneeId` query param, returns `TaskResponse`).
- **Validation & Status Codes**:
  - Inputs validated with `@Valid` on controller methods; field errors formatted in `ErrorResponse.validationErrors`.
  - HTTP `200 OK`, `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict`, `500 Internal Server Error`.
- **Tests**:
  - `EmployeeControllerTest` (7 tests covering listing, retrieval, creation, validation error, conflict, and update).
  - `LeaveControllerTest` (11 tests covering creation, validation error, retrieval, pending query, approval, rejection, insufficient balance, and invalid transition).
  - `TaskControllerTest` (10 tests covering creation, validation error, retrieval, team query, assignment, and status transitions).
- **Current status**: Complete.

### Phase 5 — JWT Authentication
- **Objective**: Implement stateless JWT authentication using Spring Security, including token issuance, validation, password hashing, and endpoint authorization filters.
- **Authentication Flow**:
  1. **Registration** (`POST /api/auth/register`):
     - Validates payload (`username`, `email`, `password`, `firstName`, `lastName`).
     - Checks uniqueness of username and email.
     - Hashes password using `BCryptPasswordEncoder`.
     - Assigns role (defaults to `Role.EMPLOYEE` if omitted).
     - Automatically provisions default `LeaveBalance` (15 annual, 10 sick, 5 casual).
     - Issues a signed JWT and returns `AuthResponse`.
  2. **Login** (`POST /api/auth/login`):
     - Validates credentials via `AuthenticationManager`.
     - Generates HMAC-SHA256 signed JWT containing `username`, `role`, and `employeeId` claims.
     - Returns `AuthResponse` (`token`, `tokenType: "Bearer"`, user details).
- **JWT Lifecycle**:
  - Generated using JJWT 0.12 (`Jwts.builder()`, signed with HMAC-SHA256).
  - Configurable expiration (default 24 hours, test 1 hour).
  - Validated by `JwtTokenProvider.validateToken()` with secret key check and expiration verification.
- **Password Hashing**:
  - Passwords hashed using `BCryptPasswordEncoder` bean.
- **Security Filter & Stateless Session**:
  - `JwtAuthenticationFilter` intercepts requests, parses `Authorization: Bearer <token>`, validates the token, extracts `username`, loads `UserDetails` via `CustomUserDetailsService`, and sets authentication in `SecurityContextHolder`.
  - Application session management configured to `SessionCreationPolicy.STATELESS`.
  - Custom `JwtAuthenticationEntryPoint` formats unauthenticated rejections as consistent JSON (`401 Unauthorized`).
- **Public & Protected Endpoints**:
  - **Public**: `/api/auth/**` (register, login) and `/api/health`.
  - **Protected**: `/api/employees/**`, `/api/leaves/**`, `/api/tasks/**` (requires `Authorization: Bearer <token>`).
- **Configuration**:
  - Externalized via `jwt.secret` (env `JWT_SECRET`) and `jwt.expiration-ms` (env `JWT_EXPIRATION_MS`).
- **Tests**:
  - `AuthServiceTest` (5 tests covering registration, duplicate handling, login, and bad credentials).
  - `AuthControllerTest` (5 tests covering register/login request/response and bean validation).
  - `JwtSecurityIntegrationTest` (5 tests verifying public endpoint access, 401 on unauthenticated access, 401 on invalid token, 200 with valid token, and full end-to-end register/login/authenticated flow).
- **Current status**: Complete.

## 17. Future Enhancements
*(To be defined)*
