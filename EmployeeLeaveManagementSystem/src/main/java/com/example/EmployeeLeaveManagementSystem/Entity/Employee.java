package com.example.EmployeeLeaveManagementSystem.Entity;

import com.example.EmployeeLeaveManagementSystem.Enum.Role;
import com.example.EmployeeLeaveManagementSystem.Enum.Status;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long employeeId;
    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String dept;
    @Enumerated(EnumType.STRING)
    private Status status;
    private LocalDate joined_at;
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeaveRequest> leaveRequests = new ArrayList<>();
    /** IANA timezone id, e.g. "Asia/Kolkata", "America/New_York". Defaults to UTC. */
    @Column(nullable = false)
    private String timezone = "UTC";

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

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
    public void initialSetup() {
        this.status = Status.ACTIVE;
        this.joined_at = LocalDate.now();
        this.role = Role.EMPLOYEE;
        if (this.timezone == null || this.timezone.isBlank()) {
            this.timezone = "UTC";
        }
    }
}