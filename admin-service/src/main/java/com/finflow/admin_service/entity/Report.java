package com.finflow.admin_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String type;
    
    @Lob
    private String dataJson;
    
    private LocalDateTime generatedDate;

    public Report() {}

    public Report(Long id, String type, String dataJson, LocalDateTime generatedDate) {
        this.id = id;
        this.type = type;
        this.dataJson = dataJson;
        this.generatedDate = generatedDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }
    public LocalDateTime getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDateTime generatedDate) { this.generatedDate = generatedDate; }
}
