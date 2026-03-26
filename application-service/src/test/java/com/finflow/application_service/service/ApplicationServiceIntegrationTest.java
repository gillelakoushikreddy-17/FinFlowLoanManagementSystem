package com.finflow.application_service.service;

import com.finflow.application_service.entity.LoanApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
public class ApplicationServiceIntegrationTest {

    @Autowired
    private ApplicationService service;

    @Test
    public void testCreateDraft() {
        LoanApplication app = new LoanApplication();
        app.setPersonalDetails("Test");
        app.setEmploymentDetails("Dev");
        app.setLoanAmount(new BigDecimal("50000"));
        app.setLoanTermMonths(10);

        try {
            LoanApplication saved = service.createDraft(2L, app);
            System.out.println("SUCCESSFULLY SAVED ID: " + saved.getId());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
