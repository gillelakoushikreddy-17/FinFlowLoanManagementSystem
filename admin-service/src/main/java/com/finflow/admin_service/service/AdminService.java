package com.finflow.admin_service.service;

import com.finflow.admin_service.entity.Decision;
import com.finflow.admin_service.entity.Report;
import com.finflow.admin_service.repository.DecisionRepository;
import com.finflow.admin_service.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private DecisionRepository decisionRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private RestTemplate restTemplate;

    // We can call application-service to get all applications
    public Object[] getAllApplications() {
        // Assume application-service exposes /all for admins
        // Since we didn't implement /all in application-service yet, we can just call a mocked endpoint or implement it later
        // return restTemplate.getForObject("http://application-service/all", Object[].class);
        return new Object[]{}; // Placeholder
    }

    public Object verifyDocument(Long documentId) {
        // Call document-service to update document status to VERIFIED
        // restTemplate.put("http://document-service/" + documentId + "/verify", null);
        return "Document " + documentId + " verified"; // Placeholder
    }

    public Decision makeDecision(Long applicationId, Long adminId, String decisionType, String remarks) {
        Decision decision = new Decision(null, applicationId, adminId, decisionType, remarks, LocalDateTime.now());
        
        // Also call application-service to update application status to APPROVED/REJECTED
        // restTemplate.put("http://application-service/" + applicationId + "/status?newStatus=" + decisionType, null);
        
        return decisionRepository.save(decision);
    }

    public List<Report> getReports() {
        return reportRepository.findAll();
    }
    
    public Object[] getAllUsers() {
        // Call auth-service to get all users
        return new Object[]{}; // Placeholder
    }
}
