package com.workflow.exception;

public class LeaveRequestNotFoundException extends ResourceNotFoundException {

    public LeaveRequestNotFoundException(Long id) {
        super("Leave request not found: " + id);
    }

    public LeaveRequestNotFoundException(String message) {
        super(message);
    }
}
