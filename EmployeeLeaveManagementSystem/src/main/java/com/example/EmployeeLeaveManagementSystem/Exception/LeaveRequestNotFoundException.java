package com.example.EmployeeLeaveManagementSystem.Exception;

public class LeaveRequestNotFoundException extends RuntimeException{
    public LeaveRequestNotFoundException(String message) {
        super(message);
    }
}
