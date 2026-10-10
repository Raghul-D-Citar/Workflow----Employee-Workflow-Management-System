package com.workflow.service;

import com.workflow.dto.auth.AuthResponse;
import com.workflow.dto.auth.LoginRequest;
import com.workflow.dto.auth.RegisterRequest;
import com.workflow.entity.Department;
import com.workflow.entity.Employee;
import com.workflow.entity.LeaveBalance;
import com.workflow.entity.enums.Role;
import com.workflow.exception.DuplicateResourceException;
import com.workflow.exception.InvalidRequestException;
import com.workflow.repository.DepartmentRepository;
import com.workflow.repository.EmployeeRepository;
import com.workflow.repository.LeaveBalanceRepository;
import com.workflow.security.JwtTokenProvider;
import com.workflow.service.impl.AuthServiceImpl;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthServiceTest {

    private EmployeeRepository employeeRepository;
    private DepartmentRepository departmentRepository;
    private LeaveBalanceRepository leaveBalanceRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    @BeforeMethod
    public void setUp() {
        employeeRepository = Mockito.mock(EmployeeRepository.class);
        departmentRepository = Mockito.mock(DepartmentRepository.class);
        leaveBalanceRepository = Mockito.mock(LeaveBalanceRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        authenticationManager = Mockito.mock(AuthenticationManager.class);
        jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);

        authService = new AuthServiceImpl(
                employeeRepository,
                departmentRepository,
                leaveBalanceRepository,
                passwordEncoder,
                authenticationManager,
                jwtTokenProvider
        );
    }

    @Test
    public void registerSuccess() {
        RegisterRequest request = new RegisterRequest("john", "john@example.com", "secret", "John", "Doe", Role.EMPLOYEE, 1L);
        Department dept = new Department("IT", "IT Dept");
        dept.setId(1L);

        when(employeeRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(passwordEncoder.encode("secret")).thenReturn("encodedPassword");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee emp = invocation.getArgument(0);
            emp.setId(10L);
            return emp;
        });
        when(jwtTokenProvider.generateToken("john", "EMPLOYEE", 10L)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getUsername()).isEqualTo("john");
        assertThat(response.getRole()).isEqualTo(Role.EMPLOYEE);

        verify(leaveBalanceRepository).save(any(LeaveBalance.class));
        verify(passwordEncoder).encode("secret");
    }

    @Test
    public void registerDuplicateUsernameThrowsException() {
        RegisterRequest request = new RegisterRequest("john", "john@example.com", "secret", "John", "Doe", Role.EMPLOYEE, null);
        when(employeeRepository.findByUsername("john")).thenReturn(Optional.of(new Employee()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    public void registerDuplicateEmailThrowsException() {
        RegisterRequest request = new RegisterRequest("john", "john@example.com", "secret", "John", "Doe", Role.EMPLOYEE, null);
        when(employeeRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("john@example.com")).thenReturn(Optional.of(new Employee()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    public void loginSuccess() {
        LoginRequest request = new LoginRequest("john", "password123");
        Employee employee = new Employee();
        employee.setId(10L);
        employee.setUsername("john");
        employee.setEmail("john@example.com");
        employee.setRole(Role.EMPLOYEE);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("john", "password123"));
        when(employeeRepository.findByUsername("john")).thenReturn(Optional.of(employee));
        when(jwtTokenProvider.generateToken("john", "EMPLOYEE", 10L)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getUsername()).isEqualTo("john");
        assertThat(response.getId()).isEqualTo(10L);
    }

    @Test
    public void loginInvalidCredentialsThrowsException() {
        LoginRequest request = new LoginRequest("john", "wrongpass");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Invalid username or password");
    }
}
