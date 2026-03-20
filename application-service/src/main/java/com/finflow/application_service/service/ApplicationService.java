package com.finflow.application_service.service;

import com.finflow.application_service.entity.LoanApplication;
import com.finflow.application_service.repository.LoanApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {

    @Autowired
    private LoanApplicationRepository repository;

    public LoanApplication createDraft(Long applicantId, LoanApplication application) {
        application.setApplicantId(applicantId);
        application.setStatus("DRAFT");
        return repository.save(application);
    }

    public LoanApplication updateApplication(Long id, LoanApplication updated) {
        LoanApplication existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new RuntimeException("Cannot update submitted application");
        }
        existing.setPersonalDetails(updated.getPersonalDetails());
        existing.setEmploymentDetails(updated.getEmploymentDetails());
        existing.setLoanAmount(updated.getLoanAmount());
        existing.setLoanTermMonths(updated.getLoanTermMonths());
        return repository.save(existing);
    }

    public LoanApplication submitApplication(Long id) {
        LoanApplication existing = repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        existing.setStatus("SUBMITTED");
        return repository.save(existing);
    }

    public List<LoanApplication> getMyApplications(Long applicantId) {
        return repository.findByApplicantId(applicantId);
    }
    
    public LoanApplication getApplication(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
    }
}
