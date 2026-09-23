package com.workflow.service;

import com.workflow.entity.Employee;

import java.util.List;

public interface EmployeeService {

    Employee createEmployee(Employee employee, Long departmentId);

    Employee getEmployee(Long employeeId);

    List<Employee> getEmployees();

    Employee updateEmployee(Long employeeId, Employee employee, Long departmentId);
}