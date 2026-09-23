package com.workflow.service;

import com.workflow.entity.LeaveRequest;

import java.time.LocalDate;
import java.util.List;

public interface LeaveService {

    LeaveRequest createLeaveRequest(Long employeeId, LocalDate startDate, LocalDate endDate, String reason);

    List<LeaveRequest> getOwnLeaveRequests(Long employeeId);

    List<LeaveRequest> getPendingLeaveRequests();

    List<LeaveRequest> getTeamLeaveRequests(Long departmentId);

    LeaveRequest approveLeave(Long leaveRequestId, Long reviewerId, String reviewComment);

    LeaveRequest rejectLeave(Long leaveRequestId, Long reviewerId, String reviewComment);
}