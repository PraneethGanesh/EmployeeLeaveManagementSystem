package com.example.EmployeeLeaveManagementSystem.Service;

import com.example.EmployeeLeaveManagementSystem.DTO.EmployeeDTO;
import com.example.EmployeeLeaveManagementSystem.Entity.Employee;
import com.example.EmployeeLeaveManagementSystem.Enum.Status;
import com.example.EmployeeLeaveManagementSystem.Exception.EmployeeNotFound;
import com.example.EmployeeLeaveManagementSystem.Repository.EmployeeRepo;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepo employeeRepo;

    public EmployeeService(EmployeeRepo employeeRepo) {
        this.employeeRepo = employeeRepo;
    }

    public List<Employee> getAllEmployees(){
        return employeeRepo.findAll();
    }

    public Employee createEmployee(EmployeeDTO employeeDTO){
        Employee employee=new Employee();
        if(employeeDTO.getName()!=null) employee.setName(employeeDTO.getName());
        if(employeeDTO.getEmail()!=null) employee.setEmail(employeeDTO.getEmail());
        if(employeeDTO.getDept()!=null) employee.setDept(employeeDTO.getDept());
        return employeeRepo.save(employee);
    }

    public Employee updateEmployee(long id, EmployeeDTO employee){
        Employee updateEmployee=employeeRepo.findById(id).orElseThrow(
                ()->new EmployeeNotFound("Employee with id:"+id+" not found")
        );
        if(employee.getName()!=null) updateEmployee.setName(employee.getName());
        if(employee.getEmail()!=null) updateEmployee.setEmail(employee.getEmail());
        if(employee.getDept()!=null) updateEmployee.setDept(employee.getDept());
        return employeeRepo.save(updateEmployee);
    }

    public void deleteEmployee(long id){
        Employee employee=employeeRepo.findById(id).orElseThrow(
                ()->new EmployeeNotFound("Employee with id:"+id+" not found")
        );
        employeeRepo.delete(employee);
    }

    public ResponseEntity<String> inactivateUser(long id){
        Employee employee=employeeRepo.findById(id).orElseThrow(
                ()->new EmployeeNotFound("Employee with id:"+id+" not found")
        );
        if(employee.getStatus()== Status.IN_ACTIVE){
            return ResponseEntity.badRequest()
                    .body("Status of the employee with id: "+id+" is already set to inactive");
        }
        employee.setStatus(Status.IN_ACTIVE);
        employeeRepo.save(employee);
        return ResponseEntity.ok("Status of the employee with id: "+id+" is set to inactive");
    }
}
