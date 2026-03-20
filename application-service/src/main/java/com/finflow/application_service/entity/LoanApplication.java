package com.finflow.application_service.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "loan_applications")
public class LoanApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long applicantId;
    private String personalDetails;
    private String employmentDetails;
    private BigDecimal loanAmount;
    private Integer loanTermMonths;
    private String status;

    public LoanApplication() {}

    public LoanApplication(Long id, Long applicantId, String personalDetails, String employmentDetails, BigDecimal loanAmount, Integer loanTermMonths, String status) {
        this.id = id;
        this.applicantId = applicantId;
        this.personalDetails = personalDetails;
        this.employmentDetails = employmentDetails;
        this.loanAmount = loanAmount;
        this.loanTermMonths = loanTermMonths;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getApplicantId() { return applicantId; }
    public void setApplicantId(Long applicantId) { this.applicantId = applicantId; }

    public String getPersonalDetails() { return personalDetails; }
    public void setPersonalDetails(String personalDetails) { this.personalDetails = personalDetails; }

    public String getEmploymentDetails() { return employmentDetails; }
    public void setEmploymentDetails(String employmentDetails) { this.employmentDetails = employmentDetails; }

    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }

    public Integer getLoanTermMonths() { return loanTermMonths; }
    public void setLoanTermMonths(Integer loanTermMonths) { this.loanTermMonths = loanTermMonths; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
