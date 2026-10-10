package com.workflow.dto.employee;

import com.workflow.entity.Employee;
import com.workflow.entity.enums.Role;

import java.time.LocalDateTime;

public class EmployeeResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private DepartmentResponse department;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmployeeResponse() {}

    public static EmployeeResponse fromEntity(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setUsername(employee.getUsername());
        response.setEmail(employee.getEmail());
        response.setFirstName(employee.getFirstName());
        response.setLastName(employee.getLastName());
        response.setRole(employee.getRole());
        response.setDepartment(DepartmentResponse.fromEntity(employee.getDepartment()));
        response.setCreatedAt(employee.getCreatedAt());
        response.setUpdatedAt(employee.getUpdatedAt());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public DepartmentResponse getDepartment() { return department; }
    public void setDepartment(DepartmentResponse department) { this.department = department; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
