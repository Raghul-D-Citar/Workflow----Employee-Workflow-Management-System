package com.workflow.service;

import com.workflow.entity.Employee;
import com.workflow.entity.Task;
import com.workflow.entity.enums.Role;
import com.workflow.entity.enums.TaskPriority;
import com.workflow.entity.enums.TaskStatus;
import com.workflow.exception.InvalidRequestException;
import com.workflow.exception.InvalidStateTransitionException;
import com.workflow.repository.EmployeeRepository;
import com.workflow.repository.TaskRepository;
import com.workflow.service.impl.TaskServiceImpl;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private AutoCloseable mocks;
    private TaskService taskService;

    @BeforeMethod
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        taskService = new TaskServiceImpl(taskRepository, employeeRepository);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void createTaskStartsAsCreated() {
        Employee creator = employee(1L);
        Task task = task();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task createdTask = taskService.createTask(task, 1L);

        assertThat(createdTask.getStatus()).isEqualTo(TaskStatus.CREATED);
        assertThat(createdTask.getCreator()).isEqualTo(creator);
    }

    @Test
    public void assignTaskMovesFromCreatedToAssigned() {
        Employee creator = employee(1L);
        Employee assignee = employee(2L);
        Task task = task();
        task.setCreator(creator);
        task.setStatus(TaskStatus.CREATED);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task assignedTask = taskService.assignTask(10L, 2L);

        assertThat(assignedTask.getStatus()).isEqualTo(TaskStatus.ASSIGNED);
        assertThat(assignedTask.getAssignee()).isEqualTo(assignee);
    }

    @Test
    public void invalidStatusTransitionIsRejected() {
        Task task = task();
        task.setStatus(TaskStatus.CREATED);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.updateTaskStatus(10L, TaskStatus.COMPLETED))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    public void taskCannotBeCreatedWithAssignee() {
        Task task = task();
        task.setAssignee(employee(2L));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(1L)));

        assertThatThrownBy(() -> taskService.createTask(task, 1L))
                .isInstanceOf(InvalidRequestException.class);
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

    private Task task() {
        Task task = new Task();
        task.setId(10L);
        task.setTitle("Task");
        task.setDescription("Task description");
        task.setPriority(TaskPriority.HIGH);
        return task;
    }
}