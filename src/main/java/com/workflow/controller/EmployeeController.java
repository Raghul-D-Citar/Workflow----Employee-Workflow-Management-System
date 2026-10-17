package com.workflow.controller;

import com.workflow.dto.employee.EmployeeRequest;
import com.workflow.dto.employee.EmployeeResponse;
import com.workflow.dto.employee.EmployeeUpdateRequest;
import com.workflow.entity.Employee;
import com.workflow.service.EmployeeService;
import com.workflow.exception.InvalidRequestException;
import com.workflow.security.CustomUserDetails;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "Employee profiles and administration")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @Operation(summary = "List employees", description = "Lists all employees. Requires MANAGER or ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<EmployeeResponse>> getEmployees() {
        List<EmployeeResponse> employees = employeeService.getEmployees()
                .stream()
                .map(EmployeeResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Returns the authenticated employee profile.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<EmployeeResponse> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new InvalidRequestException("Authenticated user details not found");
        }
        Employee employee = employeeService.getEmployee(userDetails.getId());
        return ResponseEntity.ok(EmployeeResponse.fromEntity(employee));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an employee", description = "Returns an employee by ID. Requires JWT authentication.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployee(id);
        return ResponseEntity.ok(EmployeeResponse.fromEntity(employee));
    }

    @PostMapping
    @Operation(summary = "Create an employee", description = "Creates an employee. Requires ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        Employee employee = request.toEntity();
        Employee created = employeeService.createEmployee(employee, request.getDepartmentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeResponse.fromEntity(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an employee", description = "Updates an employee. Requires ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id,
                                                           @Valid @RequestBody EmployeeUpdateRequest request) {
        Employee employee = request.toEntity();
        Employee updated = employeeService.updateEmployee(id, employee, request.getDepartmentId());
        return ResponseEntity.ok(EmployeeResponse.fromEntity(updated));
    }
}
