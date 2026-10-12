package com.workflow.service;

import com.workflow.entity.Department;
import com.workflow.entity.Employee;
import com.workflow.entity.enums.Role;
import com.workflow.exception.DuplicateResourceException;
import com.workflow.exception.ResourceNotFoundException;
import com.workflow.repository.DepartmentRepository;
import com.workflow.repository.EmployeeRepository;
import com.workflow.service.impl.EmployeeServiceImpl;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    private AutoCloseable mocks;
    private EmployeeService employeeService;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        employeeService = new EmployeeServiceImpl(employeeRepository, departmentRepository);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void createEmployeeAssignsDepartment() {
        Department department = new Department("Engineering", "Engineering");
        department.setId(7L);

        Employee employee = new Employee();
        employee.setUsername("jane");
        employee.setEmail("jane@example.com");
        employee.setPassword("secret");
        employee.setFirstName("Jane");
        employee.setLastName("Doe");
        employee.setRole(Role.EMPLOYEE);

        when(employeeRepository.findByUsername("jane")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(departmentRepository.findById(7L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee createdEmployee = employeeService.createEmployee(employee, 7L);

        assertThat(createdEmployee.getDepartment()).isEqualTo(department);
        verify(employeeRepository).save(employee);
    }

    @Test
    public void createEmployeeRejectsDuplicateUsername() {
        Employee employee = new Employee();
        employee.setUsername("jane");
        employee.setEmail("jane@example.com");

        when(employeeRepository.findByUsername("jane")).thenReturn(Optional.of(new Employee()));

        assertThatThrownBy(() -> employeeService.createEmployee(employee, null))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    public void getEmployeeThrowsWhenMissing() {
        when(employeeRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployee(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void updateEmployeeChangesProvidedFieldsAndDepartment() {
        Employee existing = employee(1L, "old", "old@example.com");
        Employee update = employee(null, "new", "new@example.com");
        update.setFirstName("Updated");
        update.setLastName(null);
        Department department = new Department("Finance", "Finance");
        department.setId(8L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.findByUsername("new")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(departmentRepository.findById(8L)).thenReturn(Optional.of(department));
        when(employeeRepository.save(existing)).thenReturn(existing);

        Employee result = employeeService.updateEmployee(1L, update, 8L);

        assertThat(result.getUsername()).isEqualTo("new");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getFirstName()).isEqualTo("Updated");
        assertThat(result.getLastName()).isEqualTo("Last");
        assertThat(result.getDepartment()).isEqualTo(department);
        verify(employeeRepository).save(existing);
    }

    @Test
    public void updateEmployeeThrowsWhenTargetDoesNotExist() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(99L, new Employee(), null))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(employeeRepository, never()).save(any(Employee.class));
    }

    private Employee employee(Long id, String username, String email) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setUsername(username);
        employee.setEmail(email);
        employee.setPassword("secret");
        employee.setFirstName("First");
        employee.setLastName("Last");
        employee.setRole(Role.EMPLOYEE);
        return employee;
    }
}