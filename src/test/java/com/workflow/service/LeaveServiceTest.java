package com.workflow.service;

import com.workflow.entity.Employee;
import com.workflow.entity.LeaveBalance;
import com.workflow.entity.LeaveRequest;
import com.workflow.entity.enums.LeaveStatus;
import com.workflow.entity.enums.Role;
import com.workflow.exception.InsufficientLeaveBalanceException;
import com.workflow.exception.InvalidRequestException;
import com.workflow.exception.InvalidStateTransitionException;
import com.workflow.repository.EmployeeRepository;
import com.workflow.repository.LeaveBalanceRepository;
import com.workflow.repository.LeaveRequestRepository;
import com.workflow.service.impl.LeaveServiceImpl;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private AutoCloseable mocks;
    private LeaveService leaveService;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        leaveService = new LeaveServiceImpl(leaveRequestRepository, leaveBalanceRepository, employeeRepository);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void createLeaveRequestCreatesPendingRequest() {
        Employee employee = employee(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequest leaveRequest = leaveService.createLeaveRequest(1L, LocalDate.now(), LocalDate.now().plusDays(2), "Vacation");

        assertThat(leaveRequest.getStatus()).isEqualTo(LeaveStatus.PENDING);
        assertThat(leaveRequest.getEmployee()).isEqualTo(employee);
        verify(leaveRequestRepository).save(leaveRequest);
    }

    @Test
    public void createLeaveRequestRejectsInvalidDates() {
        Employee employee = employee(1L);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> leaveService.createLeaveRequest(1L, LocalDate.now().plusDays(1), LocalDate.now(), "Vacation"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Start date cannot be after end date");
    }

    @Test
    public void approveLeaveDeductsBalance() {
        Employee employee = employee(1L);
        Employee reviewer = employee(2L);
        LeaveRequest leaveRequest = pendingLeaveRequest(employee, 10L);
        LeaveBalance leaveBalance = new LeaveBalance(employee, 5, 0, 0);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(leaveBalance));
        when(leaveBalanceRepository.save(any(LeaveBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequest approvedRequest = leaveService.approveLeave(10L, 2L, "Approved");

        assertThat(approvedRequest.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(leaveBalance.getAnnualLeave()).isEqualTo(2);
        verify(leaveBalanceRepository).save(leaveBalance);
    }

    @Test
    public void rejectLeaveDoesNotDeductBalance() {
        Employee employee = employee(1L);
        Employee reviewer = employee(2L);
        LeaveRequest leaveRequest = pendingLeaveRequest(employee, 10L);
        LeaveBalance leaveBalance = new LeaveBalance(employee, 5, 0, 0);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(leaveBalance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LeaveRequest rejectedRequest = leaveService.rejectLeave(10L, 2L, "Rejected");

        assertThat(rejectedRequest.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        assertThat(leaveBalance.getAnnualLeave()).isEqualTo(5);
    }

    @Test
    public void approveLeaveFailsWhenBalanceIsInsufficient() {
        Employee employee = employee(1L);
        Employee reviewer = employee(2L);
        LeaveRequest leaveRequest = pendingLeaveRequest(employee, 10L);
        LeaveBalance leaveBalance = new LeaveBalance(employee, 1, 0, 0);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(leaveBalanceRepository.findByEmployeeId(1L)).thenReturn(Optional.of(leaveBalance));

        assertThatThrownBy(() -> leaveService.approveLeave(10L, 2L, "Approved"))
                .isInstanceOf(InsufficientLeaveBalanceException.class);
    }

    @Test
    public void duplicateLeaveProcessingIsRejected() {
        Employee employee = employee(1L);
        Employee reviewer = employee(2L);
        LeaveRequest leaveRequest = pendingLeaveRequest(employee, 10L);
        leaveRequest.setStatus(LeaveStatus.APPROVED);

        when(leaveRequestRepository.findById(10L)).thenReturn(Optional.of(leaveRequest));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(reviewer));

        assertThatThrownBy(() -> leaveService.rejectLeave(10L, 2L, "Rejected"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    private Employee employee(Long id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setUsername("user" + id);
        employee.setEmail("user" + id + "@example.com");
        employee.setPassword("secret");
        employee.setFirstName("First");
        employee.setLastName("Last");
        employee.setRole(Role.EMPLOYEE);
        return employee;
    }

    private LeaveRequest pendingLeaveRequest(Employee employee, Long id) {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setId(id);
        leaveRequest.setEmployee(employee);
        leaveRequest.setStartDate(LocalDate.now());
        leaveRequest.setEndDate(LocalDate.now().plusDays(2));
        leaveRequest.setReason("Vacation");
        leaveRequest.setStatus(LeaveStatus.PENDING);
        return leaveRequest;
    }
}