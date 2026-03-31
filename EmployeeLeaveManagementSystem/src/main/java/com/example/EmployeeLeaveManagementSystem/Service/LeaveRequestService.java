package com.example.EmployeeLeaveManagementSystem.Service;

import com.example.EmployeeLeaveManagementSystem.DTO.ActionDTO;
import com.example.EmployeeLeaveManagementSystem.DTO.LeaveRequestDTO;
import com.example.EmployeeLeaveManagementSystem.DTO.LeaveResponseDTO;
import com.example.EmployeeLeaveManagementSystem.Entity.Employee;
import com.example.EmployeeLeaveManagementSystem.Entity.LeaveRequest;
import com.example.EmployeeLeaveManagementSystem.Enum.LeaveStatus;
import com.example.EmployeeLeaveManagementSystem.Enum.LeaveType;
import com.example.EmployeeLeaveManagementSystem.Enum.Role;
import com.example.EmployeeLeaveManagementSystem.Enum.Status;
import com.example.EmployeeLeaveManagementSystem.Exception.*;
import com.example.EmployeeLeaveManagementSystem.Repository.EmployeeRepo;
import com.example.EmployeeLeaveManagementSystem.Repository.LeaveRequestRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LeaveRequestService {
    private final LeaveRequestRepo leaveRequestRepo;
    private final EmployeeRepo employeeRepo;
    private static final Logger log = LoggerFactory.getLogger(LeaveRequestService.class);
    public LeaveRequestService(LeaveRequestRepo leaveRequestRepo, EmployeeRepo employeeRepo) {
        this.leaveRequestRepo = leaveRequestRepo;
        this.employeeRepo = employeeRepo;
    }

    public List<LeaveResponseDTO> getAllTheLeaveRequest(){
        List<LeaveRequest> requestList=leaveRequestRepo.findAll();
        List<LeaveResponseDTO> dtos=new ArrayList<>();
        for(LeaveRequest request:requestList){
           dtos.add(convertToDTO(request));
        }
        return dtos;
    }


    public ResponseEntity<?> createRequest(long id,LeaveRequestDTO requestDTO){
        var leaveRequest=new LeaveRequest();

        //Validate employee exists and is active
        var employee=employeeRepo.findById(id).orElseThrow(()->
                new EmployeeNotFound("Employee with id: "+id+" is not found")
        );
        if (employee.getStatus() != Status.ACTIVE) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only active employees can apply for leave"));
        }

        leaveRequest.setEmployee(employee);
        //Checking if the start date is before today
        LocalDate today = LocalDate.now();
        if (requestDTO.getStartDate().isBefore(today)) {
            throw new InvalidStartDateException("Start date cannot be before current date");
        }
        //Checking if the end date is before start date
        if (requestDTO.getEndDate().isBefore(requestDTO.getStartDate())) {
            throw new InvalidEndDateException("End date must be equal to or greater than start date");
        }
        //optional
        long daysRequested = ChronoUnit.DAYS.between(requestDTO.getStartDate(), requestDTO.getEndDate()) + 1;
        if (daysRequested > 30) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Leave cannot exceed 30 consecutive days"));
        }
        //optional
        if (requestDTO.getLeaveType() == LeaveType.SICK) {
            long sickDaysUsed = leaveRequestRepo.countDaysByEmployeeAndLeaveTypeAndYear(
                    employee, LeaveType.SICK, today.getYear());

            if (sickDaysUsed + daysRequested > 12) { // Assuming 12 sick days per year
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Sick leave limit exceeded for this year"));
            }
        }
        long duplicateCount= leaveRequestRepo.checkDuplicate(
                id,
                requestDTO.getStartDate(),
                requestDTO.getEndDate(),
                LeaveStatus.PENDING.name()
        );
        if(duplicateCount>0){
            throw new DuplicateRequestException("Duplicate leave request");
        }

        //checking if there is any overlapping leave request of that employee
        long count= leaveRequestRepo.countOverlappingLeave(
                id,
                requestDTO.getStartDate(),
                requestDTO.getEndDate()
        );
         if(count>0){
             throw new OverlappingLeaveException("Overlapping leave exists");
         }
         leaveRequest.setStartDate(requestDTO.getStartDate());
         leaveRequest.setEndDate(requestDTO.getEndDate());

         if(requestDTO.getLeaveType()==null){
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                     .body("Leave type is required..");
         }
         leaveRequest.setLeaveType(requestDTO.getLeaveType());
         if(requestDTO.getReason()!=null){
             leaveRequest.setReason(requestDTO.getReason());
         }
         LeaveRequest savedRequest=leaveRequestRepo.save(leaveRequest);
         return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedRequest));
    }

    //Only for manager
    public List<LeaveResponseDTO> getAllThePendingLeaveRequests(long ManagerId){
        var manager=employeeRepo.findById(ManagerId).orElseThrow(
                ()->new EmployeeNotFound("Manager with id: "+ManagerId+" is not found")
        );
        if(manager.getRole()== Role.EMPLOYEE){
            throw new InvalidManagerException("Only manager can see the pending leave requests");
        }
        List<LeaveRequest> requestList=leaveRequestRepo.findByStatus(LeaveStatus.PENDING);
        List<LeaveResponseDTO> responseDTOS=new ArrayList<>();
        for(LeaveRequest request:requestList){
            responseDTOS.add(convertToDTO(request));
        }
        return responseDTOS;
    }

    public ResponseEntity<?> updateLeaveRequestStatus(ActionDTO actionDTO){

        if (actionDTO.getAction() == null) {
            return ResponseEntity.badRequest().body("Action is required");
        }
        long managerId=actionDTO.getManagerId();
        log.info("Updating leave request status for id: {}", actionDTO.getLeaveRequestId());
        Employee manager=employeeRepo.findById(managerId).orElseThrow(
                ()->new EmployeeNotFound("Manager with id: "+managerId+" is not found")
        );
        if(manager.getRole()== Role.EMPLOYEE){
            throw new InvalidManagerException("Only manager can update leave requests");
        }

        var leaveRequest=leaveRequestRepo.findById(actionDTO.getLeaveRequestId()).orElseThrow(
                ()-> new LeaveRequestNotFoundException("LeaveRequest with id:"+actionDTO.getLeaveRequestId()+" not found")
        );

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            return ResponseEntity.badRequest()
                    .body("Only PENDING requests can be approved or rejected. Current status: "
                            + leaveRequest.getStatus());
        }

         leaveRequest.setManager(manager.getName());
         if(actionDTO.getAction().equalsIgnoreCase("approved")){
             leaveRequest.setStatus(LeaveStatus.APPROVED);
         }
         else if(actionDTO.getAction().equalsIgnoreCase("rejected")){
             leaveRequest.setStatus(LeaveStatus.REJECTED);
         }
         else{
             return ResponseEntity.badRequest().body("Invalid action,Use APPROVED or REJECTED");
         }

         if(actionDTO.getRemarks()!=null){
             leaveRequest.setRemarks(actionDTO.getRemarks());
         }
         return ResponseEntity.ok(leaveRequestRepo.save(leaveRequest));
    }

    public ResponseEntity<?> cancelLeaveRequest(String email,long leaveId){
        var employee=employeeRepo.findByEmail(email).orElseThrow(
                ()->new EmployeeNotFound("Employee with email:"+email+" not found")
        );
        var leaveRequest=leaveRequestRepo.findById(leaveId).orElseThrow(
                ()->new LeaveRequestNotFoundException("Leave request with id:"+leaveId+" is not found")
        );
        if(employee.getEmployeeId()!=leaveRequest.getEmployee().getEmployeeId()){
            return ResponseEntity.badRequest().body("You cannot cancel others Leave request..");
        }
        if(leaveRequest.getStartDate().isEqual(LocalDate.now())||leaveRequest.getStartDate().isBefore(LocalDate.now())){
            return ResponseEntity.badRequest().body("You cannot cancel leave request after the leave has started");
        }
        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        leaveRequestRepo.save(leaveRequest);
        return ResponseEntity.ok("Leave Request with id:"+ leaveId+" Successfully cancelled");
    }

    private LeaveResponseDTO convertToDTO(LeaveRequest request){
        var responseDTO=new LeaveResponseDTO();
        responseDTO.setLeaveRequestId(request.getId());
        responseDTO.setEmployeeId(request.getEmployee().getEmployeeId());
        responseDTO.setLeaveType(request.getLeaveType());
        responseDTO.setStartDate(request.getStartDate());
        responseDTO.setEndDate(request.getEndDate());
        responseDTO.setReason(request.getReason());
        responseDTO.setStatus(request.getStatus());
        return responseDTO;
    }

    public ResponseEntity<?> getLeaveRequestsByEmployee(long employeeId) {
        log.info("Fetching leave requests for employee: {}", employeeId);

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFound("Employee not found: " + employeeId));

        List<LeaveRequest> requests = leaveRequestRepo.findByEmployeeOrderByStartDateDesc(employee);
        List<LeaveResponseDTO> response = requests.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "employeeId", employeeId,
                "employeeName", employee.getName(),
                "totalRequests", response.size(),
                "requests", response
        ));
    }
}
