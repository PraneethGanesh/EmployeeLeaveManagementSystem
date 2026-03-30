package com.example.EmployeeLeaveManagementSystem.Service;

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
