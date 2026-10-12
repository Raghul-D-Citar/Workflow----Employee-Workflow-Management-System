package com.workflow.exception;

public class InsufficientLeaveException extends InsufficientLeaveBalanceException {

    public InsufficientLeaveException(String message) {
        super(message);
    }
}
