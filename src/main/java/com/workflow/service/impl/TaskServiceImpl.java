package com.workflow.service.impl;

import com.workflow.entity.Employee;
import com.workflow.entity.Task;
import com.workflow.entity.enums.TaskStatus;
import com.workflow.exception.InvalidRequestException;
import com.workflow.exception.InvalidStateTransitionException;
import com.workflow.exception.ResourceNotFoundException;
import com.workflow.repository.EmployeeRepository;
import com.workflow.repository.TaskRepository;
import com.workflow.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Map<TaskStatus, TaskStatus> VALID_TRANSITIONS = Map.of(
            TaskStatus.CREATED, TaskStatus.ASSIGNED,
            TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS,
            TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED
    );

    private final TaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;

    public TaskServiceImpl(TaskRepository taskRepository, EmployeeRepository employeeRepository) {
        this.taskRepository = taskRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public Task createTask(Task task, Long creatorId) {
        if (task == null) {
            throw new InvalidRequestException("Task payload is required");
        }

        if (task.getAssignee() != null) {
            throw new InvalidRequestException("Task must be created without an assignee");
        }

        Employee creator = getEmployee(creatorId);
        task.setCreator(creator);
        task.setStatus(TaskStatus.CREATED);

        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task assignTask(Long taskId, Long assigneeId) {
        Task task = getTask(taskId);
        validateTransition(task.getStatus(), TaskStatus.ASSIGNED);

        Employee assignee = getEmployee(assigneeId);
        task.setAssignee(assignee);
        task.setStatus(TaskStatus.ASSIGNED);

        return taskRepository.save(task);
    }

    @Override
    public List<Task> getAssignedTasks(Long employeeId) {
        return taskRepository.findByAssigneeId(employeeId);
    }

    @Override
    @Transactional
    public Task updateTaskStatus(Long taskId, TaskStatus taskStatus) {
        Task task = getTask(taskId);
        validateTransition(task.getStatus(), taskStatus);

        if (taskStatus == TaskStatus.ASSIGNED && task.getAssignee() == null) {
            throw new InvalidRequestException("A task cannot be marked ASSIGNED without an assignee");
        }

        task.setStatus(taskStatus);
        return taskRepository.save(task);
    }

    @Override
    public List<Task> getTeamTasks(Long departmentId) {
        return taskRepository.findByDepartmentId(departmentId);
    }

    private Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
    }

    private Employee getEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));
    }

    private void validateTransition(TaskStatus currentStatus, TaskStatus targetStatus) {
        TaskStatus expectedNextStatus = VALID_TRANSITIONS.get(currentStatus);
        if (expectedNextStatus != targetStatus) {
            throw new InvalidStateTransitionException(
                    "Invalid task status transition from " + currentStatus + " to " + targetStatus);
        }
    }
}