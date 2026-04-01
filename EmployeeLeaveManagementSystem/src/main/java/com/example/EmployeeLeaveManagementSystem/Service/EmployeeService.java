package com.example.EmployeeLeaveManagementSystem.Service;

import com.example.EmployeeLeaveManagementSystem.Controller.EmployeeController;
import com.example.EmployeeLeaveManagementSystem.DTO.EmployeeDTO;
import com.example.EmployeeLeaveManagementSystem.Entity.Employee;
import com.example.EmployeeLeaveManagementSystem.Enum.Role;
import com.example.EmployeeLeaveManagementSystem.Enum.Status;
import com.example.EmployeeLeaveManagementSystem.Exception.EmployeeNotFound;
import com.example.EmployeeLeaveManagementSystem.Repository.EmployeeRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;

@Service
public class EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepo employeeRepo;

    public EmployeeService(EmployeeRepo employeeRepo) {
        this.employeeRepo = employeeRepo;
    }

    public List<Employee> getAllEmployees() {
        return employeeRepo.findAll();
    }

    public Employee createEmployee(EmployeeDTO employeeDTO) {
        log.info("Creating new employee with email: {}", employeeDTO.getEmail());
        var employee = new Employee();
        if (employeeDTO.getName() != null)  employee.setName(employeeDTO.getName());
        if (employeeDTO.getEmail() != null) employee.setEmail(employeeDTO.getEmail());
        if (employeeDTO.getDept() != null)  employee.setDept(employeeDTO.getDept());
        if (employeeDTO.getTimezone() != null && isValidTimezone(employeeDTO.getTimezone())) {
            employee.setTimezone(employeeDTO.getTimezone());
        } else {
            employee.setTimezone("UTC");
        }

        return employeeRepo.save(employee);
    }

    public Employee updateEmployee(long id, EmployeeDTO employee) {
        var updateEmployee = employeeRepo.findById(id).orElseThrow(
                () -> new EmployeeNotFound("Employee with id:" + id + " not found")
        );

        if (employee.getName() != null)     updateEmployee.setName(employee.getName());
        if (employee.getEmail() != null)    updateEmployee.setEmail(employee.getEmail());
        if (employee.getDept() != null)     updateEmployee.setDept(employee.getDept());
        if (employee.getTimezone() != null && isValidTimezone(employee.getTimezone())) {
            updateEmployee.setTimezone(employee.getTimezone());
        }
        return employeeRepo.save(updateEmployee);
    }

    public void deleteEmployee(long id) {
        var employee = employeeRepo.findById(id).orElseThrow(
                () -> new EmployeeNotFound("Employee with id:" + id + " not found")
        );
        employeeRepo.delete(employee);
    }

    public ResponseEntity<String> inactivateUser(long ManagerId, long employeeId) {
        var manager = employeeRepo.findById(ManagerId).orElseThrow(
                () -> new EmployeeNotFound("Manager with id:" + ManagerId + " not found")
        );
        var employee = employeeRepo.findById(employeeId).orElseThrow(
                () -> new EmployeeNotFound("Employee with id:" + employeeId + " not found")
        );
        if (manager.getRole() == Role.EMPLOYEE) {
            return ResponseEntity.badRequest().body("Only manager can inactivate the user");
        }
        if (employee.getStatus() == Status.INACTIVE) {
            return ResponseEntity.badRequest()
                    .body("Status of the employee with id: " + employeeId + " is already set to inactive");
        }
        employee.setStatus(Status.INACTIVE);
        employeeRepo.save(employee);
        return ResponseEntity.ok("Status of the employee with id: " + employeeId + " is set to inactive");
    }

    /** Validate that the given string is a recognized IANA timezone id. */
    private boolean isValidTimezone(String tz) {
        try {
            ZoneId.of(tz);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}