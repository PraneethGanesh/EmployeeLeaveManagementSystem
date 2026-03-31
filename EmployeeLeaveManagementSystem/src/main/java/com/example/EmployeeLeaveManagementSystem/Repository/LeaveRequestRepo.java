package com.example.EmployeeLeaveManagementSystem.Repository;

import com.example.EmployeeLeaveManagementSystem.Entity.Employee;
import com.example.EmployeeLeaveManagementSystem.Entity.LeaveRequest;
import com.example.EmployeeLeaveManagementSystem.Enum.LeaveStatus;
import com.example.EmployeeLeaveManagementSystem.Enum.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepo extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByStatus(LeaveStatus status);

    /**
     * Find leave requests by start date and status
     * Used by scheduler to find leaves starting today
     */
    List<LeaveRequest> findByStartDateAndStatus(LocalDate startDate, LeaveStatus status);

    // Fix the checkDuplicate query (fix parameter names)
    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.employee.employeeId = :employeeId " +
            "AND l.startDate = :startDate AND l.endDate = :endDate AND l.status = :status")
    long checkDuplicate(@Param("employeeId") long employeeId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("status") LeaveStatus status);

    // Fix overlapping leave query (was commented in your code)
    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.employee.employeeId = :employeeId " +
            "AND l.status = 'APPROVED' " +
            "AND ((l.startDate BETWEEN :startDate AND :endDate) " +
            "OR (l.endDate BETWEEN :startDate AND :endDate) " +
            "OR (:startDate BETWEEN l.startDate AND l.endDate))")
    long countOverlappingApprovedLeave(@Param("employeeId") long employeeId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    // Add missing methods
    List<LeaveRequest> findByEmployeeOrderByStartDateDesc(Employee employee);


    List<LeaveRequest> findByEndDateAndStatus(LocalDate endDate, LeaveStatus status);

    // Add for leave duration validation
    @Query("SELECT COALESCE(SUM(DATEDIFF(l.endDate, l.startDate) + 1), 0) " +
            "FROM LeaveRequest l WHERE l.employee = :employee " +
            "AND l.leaveType = :leaveType " +
            "AND YEAR(l.startDate) = :year " +
            "AND l.status IN ('APPROVED', 'PENDING')")
    long countDaysByEmployeeAndLeaveTypeAndYear(@Param("employee") Employee employee,
                                                @Param("leaveType") LeaveType leaveType,
                                                @Param("year") int year);
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = :status AND lr.endDate < :today")
    List<LeaveRequest> findApprovedLeavesEndedBefore(@Param("today") LocalDate today, @Param("status") LeaveStatus status);

}
