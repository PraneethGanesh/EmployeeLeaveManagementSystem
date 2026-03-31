package com.example.EmployeeLeaveManagementSystem.Controller;

import com.example.EmployeeLeaveManagementSystem.DTO.EmployeeDTO;
import com.example.EmployeeLeaveManagementSystem.Entity.Employee;
import com.example.EmployeeLeaveManagementSystem.Repository.EmployeeRepo;
import com.example.EmployeeLeaveManagementSystem.Service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<?> getAllEmployees(){
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @PostMapping
    public ResponseEntity<Employee> addEmployee(@RequestBody EmployeeDTO employee){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(employee));

    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable long id,@RequestBody EmployeeDTO employeeDTO){
        return ResponseEntity.ok(employeeService.updateEmployee(id,employeeDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable long id){
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee with id: "+id+" is deleted");
    }

    //should be done by manager
    @PutMapping("/{ManagerId}/inactive")
    public ResponseEntity<String> inactivateEmployee(@PathVariable long ManagerId,@RequestParam long employeeId){
        return employeeService.inactivateUser(ManagerId,employeeId);
    }
}
