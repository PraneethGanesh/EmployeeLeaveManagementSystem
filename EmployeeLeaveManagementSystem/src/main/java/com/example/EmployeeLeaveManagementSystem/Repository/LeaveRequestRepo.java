package com.example.EmployeeLeaveManagementSystem.Repository;

import com.example.EmployeeLeaveManagementSystem.Entity.LeaveRequest;
import com.example.EmployeeLeaveManagementSystem.Enum.LeaveStatus;
import com.example.EmployeeLeaveManagementSystem.Enum.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepo extends JpaRepository<LeaveRequest,Long> {

    @Query(value = "select COUNT(*) " +
            "from leave_request l " +
            "where l.employee_id=:employeeId " +
            "AND l.status='APPROVED' " +
            "AND l.start_date <= :endDate " +
            "AND l.end_date >= :startDate",nativeQuery = true)
    long countOverlappingApprovedLeave(
            @Param("employeeId") long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "select count(*) " +
            "from leave_request l " +
            "where l.employee_id =:employeeId " +
            "AND l.start_date =:startDate " +
            "AND l.end_date = :endDate " +
            "AND l.status= :status",nativeQuery = true)
    long checkDuplicate(@Param(value = "employeeId") long employeeId,
                           @Param(value = "startDate") LocalDate startDate,
                           @Param(value = "endDate") LocalDate endDate,
                        @Param(value = "status") String status);

    List<LeaveRequest> findByStatus(LeaveStatus status);
}
