package com.workflow.service.impl;

import com.workflow.entity.Department;
import com.workflow.entity.Employee;
import com.workflow.exception.DuplicateResourceException;
import com.workflow.exception.InvalidRequestException;
import com.workflow.exception.ResourceNotFoundException;
import com.workflow.repository.DepartmentRepository;
import com.workflow.repository.EmployeeRepository;
import com.workflow.service.EmployeeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public Employee createEmployee(Employee employee, Long departmentId) {
        if (employee == null) {
            throw new InvalidRequestException("Employee payload is required");
        }

        ensureEmployeeIdentityIsUnique(employee);
        assignDepartment(employee, departmentId);

        return employeeRepository.save(employee);
    }

    @Override
    public Employee getEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
    }

    @Override
    public List<Employee> getEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee updateEmployee(Long employeeId, Employee employee, Long departmentId) {
        if (employee == null) {
            throw new InvalidRequestException("Employee payload is required");
        }

        Employee existingEmployee = getEmployee(employeeId);

        if (employee.getUsername() != null && !employee.getUsername().equals(existingEmployee.getUsername())) {
            employeeRepository.findByUsername(employee.getUsername())
                    .filter(found -> !found.getId().equals(employeeId))
                    .ifPresent(found -> {
                        throw new DuplicateResourceException("Username already exists: " + employee.getUsername());
                    });
            existingEmployee.setUsername(employee.getUsername());
        }

        if (employee.getEmail() != null && !employee.getEmail().equals(existingEmployee.getEmail())) {
            employeeRepository.findByEmail(employee.getEmail())
                    .filter(found -> !found.getId().equals(employeeId))
                    .ifPresent(found -> {
                        throw new DuplicateResourceException("Email already exists: " + employee.getEmail());
                    });
            existingEmployee.setEmail(employee.getEmail());
        }

        if (employee.getFirstName() != null) {
            existingEmployee.setFirstName(employee.getFirstName());
        }

        if (employee.getLastName() != null) {
            existingEmployee.setLastName(employee.getLastName());
        }

        assignDepartment(existingEmployee, departmentId);

        return employeeRepository.save(existingEmployee);
    }

    private void ensureEmployeeIdentityIsUnique(Employee employee) {
        employeeRepository.findByUsername(employee.getUsername())
                .ifPresent(found -> {
                    throw new DuplicateResourceException("Username already exists: " + employee.getUsername());
                });

        employeeRepository.findByEmail(employee.getEmail())
                .ifPresent(found -> {
                    throw new DuplicateResourceException("Email already exists: " + employee.getEmail());
                });
    }

    private void assignDepartment(Employee employee, Long departmentId) {
        if (departmentId == null) {
            return;
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + departmentId));
        employee.setDepartment(department);
    }
}