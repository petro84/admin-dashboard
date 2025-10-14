package com.petro.admin_dashboard.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
