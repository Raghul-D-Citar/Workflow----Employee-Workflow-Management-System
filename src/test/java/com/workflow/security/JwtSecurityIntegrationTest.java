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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    public void publicHealthEndpointPermittedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    public void protectedEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    public void protectedEndpointWithInvalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    public void protectedEndpointWithValidTokenSucceeds() throws Exception {
        String username = "jwtuser_" + System.currentTimeMillis();
        Employee emp = new Employee();
        emp.setUsername(username);
        emp.setEmail(username + "@example.com");
        emp.setPassword(passwordEncoder.encode("password123"));
        emp.setFirstName("Jwt");
        emp.setLastName("User");
        emp.setRole(Role.EMPLOYEE);
        emp = employeeRepository.save(emp);

        String token = jwtTokenProvider.generateToken(username, "EMPLOYEE", emp.getId());

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
        String registerResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn().getResponse().getContentAsString();

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
