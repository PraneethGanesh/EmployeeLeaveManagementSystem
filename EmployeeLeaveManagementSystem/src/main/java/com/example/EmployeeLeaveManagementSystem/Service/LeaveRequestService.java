package com.example.EmployeeLeaveManagementSystem.Service;

import com.example.EmployeeLeaveManagementSystem.DTO.ActionDTO;
import com.example.EmployeeLeaveManagementSystem.DTO.LeaveRequestDTO;
import com.example.EmployeeLeaveManagementSystem.DTO.LeaveResponseDTO;
import com.example.EmployeeLeaveManagementSystem.Entity.Employee;
import com.example.EmployeeLeaveManagementSystem.Entity.LeaveRequest;
import com.example.EmployeeLeaveManagementSystem.Enum.LeaveStatus;
import com.example.EmployeeLeaveManagementSystem.Exception.*;
import com.example.EmployeeLeaveManagementSystem.Repository.EmployeeRepo;
import com.example.EmployeeLeaveManagementSystem.Repository.LeaveRequestRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveRequestService {
    private final LeaveRequestRepo leaveRequestRepo;
    private final EmployeeRepo employeeRepo;

    public LeaveRequestService(LeaveRequestRepo leaveRequestRepo, EmployeeRepo employeeRepo) {
        this.leaveRequestRepo = leaveRequestRepo;
        this.employeeRepo = employeeRepo;
    }


    public ResponseEntity<?> createRequest(long id,LeaveRequestDTO requestDTO){
        LeaveRequest leaveRequest=new LeaveRequest();

        Employee employee=employeeRepo.findById(id).orElseThrow(()->
                new EmployeeNotFound("Employee with id: "+id+" is not found")
        );
        leaveRequest.setEmployee(employee);
        //Checking if the start date is before today
        if(requestDTO.getStartDate().isBefore(LocalDate.now())){
               throw new InvalidStartDateException("Start date cannot before the current date");
        }
        //Checking if the end date is before start date
        if(requestDTO.getEndDate().isBefore(requestDTO.getStartDate())){
            throw new InvalidEndDateException("End date should be equal to or greater than end date");
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
        //checking if there is any overlapping approved leave request of that employee
        long count= leaveRequestRepo.countOverlappingApprovedLeave(
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
    public List<LeaveResponseDTO> getAllThePendingLeaveRequests(){
        List<LeaveRequest> requestList=leaveRequestRepo.findByStatus(LeaveStatus.PENDING);
        List<LeaveResponseDTO> responseDTOS=new ArrayList<>();
        for(LeaveRequest request:requestList){
            responseDTOS.add(convertToDTO(request));
        }
        return responseDTOS;
    }

    public ResponseEntity<?> updateLeaveRequestStatus(ActionDTO actionDTO){
        String email=actionDTO.getManagerEmail();
        Optional<Employee> employee=employeeRepo.findByEmail(email);
        if(employee.isPresent()){
           return ResponseEntity.badRequest().body("Employee cannot update leave request status");
        }
        LeaveRequest leaveRequest=leaveRequestRepo.findById(actionDTO.getLeaveRequestId()).orElseThrow(
                ()-> new LeaveRequestNotFoundException("LeaveRequest with id:"+actionDTO.getLeaveRequestId()+" not found")
        );
         if(actionDTO.getManagerEmail()==null){
             return ResponseEntity.badRequest().body("Manager Email is required..");
         }
         leaveRequest.setManager(actionDTO.getManagerEmail());
         if(actionDTO.getAction().equalsIgnoreCase("approved")){
             leaveRequest.setStatus(LeaveStatus.APPROVED);
         }
         if(actionDTO.getAction().equalsIgnoreCase("rejected")){
             leaveRequest.setStatus(LeaveStatus.REJECTED);
         }

         if(actionDTO.getRemarks()!=null){
             leaveRequest.setRemarks(actionDTO.getRemarks());
         }
         return ResponseEntity.ok(leaveRequestRepo.save(leaveRequest));
    }

    public ResponseEntity<?> cancelLeaveRequest(String email,long leaveId){
        Employee employee=employeeRepo.findByEmail(email).orElseThrow(
                ()->new EmployeeNotFound("Employee with email:"+email+" not found")
        );
        LeaveRequest leaveRequest=leaveRequestRepo.findById(leaveId).orElseThrow(
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
        LeaveResponseDTO responseDTO=new LeaveResponseDTO();
        responseDTO.setLeaveId(request.getId());
        responseDTO.setEmployeeId(request.getEmployee().getEmployeeId());
        responseDTO.setLeaveType(request.getLeaveType());
        responseDTO.setStartDate(request.getStartDate());
        responseDTO.setEndDate(request.getEndDate());
        responseDTO.setReason(request.getReason());
        responseDTO.setStatus(request.getStatus());
        return responseDTO;
    }
}
