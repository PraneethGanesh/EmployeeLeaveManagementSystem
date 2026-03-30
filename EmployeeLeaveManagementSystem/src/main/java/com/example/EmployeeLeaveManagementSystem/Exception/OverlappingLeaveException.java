package com.example.EmployeeLeaveManagementSystem.Exception;

public class OverlappingLeaveException extends RuntimeException{
    public OverlappingLeaveException(String message) {
        super(message);
    }
}
