package com.example.EmployeeLeaveManagementSystem.Controller;

import com.example.EmployeeLeaveManagementSystem.DTO.ActionDTO;
import com.example.EmployeeLeaveManagementSystem.DTO.LeaveRequestDTO;
import com.example.EmployeeLeaveManagementSystem.DTO.LeaveResponseDTO;
import com.example.EmployeeLeaveManagementSystem.Entity.LeaveRequest;
import com.example.EmployeeLeaveManagementSystem.Service.LeaveRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("leave_requests")
public class LeaveRequestController {
    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> addLeaveRequest(@PathVariable long id,@RequestBody LeaveRequestDTO dto){
        return leaveRequestService.createRequest(id,dto);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveResponseDTO>> getAllTheLeaveRequests(){
        return ResponseEntity.ok(leaveRequestService.getAllThePendingLeaveRequests());
    }

    @PutMapping("/approval")
    public ResponseEntity<?> updateLeaveRequestStatus(@RequestBody ActionDTO actionDTO){
      return leaveRequestService.updateLeaveRequestStatus(actionDTO);
    }

    @PutMapping("/cancel/{leaveId}")
    public ResponseEntity<?> cancelLeaveRequest(@RequestParam String email,@PathVariable long leaveId){
      return leaveRequestService.cancelLeaveRequest(email, leaveId);
    }

}
