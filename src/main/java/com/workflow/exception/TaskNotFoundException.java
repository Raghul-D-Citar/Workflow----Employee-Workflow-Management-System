package com.workflow.exception;

public class TaskNotFoundException extends ResourceNotFoundException {

    public TaskNotFoundException(Long id) {
        super("Task not found: " + id);
    }

    public TaskNotFoundException(String message) {
        super(message);
    }
}
