package com.workflow.controller;

import com.workflow.dto.task.TaskCreateRequest;
import com.workflow.dto.task.TaskResponse;
import com.workflow.dto.task.TaskStatusUpdateRequest;
import com.workflow.entity.Task;
import com.workflow.exception.InvalidRequestException;
import com.workflow.service.TaskService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task creation, assignment, and status workflows")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a task", description = "Creates a task. Requires MANAGER or ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest request) {
        Task task = request.toEntity();
        Task created = taskService.createTask(task, request.getCreatorId());
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.fromEntity(created));
    }

    @GetMapping("/my")
    @Operation(summary = "List assigned tasks", description = "Lists tasks assigned to an employee. Requires JWT authentication.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TaskResponse>> getMyTasks(
            @RequestParam(name = "employeeId", required = false) Long employeeId,
            @RequestHeader(name = "X-Employee-Id", required = false) Long headerEmpId) {
        Long targetEmployeeId = employeeId != null ? employeeId : headerEmpId;
        if (targetEmployeeId == null) {
            throw new InvalidRequestException("Employee ID must be provided via query param or header");
        }

        List<TaskResponse> tasks = taskService.getAssignedTasks(targetEmployeeId)
                .stream()
                .map(TaskResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task", description = "Returns a task by ID. Requires JWT authentication.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTask(id);
        return ResponseEntity.ok(TaskResponse.fromEntity(task));
    }

    @GetMapping("/team")
    @Operation(summary = "List team tasks", description = "Lists tasks for a department. Requires MANAGER or ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<TaskResponse>> getTeamTasks(@RequestParam Long departmentId) {
        List<TaskResponse> tasks = taskService.getTeamTasks(departmentId)
                .stream()
                .map(TaskResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update task status", description = "Advances a task through its valid workflow. Requires JWT authentication.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long id,
                                                         @Valid @RequestBody TaskStatusUpdateRequest request) {
        Task updated = taskService.updateTaskStatus(id, request.getStatus());
        return ResponseEntity.ok(TaskResponse.fromEntity(updated));
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign a task", description = "Assigns a task to an employee. Requires MANAGER or ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TaskResponse> assignTask(@PathVariable Long id,
                                                   @RequestParam Long assigneeId) {
        Task assigned = taskService.assignTask(id, assigneeId);
        return ResponseEntity.ok(TaskResponse.fromEntity(assigned));
    }
}
