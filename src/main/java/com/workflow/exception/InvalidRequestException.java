package com.workflow.exception;

public class InvalidRequestException extends BusinessException {

    public InvalidRequestException(String message) {
        super(message);
    }
}