package com.workflow.controller;

import com.workflow.dto.leave.LeaveCreateRequest;
import com.workflow.dto.leave.LeaveDecisionRequest;
import com.workflow.dto.leave.LeaveResponse;
import com.workflow.entity.LeaveRequest;
import com.workflow.exception.InvalidRequestException;
import com.workflow.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    public ResponseEntity<LeaveResponse> createLeaveRequest(@Valid @RequestBody LeaveCreateRequest request) {
        LeaveRequest created = leaveService.createLeaveRequest(
                request.getEmployeeId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getReason()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(LeaveResponse.fromEntity(created));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeaveResponse>> getMyLeaveRequests(
            @RequestParam(name = "employeeId", required = false) Long employeeId,
            @RequestHeader(name = "X-Employee-Id", required = false) Long headerEmpId) {
        Long targetEmployeeId = employeeId != null ? employeeId : headerEmpId;
        if (targetEmployeeId == null) {
            throw new InvalidRequestException("Employee ID must be provided via query param or header");
        }

        List<LeaveResponse> leaves = leaveService.getOwnLeaveRequests(targetEmployeeId)
                .stream()
                .map(LeaveResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LeaveResponse>> getPendingLeaveRequests() {
        List<LeaveResponse> pendingLeaves = leaveService.getPendingLeaveRequests()
                .stream()
                .map(LeaveResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(pendingLeaves);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveResponse> getLeaveRequestById(@PathVariable Long id) {
        LeaveRequest leaveRequest = leaveService.getLeaveRequest(id);
        return ResponseEntity.ok(LeaveResponse.fromEntity(leaveRequest));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<LeaveResponse> approveLeave(@PathVariable Long id,
                                                      @Valid @RequestBody LeaveDecisionRequest request) {
        LeaveRequest approved = leaveService.approveLeave(id, request.getReviewerId(), request.getReviewComment());
        return ResponseEntity.ok(LeaveResponse.fromEntity(approved));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<LeaveResponse> rejectLeave(@PathVariable Long id,
                                                     @Valid @RequestBody LeaveDecisionRequest request) {
        LeaveRequest rejected = leaveService.rejectLeave(id, request.getReviewerId(), request.getReviewComment());
        return ResponseEntity.ok(LeaveResponse.fromEntity(rejected));
    }
}
