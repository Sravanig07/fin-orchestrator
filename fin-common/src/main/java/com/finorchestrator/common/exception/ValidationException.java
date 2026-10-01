package com.finorchestrator.common.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_INPUT_DATA");
    }
}
