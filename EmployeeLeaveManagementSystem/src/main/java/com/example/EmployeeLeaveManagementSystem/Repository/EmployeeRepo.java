package com.example.EmployeeLeaveManagementSystem.Repository;

import com.example.EmployeeLeaveManagementSystem.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee,Long> {
}
