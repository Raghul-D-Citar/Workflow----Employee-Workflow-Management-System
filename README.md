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
For the local Windows setup, start Docker Desktop first. Then create or start the PostgreSQL container:

```powershell
docker start workflow-postgres
```

If the container does not exist, create it instead:

```powershell
docker run --name workflow-postgres `
    -e POSTGRES_DB=workflow_db `
    -e POSTGRES_USER=postgres `
    -e POSTGRES_PASSWORD=postgres `
    -p 15432:5432 `
    -v workflow-postgres-data:/var/lib/postgresql `
    -d postgres
```

The application uses host port `15432` because another PostgreSQL installation may already use host port `5432`.

In a new PowerShell window, configure the database connection and start Spring Boot on port `8081`:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:15432/workflow_db"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "postgres"

.\mvnw.cmd spring-boot:run `
    "-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC" `
    "-Dspring-boot.run.arguments=--server.port=8081"
```

Keep this terminal open while using the application. Verify the health endpoint from another PowerShell window:

```powershell
Invoke-WebRequest http://localhost:8081/api/health
```

To open Swagger UI from PowerShell, use `Start-Process`; do not type the URL as if it were a command:

```powershell
Start-Process "http://localhost:8081/swagger-ui.html"
```

The OpenAPI JSON document is available at:

```powershell
Invoke-WebRequest http://localhost:8081/v3/api-docs
```

The shorter Maven command below works when the database is already available, the required environment variables are set, and port `8080` is free:

```powershell
.\mvnw.cmd spring-boot:run
```

## 16. Creating Development Users
The application supports these roles:

| Role | Typical access |
|------|----------------|
| `EMPLOYEE` | View profile, submit leave requests, view assigned tasks, and update assigned task status |
| `MANAGER` | Review leave requests, view team data, create tasks, assign tasks, and manage team tasks |
| `ADMIN` | Manager capabilities plus employee administration |

For local development, users can be created through `POST /api/auth/register`. The following PowerShell script creates one user for each role. Change the passwords before using these accounts outside local development.

Run the application first, then execute:

```powershell
$baseUrl = "http://localhost:8081"

try {
    Invoke-RestMethod -Uri "$baseUrl/api/health" -Method Get | Out-Null
}
catch {
    throw "WorkFlow is not reachable at $baseUrl. Start Spring Boot on this port before creating users."
}

