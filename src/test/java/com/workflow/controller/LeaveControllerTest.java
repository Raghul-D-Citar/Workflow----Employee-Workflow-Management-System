package com.workflow.controller;

import com.workflow.entity.Employee;
import com.workflow.entity.LeaveRequest;
import com.workflow.entity.enums.LeaveStatus;
import com.workflow.entity.enums.Role;
import com.workflow.exception.GlobalExceptionHandler;
import com.workflow.exception.InsufficientLeaveBalanceException;
import com.workflow.exception.InvalidStateTransitionException;
import com.workflow.exception.ResourceNotFoundException;
import com.workflow.service.LeaveService;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class LeaveControllerTest {

    private LeaveService leaveService;
    private LeaveController leaveController;
    private MockMvc mockMvc;

    @BeforeMethod
    public void setUp() {
        leaveService = Mockito.mock(LeaveService.class);
        leaveController = new LeaveController(leaveService);
        mockMvc = MockMvcBuilders.standaloneSetup(leaveController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void createLeaveRequestSuccess() throws Exception {
        String jsonPayload = """
                {
                    "employeeId": 1,
                    "startDate": "2026-11-01",
                    "endDate": "2026-11-05",
                    "reason": "Vacation"
                }
                """;

        LeaveRequest created = sampleLeaveRequest(10L, 1L, LeaveStatus.PENDING);

        when(leaveService.createLeaveRequest(eq(1L), any(LocalDate.class), any(LocalDate.class), eq("Vacation")))
                .thenReturn(created);

        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.employeeId").value(1));
    }

    @Test
    public void createLeaveRequestValidationError() throws Exception {
        String invalid = "{}";

        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    public void getMyLeaveRequestsSuccess() throws Exception {
        LeaveRequest leave = sampleLeaveRequest(10L, 1L, LeaveStatus.PENDING);
        when(leaveService.getOwnLeaveRequests(1L)).thenReturn(List.of(leave));

        mockMvc.perform(get("/api/leaves/my?employeeId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    public void getMyLeaveRequestsMissingParam() throws Exception {
        mockMvc.perform(get("/api/leaves/my"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    public void getPendingLeaveRequestsSuccess() throws Exception {
        LeaveRequest leave = sampleLeaveRequest(10L, 1L, LeaveStatus.PENDING);
        when(leaveService.getPendingLeaveRequests()).thenReturn(List.of(leave));

        mockMvc.perform(get("/api/leaves/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    public void getLeaveRequestByIdSuccess() throws Exception {
        LeaveRequest leave = sampleLeaveRequest(10L, 1L, LeaveStatus.PENDING);
        when(leaveService.getLeaveRequest(10L)).thenReturn(leave);

        mockMvc.perform(get("/api/leaves/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    public void getLeaveRequestByIdNotFound() throws Exception {
        when(leaveService.getLeaveRequest(999L)).thenThrow(new ResourceNotFoundException("Leave request not found: 999"));

        mockMvc.perform(get("/api/leaves/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    public void approveLeaveSuccess() throws Exception {
        String decisionPayload = """
                {
                    "reviewerId": 2,
                    "reviewComment": "Approved by manager"
                }
                """;

        LeaveRequest approved = sampleLeaveRequest(10L, 1L, LeaveStatus.APPROVED);

        when(leaveService.approveLeave(eq(10L), eq(2L), eq("Approved by manager"))).thenReturn(approved);

        mockMvc.perform(put("/api/leaves/10/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decisionPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    public void approveLeaveInsufficientBalance() throws Exception {
        String decisionPayload = """
                {
                    "reviewerId": 2,
                    "reviewComment": "Approve attempt"
                }
                """;

        when(leaveService.approveLeave(eq(10L), eq(2L), anyString()))
                .thenThrow(new InsufficientLeaveBalanceException("Insufficient annual leave balance"));

        mockMvc.perform(put("/api/leaves/10/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decisionPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Insufficient annual leave balance"));
    }

    @Test
    public void rejectLeaveSuccess() throws Exception {
        String decisionPayload = """
                {
                    "reviewerId": 2,
                    "reviewComment": "Rejected due to coverage"
                }
                """;

        LeaveRequest rejected = sampleLeaveRequest(10L, 1L, LeaveStatus.REJECTED);

        when(leaveService.rejectLeave(eq(10L), eq(2L), eq("Rejected due to coverage"))).thenReturn(rejected);

        mockMvc.perform(put("/api/leaves/10/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decisionPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    public void rejectLeaveInvalidTransition() throws Exception {
        String decisionPayload = """
                {
                    "reviewerId": 2,
                    "reviewComment": "Reject attempt"
                }
                """;

        when(leaveService.rejectLeave(eq(10L), eq(2L), anyString()))
                .thenThrow(new InvalidStateTransitionException("Only pending leave requests can be processed"));

        mockMvc.perform(put("/api/leaves/10/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(decisionPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Only pending leave requests can be processed"));
    }

    private LeaveRequest sampleLeaveRequest(Long id, Long employeeId, LeaveStatus status) {
        Employee employee = new Employee();
        employee.setId(employeeId);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setUsername("johndoe");
        employee.setEmail("john@example.com");
        employee.setRole(Role.EMPLOYEE);

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(id);
        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(LocalDate.of(2026, 11, 1));
        leaveRequest.setEndDate(LocalDate.of(2026, 11, 5));
        leaveRequest.setReason("Vacation");
        leaveRequest.setStatus(status);
        return leaveRequest;
    }
}
