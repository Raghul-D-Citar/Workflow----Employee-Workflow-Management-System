package com.workflow.controller;

import com.workflow.dto.task.TaskCreateRequest;
import com.workflow.dto.task.TaskResponse;
import com.workflow.dto.task.TaskStatusUpdateRequest;
import com.workflow.entity.Task;
import com.workflow.exception.InvalidRequestException;
import com.workflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest request) {
        Task task = request.toEntity();
        Task created = taskService.createTask(task, request.getCreatorId());
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.fromEntity(created));
    }

    @GetMapping("/my")
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
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTask(id);
        return ResponseEntity.ok(TaskResponse.fromEntity(task));
    }

    @GetMapping("/team")
    public ResponseEntity<List<TaskResponse>> getTeamTasks(@RequestParam Long departmentId) {
        List<TaskResponse> tasks = taskService.getTeamTasks(departmentId)
                .stream()
                .map(TaskResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long id,
                                                         @Valid @RequestBody TaskStatusUpdateRequest request) {
        Task updated = taskService.updateTaskStatus(id, request.getStatus());
        return ResponseEntity.ok(TaskResponse.fromEntity(updated));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<TaskResponse> assignTask(@PathVariable Long id,
                                                   @RequestParam Long assigneeId) {
        Task assigned = taskService.assignTask(id, assigneeId);
        return ResponseEntity.ok(TaskResponse.fromEntity(assigned));
    }
}
