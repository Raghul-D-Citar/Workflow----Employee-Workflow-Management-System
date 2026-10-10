package com.workflow.service.impl;

import com.workflow.dto.auth.AuthResponse;
import com.workflow.dto.auth.LoginRequest;
import com.workflow.dto.auth.RegisterRequest;
import com.workflow.entity.Department;
import com.workflow.entity.Employee;
import com.workflow.entity.LeaveBalance;
import com.workflow.entity.enums.Role;
import com.workflow.exception.DuplicateResourceException;
import com.workflow.exception.InvalidRequestException;
import com.workflow.exception.ResourceNotFoundException;
import com.workflow.repository.DepartmentRepository;
import com.workflow.repository.EmployeeRepository;
import com.workflow.repository.LeaveBalanceRepository;
import com.workflow.security.JwtTokenProvider;
import com.workflow.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           LeaveBalanceRepository leaveBalanceRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Registration request payload is required");
        }

        if (employeeRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }

        if (employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        Employee employee = request.toEntity();
        employee.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + request.getDepartmentId()));
            employee.setDepartment(department);
        }

        Employee saved = employeeRepository.save(employee);

        // Initialize default leave balance
        LeaveBalance leaveBalance = new LeaveBalance(saved, 15, 10, 5);
        leaveBalanceRepository.save(leaveBalance);

        String token = jwtTokenProvider.generateToken(saved.getUsername(), saved.getRole().name(), saved.getId());
        return new AuthResponse(token, saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        if (request == null) {
            throw new InvalidRequestException("Login payload is required");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidRequestException("Invalid username or password");
        }

        Employee employee = employeeRepository.findByUsername(request.getUsername())
                .or(() -> employeeRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUsername()));

        String token = jwtTokenProvider.generateToken(employee.getUsername(), employee.getRole().name(), employee.getId());
        return new AuthResponse(token, employee.getId(), employee.getUsername(), employee.getEmail(), employee.getRole());
    }
}
