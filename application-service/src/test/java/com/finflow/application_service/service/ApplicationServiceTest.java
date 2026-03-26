package com.finflow.application_service.service;

import com.finflow.application_service.entity.LoanApplication;
import com.finflow.application_service.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @Mock
    private LoanApplicationRepository repository;

    @InjectMocks
    private ApplicationService service;

    private LoanApplication draftApp;
    private LoanApplication submittedApp;

    @BeforeEach
    void setUp() {
        draftApp = new LoanApplication();
        draftApp.setId(1L);
        draftApp.setApplicantId(10L);
        draftApp.setStatus("DRAFT");
        draftApp.setLoanAmount(new BigDecimal("5000"));
        draftApp.setPersonalDetails("John Doe");

        submittedApp = new LoanApplication();
        submittedApp.setId(2L);
        submittedApp.setApplicantId(10L);
        submittedApp.setStatus("SUBMITTED");
        submittedApp.setLoanAmount(new BigDecimal("10000"));
        submittedApp.setPersonalDetails("Jane Doe");
    }

    @Test
    void testCreateDraft() {
        when(repository.save(any(LoanApplication.class))).thenReturn(draftApp);

        LoanApplication result = service.createDraft(10L, draftApp);

        assertNotNull(result);
        assertEquals("DRAFT", result.getStatus());
        assertEquals(10L, result.getApplicantId());
        verify(repository, times(1)).save(draftApp);
    }

    @Test
    void testUpdateApplication_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftApp));
        when(repository.save(any(LoanApplication.class))).thenReturn(draftApp);

        LoanApplication updatedInfo = new LoanApplication();
        updatedInfo.setLoanAmount(new BigDecimal("7000"));
        updatedInfo.setPersonalDetails("Updated details");

        LoanApplication result = service.updateApplication(1L, updatedInfo);

        assertEquals(new BigDecimal("7000"), result.getLoanAmount());
        assertEquals("Updated details", result.getPersonalDetails());
        verify(repository, times(1)).save(draftApp);
    }

    @Test
    void testUpdateApplication_ThrowsExceptionIfNotDraft() {
        when(repository.findById(2L)).thenReturn(Optional.of(submittedApp));

        LoanApplication updatedInfo = new LoanApplication();
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.updateApplication(2L, updatedInfo));
        assertEquals("Cannot update submitted application", ex.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void testSubmitApplication_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftApp));
        when(repository.save(any(LoanApplication.class))).thenReturn(draftApp);

        LoanApplication result = service.submitApplication(1L);

        assertEquals("SUBMITTED", result.getStatus());
        verify(repository, times(1)).save(draftApp);
    }

    @Test
    void testGetAllApplications() {
        when(repository.findAll()).thenReturn(List.of(draftApp, submittedApp));

        List<LoanApplication> result = service.getAllApplications();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetMyApplications() {
        when(repository.findByApplicantId(10L)).thenReturn(List.of(draftApp, submittedApp));

        List<LoanApplication> result = service.getMyApplications(10L);

        assertEquals(2, result.size());
        verify(repository, times(1)).findByApplicantId(10L);
    }

    @Test
    void testGetApplication_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftApp));

        LoanApplication result = service.getApplication(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetApplication_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getApplication(99L));
    }
}
