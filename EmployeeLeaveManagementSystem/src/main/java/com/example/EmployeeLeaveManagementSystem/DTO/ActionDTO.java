package com.example.EmployeeLeaveManagementSystem.DTO;


public class ActionDTO {
    private long leaveRequestId;
    private String managerEmail;
    private String Action;
    private String remarks;

    public long getLeaveRequestId() {
        return leaveRequestId;
    }

    public void setLeaveRequestId(long leaveRequestId) {
        this.leaveRequestId = leaveRequestId;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }

    public String getAction() {
        return Action;
    }

    public void setAction(String action) {
        Action = action;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