function New-WorkflowUser {
    param(
        [string]$Username,
        [string]$Email,
        [string]$Password,
        [string]$FirstName,
        [string]$LastName,
        [string]$Role
    )

    $body = @{
        username  = $Username
        email     = $Email
        password  = $Password
        firstName = $FirstName
        lastName  = $LastName
        role      = $Role
    } | ConvertTo-Json

    Invoke-RestMethod `
        -Uri "$baseUrl/api/auth/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body
}

New-WorkflowUser `
    -Username "employee.demo" `
    -Email "employee.demo@example.com" `
    -Password "Employee@123" `
    -FirstName "Demo" `
    -LastName "Employee" `
    -Role "EMPLOYEE"

New-WorkflowUser `
    -Username "manager.demo" `
    -Email "manager.demo@example.com" `
    -Password "Manager@123" `
    -FirstName "Demo" `
    -LastName "Manager" `
    -Role "MANAGER"

New-WorkflowUser `
    -Username "admin.demo" `
    -Email "admin.demo@example.com" `
    -Password "Admin@123" `
    -FirstName "Demo" `
    -LastName "Administrator" `
    -Role "ADMIN"
```

You can then sign in through the frontend at `http://localhost:8081/` using:

| Role | Username | Password |
|------|----------|----------|
| Employee | `employee.demo` | `Employee@123` |
| Manager | `manager.demo` | `Manager@123` |
| Admin | `admin.demo` | `Admin@123` |

If the application is running on another port, update `$baseUrl` and the frontend URL accordingly. Registration is intended for development setup here; production deployments should restrict who can create privileged roles.

## 17. Development Phases

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

### Phase 6 — Role-Based Authorization
- **Objective**: Implement fine-grained role-based access control (RBAC) across endpoints and business operations using Spring Security.
- **Roles**:
  - `EMPLOYEE`: General staff member.
  - `MANAGER`: Team supervisor with review and assignment capabilities.
  - `ADMIN`: System administrator with full management access.
- **Permissions Matrix**:
  - **EMPLOYEE**:
    - View own profile (`GET /api/employees/me`).
    - Submit leave requests (`POST /api/leaves`).
    - View own leave history (`GET /api/leaves/my`).
    - View assigned tasks (`GET /api/tasks/my`).
    - Progress assigned task statuses (`PUT /api/tasks/{id}/status`).
  - **MANAGER**:
    - All employee read permissions plus:
    - View organization/department employees (`GET /api/employees`).
    - View pending leave requests (`GET /api/leaves/pending`).
    - Approve or reject leave requests (`PUT /api/leaves/{id}/approve`, `PUT /api/leaves/{id}/reject`).
    - Create and assign workflow tasks (`POST /api/tasks`, `PUT /api/tasks/{id}/assign`).
    - View team tasks (`GET /api/tasks/team`).
  - **ADMIN**:
    - All manager permissions plus:
    - Full employee lifecycle management (`POST /api/employees`, `PUT /api/employees/{id}`).
- **Authorization Strategy**:
  - Enabled `@EnableMethodSecurity` and configured route-level matchers in `SecurityConfig`.
  - Roles granted as `ROLE_EMPLOYEE`, `ROLE_MANAGER`, `ROLE_ADMIN` on `CustomUserDetails`.
  - Configured `JwtAccessDeniedHandler` to intercept unauthorized access attempts and return uniform `403 Forbidden` JSON responses.
  - Configured `JwtAuthenticationEntryPoint` to return uniform `401 Unauthorized` JSON responses for unauthenticated requests.
- **Security Tests**:
  - `JwtSecurityIntegrationTest` (9 integration tests covering):
    - Unauthenticated request rejection (`401 Unauthorized`).
    - Invalid JWT rejection (`401 Unauthorized`).
    - Employee allowed operations (`GET /api/employees/me`, `GET /api/leaves/my`, `GET /api/tasks/my` returning `200 OK`).
    - Employee forbidden from manager operations (`403 Forbidden` on `GET /api/leaves/pending`, `PUT /api/leaves/{id}/approve`, `POST /api/tasks`, `PUT /api/tasks/{id}/assign`, `GET /api/employees`).
    - Manager allowed operations (`200 OK` on `GET /api/employees`, `GET /api/leaves/pending`, and `201 Created` on `POST /api/tasks`).
    - Manager forbidden from administrative operations (`403 Forbidden` on `POST /api/employees`).
    - Admin allowed administrative operations (`201 Created` on `POST /api/employees` and `200 OK` on `GET /api/employees`).
    - Public health endpoint check (`200 OK`).
    - End-to-end registration, duplicate rejection, and login flow.
- **Current status**: Complete.

### Phase 7 — Validation and Centralized Exception Handling
- **Objective**: Validate incoming API requests and return consistent, safe error responses.
- **What was implemented**:
  - Applied Jakarta Bean Validation constraints to request DTOs for required fields, formats, sizes, and future-valid dates.
  - Added validation for leave start/end dates and task due dates so past dates are rejected at the API boundary.
  - Added domain-specific not-found and workflow exceptions for employees, leave requests, tasks, insufficient leave balance, and invalid leave transitions.
  - Added `UnauthorizedActionException` for unauthorized business operations.
  - Extended `GlobalExceptionHandler` to handle business errors, validation failures, constraint violations, and malformed request bodies.
  - Ensured unexpected server errors return a generic message rather than exception details or stack traces.
  - Added controller tests for missing fields, invalid dates, nonexistent resources, invalid workflow transitions, and forbidden business operations.
- **Error response format**:
  - Responses include `timestamp`, `status`, `error`, `message`, and `path`.
  - Validation responses additionally include `validationErrors` when field-level errors are available.
- **Current status**: Complete.

### Phase 8 — Automated Testing and Quality
- **Objective**: Perform a dedicated automated testing pass over service, controller, security, and persistence behavior.
- **Testing strategy**:
  - TestNG and Mockito are used for service and controller-focused tests.
  - MockMvc verifies HTTP status codes, validation behavior, authorization responses, and response structures.
  - Spring Boot integration tests use H2 to exercise the JPA repositories and the complete security filter chain.
  - The repository integration tests validate actual JPA mappings and custom JPQL queries. They do not claim PostgreSQL-specific coverage.
- **Unit tests**:
  - Employee creation, retrieval, update, duplicate handling, and missing-resource behavior.
  - Leave creation, invalid date handling, approval, rejection, insufficient balance, invalid transitions, missing requests, and persistence-sensitive failure behavior.
  - Task creation, assignment, valid status transitions, invalid status transitions, and invalid creation with an assignee.
  - Authentication registration and login behavior.
- **Controller tests**:
  - Successful requests and response structures.
  - Required-field and date validation failures.
  - Malformed JSON handling.
  - Not-found, conflict, invalid workflow, and unauthorized business-operation responses.
- **Security tests**:
  - Unauthenticated access returns `401 Unauthorized`.
  - Invalid JWT access returns `401 Unauthorized`.
  - Authenticated users without sufficient roles return `403 Forbidden`.
  - Employee, manager, and admin permissions are exercised through the real Spring Security filter chain.
- **Integration tests**:
  - Spring context loading.
  - H2-backed repository queries for departments, employees, leave requests, and tasks.
  - End-to-end registration, duplicate registration, login, and role-protected API access.
- **Commands used**:
  - `mvnw.cmd test`
- **Actual result**:
  - Full Maven test suite passed with zero failures and zero errors.
  - No coverage percentage is claimed because coverage collection was not configured or measured.
- **Current status**: Complete.

### Phase 9 — JavaScript Frontend
- **Objective**: Add a lightweight HTML, CSS, and JavaScript frontend using the existing REST API without introducing React.
- **Frontend structure**:
  - `src/main/resources/static/index.html`: login form and role-aware dashboard markup.
  - `src/main/resources/static/styles.css`: responsive, utility-focused styling.
  - `src/main/resources/static/app.js`: authentication, API client, rendering, form handling, and dashboard actions.
- **API integration**:
  - The Fetch API calls the existing authentication, employee, leave, and task endpoints.
  - Requests automatically include `Authorization: Bearer <JWT>` after login.
  - Backend validation and authorization remain authoritative; frontend checks are only for usability.
  - API validation and authorization errors are displayed to the user, including field-level validation messages when returned.
- **Authentication flow**:
  - Users sign in through `POST /api/auth/login`.
  - The returned authentication response is stored in browser `localStorage` under `workflow.auth`.
  - The token is restored on page reload and removed on sign out or HTTP 401.
- **Dashboards**:
  - Employees can view their profile, submit leave, review leave history, view assigned tasks, and advance task status.
  - Managers can view team employees, pending leave requests, approve/reject leave, create tasks, assign tasks, and view team tasks.
  - Administrators receive manager functionality plus employee creation.
  - Role-based visibility is a UX feature only; API security remains enforced by Spring Security.
- **How to run**:
  - Start the application with `mvnw.cmd spring-boot:run`.
  - Open `http://localhost:8080/` in a browser.
  - Configure the backend database and JWT environment variables as described above.
- **Current status**: Complete.

### Phase 11 — API Documentation
- **Objective**: Document the REST API and make authenticated endpoints easy to explore.
- **What was implemented**:
  - Added OpenAPI 3 documentation with Springdoc Swagger UI.
  - Documented authentication, employee, leave, task, and health endpoints.
  - Exposed request and response DTO schemas through the generated OpenAPI document.
  - Added a Bearer JWT security scheme so protected operations clearly show their authentication requirement.
  - Kept Swagger UI and the OpenAPI JSON endpoint publicly accessible without changing API business behavior.
- **Documentation URLs**:
  - Swagger UI: `http://localhost:8081/swagger-ui.html`
  - OpenAPI JSON: `http://localhost:8081/v3/api-docs`
  - If the application uses another port, replace `8081` in these URLs.
- **Using JWT in Swagger UI**:
  1. Call `POST /api/auth/login` and copy the `token` from the response.
  2. Select **Authorize** in Swagger UI.
  3. Enter `Bearer <token>` and select **Authorize**.
  4. Swagger UI will send the JWT with protected requests.
- **Current status**: Complete.

## 19. Future Enhancements
*(To be defined)*
