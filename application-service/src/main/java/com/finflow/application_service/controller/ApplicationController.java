package com.finflow.application_service.controller;

import com.finflow.application_service.entity.LoanApplication;
import com.finflow.application_service.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ApplicationController handles all REST endpoints for the application-service microservice.
 *
 * Accessible via the API Gateway at: /gateway/applications/**
 * Visible in Swagger UI under the "Application Service" definition.
 *
 * Loan Application Lifecycle (status flow):
 *   DRAFT → SUBMITTED → (Admin reviews) → APPROVED or REJECTED
 *
 * Typical applicant workflow:
 *   1. POST /          — create a draft application with loan details
 *   2. PUT /{id}       — update the draft (amount, purpose, income)
 *   3. POST /{id}/submit — submit the application for admin review
 *   4. GET /{id}/status  — poll for the decision (APPROVED / REJECTED)
 */
@RestController
@RequestMapping("") // Base path — routing prefix /gateway/applications is added by the API Gateway
public class ApplicationController {

    @Autowired
    private ApplicationService service;

    /**
     * Returns all loan applications in the system.
     * Primarily used by the admin-service (which calls this via RestTemplate)
     * to populate the admin review queue.
     *
     * No authentication header required — all applications are visible.
     */
    @GetMapping("/all")
    public ResponseEntity<List<LoanApplication>> getAllApplications() {
        return ResponseEntity.ok(service.getAllApplications());
    }

    /**
     * Returns all loan applications belonging to a specific applicant.
     *
     * @param applicantId The applicant's user ID (passed as "Applicant-Id" header)
     */
    @GetMapping("/my")
    public ResponseEntity<List<LoanApplication>> getMyApplications(@RequestHeader("Applicant-Id") Long applicantId) {
        return ResponseEntity.ok(service.getMyApplications(applicantId));
    }

    /**
     * Creates a new loan application in DRAFT status.
     *
     * The request body should contain:
     *   - loanAmount   : How much money the applicant needs
     *   - purpose      : Why they need the loan (house, car, education, etc.)
     *   - annualIncome : Applicant's income (used for eligibility)
     *
     * The application starts in DRAFT state — it is NOT yet visible to admin.
     * The applicant can still update it before submitting.
     *
     * @param applicantId The user creating the application (from "Applicant-Id" header)
     * @param application The loan application details in the request body
     */
    @PostMapping
    public ResponseEntity<LoanApplication> createDraft(@RequestHeader("Applicant-Id") Long applicantId, @RequestBody LoanApplication application) {
        return ResponseEntity.ok(service.createDraft(applicantId, application));
    }

    /**
     * Updates the fields of a DRAFT loan application.
     * Only works while the application is still in DRAFT state.
     * Once submitted, the application cannot be modified.
     *
     * @param id          The loan application ID (from URL path)
     * @param application Updated loan details (from request body)
     */
    @PutMapping("/{id}")
    public ResponseEntity<LoanApplication> updateApplication(@PathVariable Long id, @RequestBody LoanApplication application) {
        return ResponseEntity.ok(service.updateApplication(id, application));
    }

    /**
     * Submits a DRAFT application for admin review.
     *
     * Status transition: DRAFT → SUBMITTED
     *
     * After this, the application appears in the admin review queue
     * (GET /gateway/admin/applications) and can no longer be modified.
     *
     * @param id The ID of the draft application to submit
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<LoanApplication> submitApplication(@PathVariable Long id) {
        return ResponseEntity.ok(service.submitApplication(id));
    }

    /**
     * Directly updates the status of a loan application.
     * Used internally by the RabbitMQ listener in this service when it receives
     * a decision event from the admin-service.
     *
     * The admin publishes to RabbitMQ → this service receives → calls this logic
     * to update the status to APPROVED or REJECTED.
     *
     * @param id        The loan application ID
     * @param newStatus The new status string (e.g. "APPROVED" or "REJECTED")
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<LoanApplication> updateStatus(@PathVariable Long id, @RequestParam String newStatus) {
        return ResponseEntity.ok(service.updateStatus(id, newStatus));
    }

    /**
     * Returns the current status of a loan application.
     *
     * Example response:
     * { "status": "SUBMITTED" }
     *
     * Possible values: DRAFT, SUBMITTED, APPROVED, REJECTED
     *
     * Applicants can poll this endpoint after submitting to know the admin's decision.
     *
     * @param id The loan application ID
     */
    @GetMapping("/{id}/status")
    public ResponseEntity<?> getStatus(@PathVariable Long id) {
        LoanApplication app = service.getApplication(id); // fetch from DB
        return ResponseEntity.ok(Map.of("status", app.getStatus())); // return just the status field
    }
}
