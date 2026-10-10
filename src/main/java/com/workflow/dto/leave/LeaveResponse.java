package com.workflow.dto.leave;

import com.workflow.entity.LeaveRequest;
import com.workflow.entity.enums.LeaveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LeaveResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
    private Long reviewerId;
    private String reviewerName;
    private String reviewComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LeaveResponse() {}

    public static LeaveResponse fromEntity(LeaveRequest leaveRequest) {
        if (leaveRequest == null) {
            return null;
        }

        LeaveResponse response = new LeaveResponse();
        response.setId(leaveRequest.getId());
        if (leaveRequest.getEmployee() != null) {
            response.setEmployeeId(leaveRequest.getEmployee().getId());
            response.setEmployeeName(leaveRequest.getEmployee().getFirstName() + " " + leaveRequest.getEmployee().getLastName());
        }
        response.setStartDate(leaveRequest.getStartDate());
        response.setEndDate(leaveRequest.getEndDate());
        response.setReason(leaveRequest.getReason());
        response.setStatus(leaveRequest.getStatus());
        if (leaveRequest.getReviewer() != null) {
            response.setReviewerId(leaveRequest.getReviewer().getId());
            response.setReviewerName(leaveRequest.getReviewer().getFirstName() + " " + leaveRequest.getReviewer().getLastName());
        }
        response.setReviewComment(leaveRequest.getReviewComment());
        response.setCreatedAt(leaveRequest.getCreatedAt());
        response.setUpdatedAt(leaveRequest.getUpdatedAt());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LeaveStatus getStatus() { return status; }
    public void setStatus(LeaveStatus status) { this.status = status; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
