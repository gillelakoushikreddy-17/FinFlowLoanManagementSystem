package com.finflow.admin_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "decisions")
public class Decision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long applicationId;
    private Long adminId;
    
    private String decisionType;
    private String remarks;
    
    private LocalDateTime decisionDate;

    public Decision() {}

    public Decision(Long id, Long applicationId, Long adminId, String decisionType, String remarks, LocalDateTime decisionDate) {
        this.id = id;
        this.applicationId = applicationId;
        this.adminId = adminId;
        this.decisionType = decisionType;
        this.remarks = remarks;
        this.decisionDate = decisionDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public String getDecisionType() { return decisionType; }
    public void setDecisionType(String decisionType) { this.decisionType = decisionType; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getDecisionDate() { return decisionDate; }
    public void setDecisionDate(LocalDateTime decisionDate) { this.decisionDate = decisionDate; }
}
