package com.finflow.document_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long applicantId;
    private Long applicationId;
    
    private String type;
    private String fileName;
    private String filePath;
    private String status;
    
    private LocalDateTime uploadedAt;

    public Document() {}

    public Document(Long id, Long applicantId, Long applicationId, String type, String fileName, String filePath, String status, LocalDateTime uploadedAt) {
        this.id = id;
        this.applicantId = applicantId;
        this.applicationId = applicationId;
        this.type = type;
        this.fileName = fileName;
        this.filePath = filePath;
        this.status = status;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
