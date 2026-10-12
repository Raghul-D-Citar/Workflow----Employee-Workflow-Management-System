package com.workflow.exception;

public class InvalidLeaveTransitionException extends InvalidStateTransitionException {

    public InvalidLeaveTransitionException(String message) {
        super(message);
    }
}
