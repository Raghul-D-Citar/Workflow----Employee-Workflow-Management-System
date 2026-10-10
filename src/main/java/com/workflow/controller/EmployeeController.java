package com.workflow.controller;

import com.workflow.dto.employee.EmployeeRequest;
import com.workflow.dto.employee.EmployeeResponse;
import com.workflow.dto.employee.EmployeeUpdateRequest;
import com.workflow.entity.Employee;
import com.workflow.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getEmployees() {
        List<EmployeeResponse> employees = employeeService.getEmployees()
                .stream()
                .map(EmployeeResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        Employee employee = employeeService.getEmployee(id);
        return ResponseEntity.ok(EmployeeResponse.fromEntity(employee));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        Employee employee = request.toEntity();
        Employee created = employeeService.createEmployee(employee, request.getDepartmentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeResponse.fromEntity(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id,
                                                           @Valid @RequestBody EmployeeUpdateRequest request) {
        Employee employee = request.toEntity();
        Employee updated = employeeService.updateEmployee(id, employee, request.getDepartmentId());
        return ResponseEntity.ok(EmployeeResponse.fromEntity(updated));
    }
}
