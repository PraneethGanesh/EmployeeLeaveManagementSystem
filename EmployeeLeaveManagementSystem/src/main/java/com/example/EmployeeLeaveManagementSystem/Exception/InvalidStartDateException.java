package com.example.EmployeeLeaveManagementSystem.Exception;

public class InvalidStartDateException extends RuntimeException{
    public InvalidStartDateException(String message) {
        super(message);
    }
}
