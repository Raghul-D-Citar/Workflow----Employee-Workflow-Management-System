package com.workflow.dto.task;

import com.workflow.entity.Task;
import com.workflow.entity.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class TaskCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private TaskPriority priority = TaskPriority.MEDIUM;

    private LocalDate dueDate;

    @NotNull(message = "Creator ID is required")
    private Long creatorId;

    public TaskCreateRequest() {}

    public TaskCreateRequest(String title, String description, TaskPriority priority, LocalDate dueDate, Long creatorId) {
        this.title = title;
        this.description = description;
        this.priority = priority != null ? priority : TaskPriority.MEDIUM;
        this.dueDate = dueDate;
        this.creatorId = creatorId;
    }

    public Task toEntity() {
        Task task = new Task();
        task.setTitle(this.title);
        task.setDescription(this.description);
        task.setPriority(this.priority != null ? this.priority : TaskPriority.MEDIUM);
        task.setDueDate(this.dueDate);
        return task;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
}
