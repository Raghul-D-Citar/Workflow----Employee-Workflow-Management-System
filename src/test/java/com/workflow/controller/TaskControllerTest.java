package com.workflow.controller;

import com.workflow.entity.Employee;
import com.workflow.entity.Task;
import com.workflow.entity.enums.Role;
import com.workflow.entity.enums.TaskPriority;
import com.workflow.entity.enums.TaskStatus;
import com.workflow.exception.GlobalExceptionHandler;
import com.workflow.exception.InvalidStateTransitionException;
import com.workflow.exception.ResourceNotFoundException;
import com.workflow.service.TaskService;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TaskControllerTest {

    private TaskService taskService;
    private TaskController taskController;
    private MockMvc mockMvc;

    @BeforeMethod
    public void setUp() {
        taskService = Mockito.mock(TaskService.class);
        taskController = new TaskController(taskService);
        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void createTaskSuccess() throws Exception {
        String jsonPayload = """
                {
                    "title": "Task Title",
                    "description": "Description",
                    "priority": "HIGH",
                    "dueDate": "2026-11-10",
                    "creatorId": 1
                }
                """;

        Task created = sampleTask(100L, "Task Title", TaskStatus.CREATED);

        when(taskService.createTask(any(Task.class), eq(1L))).thenReturn(created);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("Task Title"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    public void createTaskValidationError() throws Exception {
        String invalid = "{}";

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    public void getMyTasksSuccess() throws Exception {
        Task task = sampleTask(100L, "Assigned Task", TaskStatus.ASSIGNED);
        when(taskService.getAssignedTasks(1L)).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks/my?employeeId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    public void getMyTasksMissingParam() throws Exception {
        mockMvc.perform(get("/api/tasks/my"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    public void getTaskByIdSuccess() throws Exception {
        Task task = sampleTask(100L, "Assigned Task", TaskStatus.ASSIGNED);
        when(taskService.getTask(100L)).thenReturn(task);

        mockMvc.perform(get("/api/tasks/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.title").value("Assigned Task"));
    }

    @Test
    public void getTaskByIdNotFound() throws Exception {
        when(taskService.getTask(999L)).thenThrow(new ResourceNotFoundException("Task not found: 999"));

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    public void getTeamTasksSuccess() throws Exception {
        Task task = sampleTask(100L, "Team Task", TaskStatus.IN_PROGRESS);
        when(taskService.getTeamTasks(5L)).thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks/team?departmentId=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].title").value("Team Task"));
    }

    @Test
    public void updateTaskStatusSuccess() throws Exception {
        String jsonPayload = """
                {
                    "status": "IN_PROGRESS"
                }
                """;

        Task updated = sampleTask(100L, "Task Title", TaskStatus.IN_PROGRESS);

        when(taskService.updateTaskStatus(eq(100L), eq(TaskStatus.IN_PROGRESS))).thenReturn(updated);

        mockMvc.perform(put("/api/tasks/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    public void updateTaskStatusInvalidTransition() throws Exception {
        String jsonPayload = """
                {
                    "status": "COMPLETED"
                }
                """;

        when(taskService.updateTaskStatus(eq(100L), eq(TaskStatus.COMPLETED)))
                .thenThrow(new InvalidStateTransitionException("Invalid task status transition from CREATED to COMPLETED"));

        mockMvc.perform(put("/api/tasks/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid task status transition from CREATED to COMPLETED"));
    }

    @Test
    public void assignTaskSuccess() throws Exception {
        Task assigned = sampleTask(100L, "Task Title", TaskStatus.ASSIGNED);

        when(taskService.assignTask(eq(100L), eq(2L))).thenReturn(assigned);

        mockMvc.perform(put("/api/tasks/100/assign?assigneeId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    private Task sampleTask(Long id, String title, TaskStatus status) {
        Employee creator = new Employee();
        creator.setId(1L);
        creator.setFirstName("Alice");
        creator.setLastName("Admin");
        creator.setUsername("alice");
        creator.setRole(Role.ADMIN);

        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription("Description");
        task.setStatus(status);
        task.setPriority(TaskPriority.MEDIUM);
        task.setCreator(creator);
        return task;
    }
}
