package com.finflow.application_service.service;

import com.finflow.application_service.entity.LoanApplication;
import com.finflow.application_service.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ApplicationServiceTest {

    @Mock
    private LoanApplicationRepository repository;

    @InjectMocks
    private ApplicationService service;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateDraft() {
        LoanApplication app = new LoanApplication();
        app.setLoanAmount(new BigDecimal("50000"));

        when(repository.save(any(LoanApplication.class))).thenAnswer(i -> {
            LoanApplication saved = (LoanApplication) i.getArguments()[0];
            saved.setId(1L);
            return saved;
        });

        LoanApplication result = service.createDraft(100L, app);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("DRAFT", result.getStatus());
        assertEquals(100L, result.getApplicantId());
    }

    @Test
    public void testSubmitApplication() {
        LoanApplication existing = new LoanApplication();
        existing.setId(1L);
        existing.setStatus("DRAFT");
        
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(LoanApplication.class))).thenReturn(existing);

        LoanApplication result = service.submitApplication(1L);

        assertEquals("SUBMITTED", result.getStatus());
        verify(repository, times(1)).save(existing);
    }
}
