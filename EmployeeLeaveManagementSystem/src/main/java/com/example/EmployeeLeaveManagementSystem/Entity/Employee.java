package com.example.EmployeeLeaveManagementSystem.Entity;

import com.example.EmployeeLeaveManagementSystem.Enum.Status;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long employeeId;
    private String name;
    @Column(unique = true)
    private String email;
    private String dept;
    @Enumerated(EnumType.STRING)
    private Status status;
    private LocalDate joined_at;

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getJoined_at() {
        return joined_at;
    }

    public void setJoined_at(LocalDate joined_at) {
        this.joined_at = joined_at;
    }

    @PrePersist
    public void initialSetup(){
        this.status=Status.ACTIVE;
        this.joined_at=LocalDate.now();
    }

}
