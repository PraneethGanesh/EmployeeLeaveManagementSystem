package com.example.EmployeeLeaveManagementSystem.Controller;

import com.example.EmployeeLeaveManagementSystem.Scheduler.LeaveStatusScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for scheduler operations
 */
@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    private final LeaveStatusScheduler leaveStatusScheduler;

    public SchedulerController(LeaveStatusScheduler leaveStatusScheduler) {
        this.leaveStatusScheduler = leaveStatusScheduler;
    }

    /**
     * Manual trigger for the leave status scheduler
     * Useful for testing or immediate updates
     */
    @PostMapping("/run")
    public ResponseEntity<String> runScheduler() {
        leaveStatusScheduler.runSchedulerManually();
        return ResponseEntity.ok("Scheduler executed successfully");
    }
}