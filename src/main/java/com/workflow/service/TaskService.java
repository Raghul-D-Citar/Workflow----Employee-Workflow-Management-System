package com.workflow.service;

import com.workflow.entity.Task;
import com.workflow.entity.enums.TaskStatus;

import java.util.List;

public interface TaskService {

    Task createTask(Task task, Long creatorId);

    Task assignTask(Long taskId, Long assigneeId);

    List<Task> getAssignedTasks(Long employeeId);

    Task updateTaskStatus(Long taskId, TaskStatus taskStatus);

    List<Task> getTeamTasks(Long departmentId);
}