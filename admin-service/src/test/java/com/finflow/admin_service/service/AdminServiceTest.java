package com.finflow.admin_service.service;

import com.finflow.admin_service.entity.Decision;
import com.finflow.admin_service.entity.Report;
import com.finflow.admin_service.repository.DecisionRepository;
import com.finflow.admin_service.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AdminService adminService;

    @Test
    void testGetAllApplications() {
        Object[] mockApps = new Object[]{new Object(), new Object()};
        when(restTemplate.getForObject("http://application-service/all", Object[].class)).thenReturn(mockApps);

        Object[] result = adminService.getAllApplications();

        assertEquals(2, result.length);
        verify(restTemplate, times(1)).getForObject("http://application-service/all", Object[].class);
    }

    @Test
    void testGetDocumentsForApplication() {
        Object[] mockDocs = new Object[]{new Object(), new Object()};
        when(restTemplate.getForObject("http://document-service/application/5", Object[].class)).thenReturn(mockDocs);

        Object[] result = adminService.getDocumentsForApplication(5L);

        assertEquals(2, result.length);
        verify(restTemplate, times(1)).getForObject("http://document-service/application/5", Object[].class);
    }

    @Test
    void testVerifyDocument() {
        // verifyDocument calls PUT on document-service, then returns a confirmation string
        doNothing().when(restTemplate).put("http://document-service/15/verify", null);

        Object result = adminService.verifyDocument(15L);

        assertEquals("Document 15 verified successfully", result);
        verify(restTemplate, times(1)).put("http://document-service/15/verify", null);
    }

    @Test
    void testMakeDecision() {
        Decision savedDecision = new Decision(1L, 10L, 2L, "APPROVED", "Looks good", LocalDateTime.now());
        when(decisionRepository.save(any(Decision.class))).thenReturn(savedDecision);

        Decision result = adminService.makeDecision(10L, 2L, "APPROVED", "Looks good");

        assertNotNull(result);
        assertEquals("APPROVED", result.getDecisionType());
        verify(decisionRepository, times(1)).save(any(Decision.class));
    }

    @Test
    void testGetReports() {
        Report report = new Report();
        report.setId(1L);
        when(reportRepository.findAll()).thenReturn(List.of(report));

        List<Report> result = adminService.getReports();

        assertEquals(1, result.size());
        verify(reportRepository, times(1)).findAll();
    }
}
