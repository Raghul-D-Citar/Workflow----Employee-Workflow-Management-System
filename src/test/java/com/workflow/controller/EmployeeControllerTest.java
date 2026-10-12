package com.workflow.controller;

import com.workflow.entity.Department;
import com.workflow.entity.Employee;
import com.workflow.entity.enums.Role;
import com.workflow.exception.DuplicateResourceException;
import com.workflow.exception.GlobalExceptionHandler;
import com.workflow.exception.ResourceNotFoundException;
import com.workflow.service.EmployeeService;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EmployeeControllerTest {

    private EmployeeService employeeService;
    private EmployeeController employeeController;
    private MockMvc mockMvc;

    @BeforeMethod
    public void setUp() {
        employeeService = Mockito.mock(EmployeeService.class);
        employeeController = new EmployeeController(employeeService);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void getEmployeesReturnsList() throws Exception {
        Employee emp = sampleEmployee(1L, "alice", "alice@example.com");
        when(employeeService.getEmployees()).thenReturn(List.of(emp));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].email").value("alice@example.com"));
    }

    @Test
    public void getEmployeeByIdSuccess() throws Exception {
        Employee emp = sampleEmployee(1L, "bob", "bob@example.com");
        when(employeeService.getEmployee(1L)).thenReturn(emp);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("bob"));
    }

    @Test
    public void getEmployeeByIdNotFound() throws Exception {
        when(employeeService.getEmployee(99L)).thenThrow(new ResourceNotFoundException("Employee not found: 99"));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Employee not found: 99"))
                .andExpect(jsonPath("$.path").value("/api/employees/99"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    public void createEmployeeSuccess() throws Exception {
        String jsonPayload = """
                {
                    "username": "charlie",
                    "email": "charlie@example.com",
                    "password": "password123",
                    "firstName": "Charlie",
                    "lastName": "Brown",
                    "role": "EMPLOYEE",
                    "departmentId": 2
                }
                """;

        Employee created = sampleEmployee(5L, "charlie", "charlie@example.com");
        when(employeeService.createEmployee(any(Employee.class), eq(2L))).thenReturn(created);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.username").value("charlie"));
    }

    @Test
    public void createEmployeeValidationError() throws Exception {
        String invalidPayload = "{}";

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    public void createEmployeeDuplicateConflict() throws Exception {
        String jsonPayload = """
                {
                    "username": "existing",
                    "email": "existing@example.com",
                    "password": "password123",
                    "firstName": "First",
                    "lastName": "Last",
                    "role": "EMPLOYEE"
                }
                """;

        when(employeeService.createEmployee(any(Employee.class), any()))
                .thenThrow(new DuplicateResourceException("Username already exists: existing"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username already exists: existing"));
    }

    @Test
    public void updateEmployeeSuccess() throws Exception {
        String updatePayload = """
                {
                    "firstName": "UpdatedName"
                }
                """;

        Employee updated = sampleEmployee(1L, "alice", "alice@example.com");
        updated.setFirstName("UpdatedName");

        when(employeeService.updateEmployee(eq(1L), any(Employee.class), any())).thenReturn(updated);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("UpdatedName"));
    }

    private Employee sampleEmployee(Long id, String username, String email) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setUsername(username);
        employee.setEmail(email);
        employee.setPassword("secret");
        employee.setFirstName("First");
        employee.setLastName("Last");
        employee.setRole(Role.EMPLOYEE);
        Department dept = new Department("IT", "Information Tech");
        dept.setId(10L);
        employee.setDepartment(dept);
        return employee;
    }
}
