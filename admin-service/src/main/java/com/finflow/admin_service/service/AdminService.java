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

/**
 * AdminService is the brain of the admin-service microservice.
 *
 * It communicates with other microservices (application-service, document-service)
 * via RestTemplate (HTTP calls using Eureka service names) and uses RabbitMQ to
 * publish loan decision events asynchronously.
 *
 * All external service calls are protected by a Resilience4j Circuit Breaker —
 * if the target service is down, the fallback method returns a safe empty response
 * instead of crashing the admin service.
 */
@Service
public class AdminService {

    // Saves loan decisions (APPROVED / REJECTED) made by admins
    @Autowired
    private DecisionRepository decisionRepository;

    // Saves auto-generated reports
    @Autowired
    private ReportRepository reportRepository;

    // Used to make HTTP calls to other microservices by their Eureka service name
    // e.g. "http://document-service/..." — Eureka resolves this to the actual container IP
    @Autowired
    private RestTemplate restTemplate;

    // Used to publish events to RabbitMQ when an admin makes a loan decision
    // The application-service listens to this queue and updates the loan status
    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    /**
     * Fetches all loan applications from the application-service.
     * These appear in the admin review queue.
     *
     * @CircuitBreaker — if application-service is unreachable, fallbackGetAllApplications()
     * is called instead, returning an empty array without crashing.
     */
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "applicationService", fallbackMethod = "fallbackGetAllApplications")
    public Object[] getAllApplications() {
        // Eureka resolves "application-service" to the actual running service instance
        return restTemplate.getForObject("http://application-service/all", Object[].class);
    }

    /**
     * Fallback for getAllApplications() — called automatically if the circuit breaker opens.
     * Returns an empty array so the admin page loads without errors.
     */
    public Object[] fallbackGetAllApplications(Throwable t) {
        System.out.println("Circuit Breaker activated for application-service: " + t.getMessage());
        return new Object[]{};
    }

    /**
     * Fetches all documents uploaded for a specific loan application.
     * Returns document metadata: id, type (AADHAAR/PAN_CARD/INCOME_CERTIFICATE),
     * fileName, filePath, status (UPLOADED / VERIFIED), uploadedAt.
     *
     * Admin uses this to check which documents have been submitted before verifying them.
     */
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "documentService", fallbackMethod = "fallbackGetDocuments")
    public Object[] getDocumentsForApplication(Long applicationId) {
        return restTemplate.getForObject("http://document-service/application/" + applicationId, Object[].class);
    }

    /** Fallback if document-service is unreachable. Returns empty list. */
    public Object[] fallbackGetDocuments(Long applicationId, Throwable t) {
        System.out.println("Circuit Breaker activated for document-service: " + t.getMessage());
        return new Object[]{};
    }

    /**
     * Calls document-service to mark a specific document as VERIFIED.
     * The document status changes: UPLOADED → VERIFIED in the database.
     *
     * Admin should call GET /applications/{id}/documents first to find
     * the document ID, then call this method to verify it.
     */
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "documentService", fallbackMethod = "fallbackVerifyDocument")
    public Object verifyDocument(Long documentId) {
        // Calls PUT /{id}/verify on the document-service
        String url = "http://document-service/" + documentId + "/verify";
        restTemplate.put(url, null);
        return "Document " + documentId + " verified successfully";
    }

    /** Fallback if document-service is unreachable during verification. */
    public Object fallbackVerifyDocument(Long documentId, Throwable t) {
        System.out.println("Circuit Breaker activated for document-service verify: " + t.getMessage());
        return "Document-service unavailable. Could not verify document " + documentId;
    }

    /**
     * Records the admin's final loan decision (APPROVED or REJECTED) and
     * publishes an async event to RabbitMQ so the application-service can
     * update the loan status without the admin waiting for it.
     *
     * Flow:
     *   1. Create a Decision entity and save it to the DB (audit trail)
     *   2. Build a StatusUpdateMessage with applicationId + status
     *   3. Send the message to RabbitMQ exchange → routing key → queue
     *   4. application-service listener picks it up and updates the loan status
     *
     * @param applicationId  The loan application being decided on
     * @param adminId        The ID of the admin making the decision
     * @param decisionType   "APPROVED" or "REJECTED"
     * @param remarks        Admin's comments / reason for decision
     */
    public Decision makeDecision(Long applicationId, Long adminId, String decisionType, String remarks) {
        // Step 1: Save admin's decision to the database for audit purposes
        Decision decision = new Decision(null, applicationId, adminId, decisionType, remarks, LocalDateTime.now());

        // Step 2: Build the message to send to RabbitMQ
        com.finflow.admin_service.dto.StatusUpdateMessage msg = new com.finflow.admin_service.dto.StatusUpdateMessage();
        msg.setApplicationId(applicationId);
        msg.setStatus(decisionType); // "APPROVED" or "REJECTED"

        // Step 3: Publish the event — application-service will receive and update its DB
        rabbitTemplate.convertAndSend(
            com.finflow.admin_service.config.RabbitMQConfig.EXCHANGE_NAME,
            com.finflow.admin_service.config.RabbitMQConfig.ROUTING_KEY,
            msg
        );

        // Step 4: Persist and return the saved decision
        return decisionRepository.save(decision);
    }

    /**
     * Returns all auto-generated reports stored in the admin-service database.
     * Reports can be generated based on approval rates, volumes, etc.
     */
    public List<Report> getReports() {
        return reportRepository.findAll();
    }

    /**
     * Placeholder for fetching all registered users from auth-service.
     * Currently returns empty — can be implemented by calling:
     * restTemplate.getForObject("http://auth-service/users", Object[].class)
     */
    public Object[] getAllUsers() {
        // TODO: Call auth-service to get all users
        return new Object[]{};
    }
}
