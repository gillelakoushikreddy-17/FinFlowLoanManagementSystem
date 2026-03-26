package com.finflow.admin_service.controller;

import com.finflow.admin_service.entity.Decision;
import com.finflow.admin_service.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AdminController exposes REST endpoints for the FinFlow admin dashboard.
 *
 * All endpoints are accessible via the API Gateway under /gateway/admin/**
 * and are visible in the Swagger UI under the "Admin Service" definition.
 *
 * Typical admin workflow:
 *   1. GET /applications           — view the loan review queue
 *   2. GET /applications/{id}/documents  — check uploaded documents
 *   3. PUT /documents/{id}/verify  — verify each document
 *   4. POST /applications/{id}/decision  — approve or reject the loan
 */
@Tag(name = "Admin Service", description = "Review applications, verify documents, and make loan decisions")
@RestController
@RequestMapping("") // Base path — routing prefix /gateway/admin is added by the API Gateway
public class AdminController {

    @Autowired
    private AdminService service;

    /**
     * Returns all loan applications submitted by applicants.
     * Proxies to application-service via RestTemplate.
     * Admin uses this to see which applications need review.
     */
    @Operation(summary = "Get all loan applications in the review queue")
    @GetMapping("/applications")
    public ResponseEntity<Object[]> getApplicationQueue() {
        return ResponseEntity.ok(service.getAllApplications());
    }

    /**
     * Returns all documents uploaded for a specific loan application.
     * Shows type (AADHAAR/PAN_CARD/INCOME_CERTIFICATE) and status (UPLOADED/VERIFIED).
     *
     * Admin should call this BEFORE verifying documents to get document IDs.
     *
     * @param id The loan application ID
     */
    @Operation(summary = "List all documents for a loan application",
            description = "Shows uploaded documents (AADHAAR, PAN_CARD, INCOME_CERTIFICATE) with their status (UPLOADED / VERIFIED)")
    @GetMapping("/applications/{id}/documents")
    public ResponseEntity<Object[]> getDocumentsForApplication(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDocumentsForApplication(id));
    }

    /**
     * Marks a document as VERIFIED by calling document-service.
     * Admin must visually inspect the file via /gateway/documents/{id}/download
     * BEFORE verifying it.
     *
     * Status transition: UPLOADED → VERIFIED
     *
     * @param id The document ID (get it from GET /applications/{id}/documents)
     */
    @Operation(summary = "Verify a document",
            description = "Changes document status from UPLOADED to VERIFIED. Use GET /applications/{id}/documents first to find the document ID.")
    @PutMapping("/documents/{id}/verify")
    public ResponseEntity<?> verifyDocument(@PathVariable Long id) {
        return ResponseEntity.ok(service.verifyDocument(id));
    }

    /**
     * Records the admin's final decision on a loan application.
     * Saves the decision to MySQL AND publishes a RabbitMQ event so
     * the application-service updates the loan status asynchronously.
     *
     * @param adminId      The admin's user ID (passed as "Admin-Id" header)
     * @param id           The loan application ID being decided on
     * @param decisionType "APPROVED" or "REJECTED"
     * @param remarks      Admin's reason / comments for the decision
     */
    @Operation(summary = "Approve or reject a loan application")
    @PostMapping("/applications/{id}/decision")
    public ResponseEntity<Decision> makeDecision(
            @RequestHeader("Admin-Id") Long adminId,       // Who is making the decision
            @PathVariable Long id,                          // Which application
            @RequestParam("decisionType") String decisionType,  // APPROVED or REJECTED
            @RequestParam("remarks") String remarks) {          // Reason for decision
        return ResponseEntity.ok(service.makeDecision(id, adminId, decisionType, remarks));
    }

    /**
     * Returns all reports stored in the admin-service database.
     * Reports can include statistics on loan approval rates, volumes, etc.
     */
    @Operation(summary = "Get all reports")
    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        return ResponseEntity.ok(service.getReports());
    }

    /**
     * Placeholder: will return all registered users from auth-service.
     * Currently returns empty until auth-service exposes a /users endpoint.
     */
    @Operation(summary = "Get all registered users")
    @GetMapping("/users")
    public ResponseEntity<Object[]> getUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    /**
     * Placeholder for updating a user's details via auth-service.
     * Currently returns a stub response — can be implemented by forwarding
     * the request to auth-service using RestTemplate.
     *
     * @param id          The user ID to update
     * @param updatedUser The updated user data (passed as request body)
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Object updatedUser) {
        // TODO: Forward update request to auth-service via RestTemplate
        return ResponseEntity.ok("User updated");
    }
}
