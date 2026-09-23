package com.workflow.exception;

public class InsufficientLeaveBalanceException extends BusinessException {

    public InsufficientLeaveBalanceException(String message) {
        super(message);
    }
}