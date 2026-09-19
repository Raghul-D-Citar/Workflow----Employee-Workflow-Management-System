package com.workflow.repository;

import com.workflow.entity.Department;
import com.workflow.entity.Employee;
import com.workflow.entity.LeaveRequest;
import com.workflow.entity.Task;
import com.workflow.entity.enums.Role;
import com.workflow.entity.enums.LeaveStatus;
import com.workflow.entity.enums.TaskPriority;
import com.workflow.entity.enums.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class RepositoryIntegrationTests {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    public void testDepartmentQueries() {
        Department dept = new Department("IT", "IT Department");
        departmentRepository.save(dept);
        
        assertThat(departmentRepository.findByName("IT")).isPresent();
    }

    @Test
    public void testEmployeeQueries() {
        Department dept = new Department("HR", "HR Department");
        departmentRepository.save(dept);

        Employee emp = new Employee();
        emp.setUsername("john_doe");
        emp.setEmail("john@example.com");
        emp.setPassword("pass");
        emp.setFirstName("John");
        emp.setLastName("Doe");
        emp.setRole(Role.EMPLOYEE);
        emp.setDepartment(dept);
        employeeRepository.save(emp);

        assertThat(employeeRepository.findByEmail("john@example.com")).isPresent();
        assertThat(employeeRepository.findByUsername("john_doe")).isPresent();
        assertThat(employeeRepository.findByDepartmentId(dept.getId())).hasSize(1);
    }

    @Test
    public void testCustomJoinQueries() {
        Department dept = new Department("Engineering", "Eng");
        departmentRepository.save(dept);

        Employee emp = new Employee();
        emp.setUsername("jane");
        emp.setEmail("jane@example.com");
        emp.setPassword("pass");
        emp.setFirstName("Jane");
        emp.setLastName("Doe");
        emp.setRole(Role.EMPLOYEE);
        emp.setDepartment(dept);
        employeeRepository.save(emp);

        LeaveRequest request = new LeaveRequest();
        request.setEmployee(emp);
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(2));
        request.setReason("Sick");
        request.setStatus(LeaveStatus.PENDING);
        leaveRequestRepository.save(request);

        Task task = new Task();
        task.setTitle("Fix Bug");
        task.setDescription("Fix null pointer");
        task.setStatus(TaskStatus.ASSIGNED);
        task.setPriority(TaskPriority.HIGH);
        task.setAssignee(emp);
        task.setCreator(emp);
        taskRepository.save(task);

        // Test the custom JPQL queries
        List<LeaveRequest> deptRequests = leaveRequestRepository.findByDepartmentId(dept.getId());
        assertThat(deptRequests).hasSize(1);

        List<Task> deptTasks = taskRepository.findByDepartmentId(dept.getId());
        assertThat(deptTasks).hasSize(1);
    }
}
