package com.workflow.service.impl;

import com.workflow.entity.Employee;
import com.workflow.entity.LeaveBalance;
import com.workflow.entity.LeaveRequest;
import com.workflow.entity.enums.LeaveStatus;
import com.workflow.exception.InsufficientLeaveBalanceException;
import com.workflow.exception.InvalidRequestException;
import com.workflow.exception.InvalidStateTransitionException;
import com.workflow.exception.ResourceNotFoundException;
import com.workflow.repository.EmployeeRepository;
import com.workflow.repository.LeaveBalanceRepository;
import com.workflow.repository.LeaveRequestRepository;
import com.workflow.service.LeaveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveServiceImpl(LeaveRequestRepository leaveRequestRepository,
                            LeaveBalanceRepository leaveBalanceRepository,
                            EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public LeaveRequest createLeaveRequest(Long employeeId, LocalDate startDate, LocalDate endDate, String reason) {
        Employee employee = getEmployee(employeeId);
        validateLeaveWindow(startDate, endDate);

        if (reason == null || reason.isBlank()) {
            throw new InvalidRequestException("Leave reason is required");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(startDate);
        leaveRequest.setEndDate(endDate);
        leaveRequest.setReason(reason.trim());
        leaveRequest.setStatus(LeaveStatus.PENDING);

        return leaveRequestRepository.save(leaveRequest);
    }

    @Override
    public List<LeaveRequest> getOwnLeaveRequests(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING);
    }

    @Override
    public List<LeaveRequest> getTeamLeaveRequests(Long departmentId) {
        return leaveRequestRepository.findByDepartmentId(departmentId);
    }

    @Override
    @Transactional
    public LeaveRequest approveLeave(Long leaveRequestId, Long reviewerId, String reviewComment) {
        return processLeaveDecision(leaveRequestId, reviewerId, reviewComment, LeaveStatus.APPROVED, true);
    }

    @Override
    @Transactional
    public LeaveRequest rejectLeave(Long leaveRequestId, Long reviewerId, String reviewComment) {
        return processLeaveDecision(leaveRequestId, reviewerId, reviewComment, LeaveStatus.REJECTED, false);
    }

    private LeaveRequest processLeaveDecision(Long leaveRequestId,
                                              Long reviewerId,
                                              String reviewComment,
                                              LeaveStatus targetStatus,
                                              boolean deductBalance) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found: " + leaveRequestId));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidStateTransitionException("Only pending leave requests can be processed");
        }

        Employee reviewer = getEmployee(reviewerId);
        leaveRequest.setReviewer(reviewer);
        leaveRequest.setReviewComment(reviewComment);
        leaveRequest.setStatus(targetStatus);

        if (deductBalance) {
            deductLeaveBalance(leaveRequest);
        }

        return leaveRequestRepository.save(leaveRequest);
    }

    private void deductLeaveBalance(LeaveRequest leaveRequest) {
        LeaveBalance leaveBalance = leaveBalanceRepository.findByEmployeeId(leaveRequest.getEmployee().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave balance not found for employee: " + leaveRequest.getEmployee().getId()));

        long requestedDays = ChronoUnit.DAYS.between(leaveRequest.getStartDate(), leaveRequest.getEndDate()) + 1;
        if (requestedDays <= 0) {
            throw new InvalidRequestException("Leave duration must be at least one day");
        }

        if (leaveBalance.getAnnualLeave() < requestedDays) {
            throw new InsufficientLeaveBalanceException("Insufficient annual leave balance");
        }

        leaveBalance.setAnnualLeave((int) (leaveBalance.getAnnualLeave() - requestedDays));
        leaveBalanceRepository.save(leaveBalance);
    }

    private Employee getEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
    }

    private void validateLeaveWindow(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidRequestException("Leave start and end dates are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new InvalidRequestException("Start date cannot be after end date");
        }
    }
}