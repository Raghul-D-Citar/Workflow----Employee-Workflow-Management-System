package com.workflow.security;

import com.workflow.entity.Employee;
import com.workflow.entity.enums.Role;
import com.workflow.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class JwtSecurityIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeMethod
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private Employee createTestEmployee(String prefix, Role role) {
        String username = prefix + "_" + System.currentTimeMillis();
        Employee emp = new Employee();
        emp.setUsername(username);
        emp.setEmail(username + "@example.com");
        emp.setPassword(passwordEncoder.encode("password123"));
        emp.setFirstName(prefix);
        emp.setLastName("User");
        emp.setRole(role);
        return employeeRepository.save(emp);
    }

    @Test
    public void publicHealthEndpointPermittedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    public void unauthenticatedRequestRejectedWith401() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    public void protectedEndpointWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    public void employeeAllowedOperationsSucceed() throws Exception {
        Employee emp = createTestEmployee("emp", Role.EMPLOYEE);
        String token = jwtTokenProvider.generateToken(emp.getUsername(), "EMPLOYEE", emp.getId());

        // 1. Employee can view their own profile
        mockMvc.perform(get("/api/employees/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(emp.getUsername()));

        // 2. Employee can view their own leaves
        mockMvc.perform(get("/api/leaves/my?employeeId=" + emp.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 3. Employee can view their own assigned tasks
        mockMvc.perform(get("/api/tasks/my?employeeId=" + emp.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void employeeForbiddenManagerOperationsReturn403() throws Exception {
        Employee emp = createTestEmployee("emp_forbidden", Role.EMPLOYEE);
        String token = jwtTokenProvider.generateToken(emp.getUsername(), "EMPLOYEE", emp.getId());

        // 1. Employee cannot view pending leaves (Manager/Admin only)
        mockMvc.perform(get("/api/leaves/pending")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));

        // 2. Employee cannot approve leave
        String approvePayload = """
                {
                    "reviewerId": %d,
                    "reviewComment": "Approved"
                }
                """.formatted(emp.getId());

        mockMvc.perform(put("/api/leaves/1/approve")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approvePayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // 3. Employee cannot create tasks (Manager/Admin only)
        String taskPayload = """
                {
                    "title": "Unauthorized Task",
                    "description": "Should fail",
                    "priority": "MEDIUM",
                    "creatorId": %d
                }
                """.formatted(emp.getId());

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskPayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // 4. Employee cannot assign tasks
        mockMvc.perform(put("/api/tasks/1/assign?assigneeId=2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // 5. Employee cannot view all employees
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    public void managerAllowedOperationsSucceed() throws Exception {
        Employee mgr = createTestEmployee("mgr", Role.MANAGER);
        String token = jwtTokenProvider.generateToken(mgr.getUsername(), "MANAGER", mgr.getId());

        // 1. Manager can view employees list
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 2. Manager can view pending leaves
        mockMvc.perform(get("/api/leaves/pending")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 3. Manager can create tasks
        String taskPayload = """
                {
                    "title": "Manager Task",
                    "description": "Created by manager",
                    "priority": "HIGH",
                    "creatorId": %d
                }
                """.formatted(mgr.getId());

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Manager Task"));
    }

    @Test
    public void managerForbiddenAdminOperationsReturn403() throws Exception {
        Employee mgr = createTestEmployee("mgr_admin_attempt", Role.MANAGER);
        String token = jwtTokenProvider.generateToken(mgr.getUsername(), "MANAGER", mgr.getId());

        // Manager cannot create employees directly (Admin only)
        String employeePayload = """
                {
                    "username": "new_user_%d",
                    "email": "new_user_%d@example.com",
                    "password": "Password123!",
                    "firstName": "New",
                    "lastName": "User",
                    "role": "EMPLOYEE"
                }
                """.formatted(System.currentTimeMillis(), System.currentTimeMillis());

        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeePayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    public void adminAllowedAdministrativeOperationsSucceed() throws Exception {
        Employee admin = createTestEmployee("admin", Role.ADMIN);
        String token = jwtTokenProvider.generateToken(admin.getUsername(), "ADMIN", admin.getId());

        long timestamp = System.currentTimeMillis();
        String createEmployeeJson = """
                {
                    "username": "created_by_admin_%d",
                    "email": "admin_created_%d@example.com",
                    "password": "Password123!",
                    "firstName": "Admin",
                    "lastName": "Created",
                    "role": "EMPLOYEE"
                }
                """.formatted(timestamp, timestamp);

        mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createEmployeeJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("created_by_admin_" + timestamp));

        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void registrationAndLoginFlowEndToEnd() throws Exception {
        String username = "flowuser_" + System.currentTimeMillis();
        String registerJson = """
                {
                    "username": "%s",
                    "email": "%s@example.com",
                    "password": "Password123!",
                    "firstName": "Flow",
                    "lastName": "Test",
                    "role": "EMPLOYEE"
                }
                """.formatted(username, username);

        // 1. Register new user
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(username));

        // 2. Duplicate registration fails
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        // 3. Login with valid credentials
        String loginJson = """
                {
                    "username": "%s",
                    "password": "Password123!"
                }
                """.formatted(username);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(username));

        // 4. Login with invalid credentials fails
        String invalidLoginJson = """
                {
                    "username": "%s",
                    "password": "WrongPassword!"
                }
                """.formatted(username);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLoginJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
