package com.workflow.exception;

public class EmployeeNotFoundException extends ResourceNotFoundException {

    public EmployeeNotFoundException(Long id) {
        super("Employee not found: " + id);
    }

    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
