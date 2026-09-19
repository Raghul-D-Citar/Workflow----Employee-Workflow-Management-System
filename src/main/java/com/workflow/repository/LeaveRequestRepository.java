package com.workflow.repository;

import com.workflow.entity.LeaveRequest;
import com.workflow.entity.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    
    List<LeaveRequest> findByStatus(LeaveStatus status);
    
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
    
    @Query("SELECT lr FROM LeaveRequest lr JOIN lr.employee e WHERE e.department.id = :departmentId")
    List<LeaveRequest> findByDepartmentId(@Param("departmentId") Long departmentId);
}
