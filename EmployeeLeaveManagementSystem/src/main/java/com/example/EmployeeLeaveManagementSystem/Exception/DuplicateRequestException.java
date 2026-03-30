package com.example.EmployeeLeaveManagementSystem.Exception;

public class DuplicateRequestException extends RuntimeException {
    public DuplicateRequestException(String message) {
        super(message);
    }
}
