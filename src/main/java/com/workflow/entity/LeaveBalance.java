package com.workflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "leave_balances")
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @Column(nullable = false)
    private Integer annualLeave = 0;

    @Column(nullable = false)
    private Integer sickLeave = 0;

    @Column(nullable = false)
    private Integer casualLeave = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Constructors
    public LeaveBalance() {}

    public LeaveBalance(Employee employee, Integer annualLeave, Integer sickLeave, Integer casualLeave) {
        this.employee = employee;
        this.annualLeave = annualLeave;
        this.sickLeave = sickLeave;
        this.casualLeave = casualLeave;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Integer getAnnualLeave() { return annualLeave; }
    public void setAnnualLeave(Integer annualLeave) { this.annualLeave = annualLeave; }

    public Integer getSickLeave() { return sickLeave; }
    public void setSickLeave(Integer sickLeave) { this.sickLeave = sickLeave; }

    public Integer getCasualLeave() { return casualLeave; }
    public void setCasualLeave(Integer casualLeave) { this.casualLeave = casualLeave; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
