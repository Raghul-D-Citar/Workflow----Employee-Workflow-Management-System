package com.workflow.repository;

import com.workflow.entity.Task;
import com.workflow.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssigneeId(Long assigneeId);

    List<Task> findByCreatorId(Long creatorId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByAssigneeIdAndStatus(Long assigneeId, TaskStatus status);

    @Query("SELECT t FROM Task t JOIN t.assignee e WHERE e.department.id = :departmentId")
    List<Task> findByDepartmentId(@Param("departmentId") Long departmentId);
}
