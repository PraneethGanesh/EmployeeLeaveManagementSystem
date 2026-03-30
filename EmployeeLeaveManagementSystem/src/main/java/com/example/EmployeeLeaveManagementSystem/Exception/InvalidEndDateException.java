package com.example.EmployeeLeaveManagementSystem.Exception;

public class InvalidEndDateException extends RuntimeException{
    public InvalidEndDateException(String message) {
        super(message);
    }
}
