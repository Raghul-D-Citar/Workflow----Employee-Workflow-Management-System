package com.workflow.controller;

import com.workflow.dto.leave.LeaveCreateRequest;
import com.workflow.dto.leave.LeaveDecisionRequest;
import com.workflow.dto.leave.LeaveResponse;
import com.workflow.entity.LeaveRequest;
import com.workflow.exception.InvalidRequestException;
import com.workflow.service.LeaveService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@Tag(name = "Leaves", description = "Leave requests, approvals, and rejections")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping
    @Operation(summary = "Create a leave request", description = "Submits a new leave request. Requires JWT authentication.", security = @SecurityRequirement(name = "bearerAuth"))
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
    @Operation(summary = "List employee leave requests", description = "Lists leave requests for the supplied employee ID. Requires JWT authentication.", security = @SecurityRequirement(name = "bearerAuth"))
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
    @Operation(summary = "List pending leave requests", description = "Lists pending requests. Requires MANAGER or ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<LeaveResponse>> getPendingLeaveRequests() {
        List<LeaveResponse> pendingLeaves = leaveService.getPendingLeaveRequests()
                .stream()
                .map(LeaveResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(pendingLeaves);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a leave request", description = "Returns a leave request by ID. Requires JWT authentication.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LeaveResponse> getLeaveRequestById(@PathVariable Long id) {
        LeaveRequest leaveRequest = leaveService.getLeaveRequest(id);
        return ResponseEntity.ok(LeaveResponse.fromEntity(leaveRequest));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a leave request", description = "Approves a pending leave request. Requires MANAGER or ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LeaveResponse> approveLeave(@PathVariable Long id,
                                                      @Valid @RequestBody LeaveDecisionRequest request) {
        LeaveRequest approved = leaveService.approveLeave(id, request.getReviewerId(), request.getReviewComment());
        return ResponseEntity.ok(LeaveResponse.fromEntity(approved));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a leave request", description = "Rejects a pending leave request. Requires MANAGER or ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LeaveResponse> rejectLeave(@PathVariable Long id,
                                                     @Valid @RequestBody LeaveDecisionRequest request) {
        LeaveRequest rejected = leaveService.rejectLeave(id, request.getReviewerId(), request.getReviewComment());
        return ResponseEntity.ok(LeaveResponse.fromEntity(rejected));
    }
}
