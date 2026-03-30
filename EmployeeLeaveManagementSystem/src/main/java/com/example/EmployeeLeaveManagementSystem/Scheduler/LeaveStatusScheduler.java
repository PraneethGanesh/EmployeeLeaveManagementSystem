package com.example.EmployeeLeaveManagementSystem.Scheduler;

import com.example.EmployeeLeaveManagementSystem.Entity.Employee;
import com.example.EmployeeLeaveManagementSystem.Entity.LeaveRequest;
import com.example.EmployeeLeaveManagementSystem.Enum.LeaveStatus;
import com.example.EmployeeLeaveManagementSystem.Enum.Status;
import com.example.EmployeeLeaveManagementSystem.Repository.EmployeeRepo;
import com.example.EmployeeLeaveManagementSystem.Repository.LeaveRequestRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Daily Scheduler for Employee Leave Status Management
 *
 * This scheduler runs daily at midnight to:
 * 1. Mark employees ON_LEAVE for approved leaves starting today
 * 2. Mark employees back to ACTIVE for approved leaves that have ended
 */
@Component
public class LeaveStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(LeaveStatusScheduler.class);

    private final LeaveRequestRepo leaveRequestRepo;
    private final EmployeeRepo employeeRepo;

    public LeaveStatusScheduler(LeaveRequestRepo leaveRequestRepo, EmployeeRepo employeeRepo) {
        this.leaveRequestRepo = leaveRequestRepo;
        this.employeeRepo = employeeRepo;
    }

    /**
     * Runs daily at midnight (00:00:00)
     * Cron expression: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 0 * * *",  zone = "UTC")
    @Transactional
    public void updateEmployeeLeaveStatus() {
        log.info("=== Starting Daily Leave Status Scheduler ===");
        LocalDate today = LocalDate.now();

        int onLeaveCount = markEmployeesOnLeave(today);
        int activeCount = markEmployeesBackToActive(today);

        log.info("=== Scheduler Completed ===");
        log.info("Employees marked ON_LEAVE: {}", onLeaveCount);
        log.info("Employees marked ACTIVE: {}", activeCount);
    }

    /**
     * Step 1: Mark employees ON_LEAVE for approved leaves starting today
     */
    private int markEmployeesOnLeave(LocalDate today) {
        log.info("Step 1: Checking for approved leaves starting on {}", today);

        List<LeaveRequest> leavesStartingToday = leaveRequestRepo.findByStartDateAndStatus(today, LeaveStatus.APPROVED);

        int count = 0;
        for (LeaveRequest leave : leavesStartingToday) {
            Employee employee = leave.getEmployee();

            // Only update if employee is currently ACTIVE
            if (employee.getStatus() == Status.ACTIVE) {
                employee.setStatus(Status.ON_LEAVE);
                employeeRepo.save(employee);
                count++;
                log.info("Employee {} ({}) marked ON_LEAVE - Leave ID: {}",
                        employee.getEmployeeId(), employee.getName(), leave.getId());
            }
        }

        log.info("Marked {} employees as ON_LEAVE", count);
        return count;
    }

    /**
     * Step 2: Mark employees back to ACTIVE for approved leaves that have ended
     */
    private int markEmployeesBackToActive(LocalDate today) {
        log.info("Step 2: Checking for approved leaves that ended before {}", today);

        // Find all approved leaves where endDate < today
        List<LeaveRequest> endedLeaves =
                leaveRequestRepo.findApprovedLeavesEndedBefore(today, LeaveStatus.APPROVED);

        int count = 0;
        for (LeaveRequest leave : endedLeaves) {
            Employee employee = leave.getEmployee();

            // Only update if employee is currently ON_LEAVE
            if (employee.getStatus() == Status.ON_LEAVE) {
                employee.setStatus(Status.ACTIVE);
                employeeRepo.save(employee);
                count++;
                log.info("Employee {} ({}) marked ACTIVE - Leave ID: {} ended on {}",
                        employee.getEmployeeId(), employee.getName(), leave.getId(), leave.getEndDate());
            }
        }

        log.info("Marked {} employees as ACTIVE", count);
        return count;
    }

    /**
     * Manual trigger for testing purposes
     * Can be called via an API endpoint if needed
     */
    public void runSchedulerManually() {
        log.info("Manual scheduler trigger initiated");
        updateEmployeeLeaveStatus();
    }
}