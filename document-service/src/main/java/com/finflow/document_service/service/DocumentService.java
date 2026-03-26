package com.finflow.document_service.service;

import com.finflow.document_service.entity.Document;
import com.finflow.document_service.entity.DocumentType;
import com.finflow.document_service.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DocumentService handles all business logic for the document upload and
 * verification workflow in the FinFlow Loan Management System.
 *
 * Key responsibilities:
 *  1. Saving uploaded document metadata to the database
 *  2. Preventing duplicate uploads (same type for same application)
 *  3. Tracking which of the 3 mandatory documents are UPLOADED vs MISSING
 *  4. Allowing admin to mark documents as VERIFIED
 *
 * The 3 mandatory document types are defined in the {@link DocumentType} enum:
 *   AADHAAR, PAN_CARD, INCOME_CERTIFICATE
 */
@Service
public class DocumentService {

    // JPA Repository for CRUD operations on the 'documents' MySQL table
    @Autowired
    private DocumentRepository repository;

    /**
     * Saves a new document record to the database after the file has been
     * physically written to the server's file system (done in the controller).
     *
     * Uses DocumentType enum to ensure only valid types are accepted —
     * this prevents typos like "AADHAR" or "PAN" from entering the database.
     *
     * Throws IllegalStateException if the same document type has already been
     * uploaded for this application (each type is allowed exactly once).
     *
     * @param applicantId   ID of the user who owns this document
     * @param applicationId ID of the loan application this document belongs to
     * @param type          One of AADHAAR, PAN_CARD, INCOME_CERTIFICATE
     * @param fileName      Original file name (e.g. "aadhaar_scan.pdf")
     * @param filePath      Absolute path where the file is stored on disk
     * @return              The saved Document entity with a generated DB id
     */
    public Document uploadDocument(Long applicantId, Long applicationId, DocumentType type, String fileName, String filePath) {
        String typeName = type.name(); // converts enum to string, e.g. "PAN_CARD"

        // Duplicate check: query the DB for an existing document of the same type for this application
        if (repository.findByApplicationIdAndType(applicationId, typeName).isPresent()) {
            throw new IllegalStateException(
                "A '" + typeName + "' document has already been uploaded for application " + applicationId + "."
            );
        }

        // Create new document entity with initial status "UPLOADED" and current timestamp
        Document document = new Document(null, applicantId, applicationId, typeName, fileName, filePath, "UPLOADED", LocalDateTime.now());
        return repository.save(document);
    }

    /**
     * Returns all documents submitted for a given loan application.
     * Used by both applicants (to see their uploads) and admins (to review them).
     *
     * @param applicationId The loan application ID
     * @return List of Document entities (may be empty if none uploaded yet)
     */
    public List<Document> getDocumentsForApplication(Long applicationId) {
        return repository.findByApplicationId(applicationId);
    }

    /**
     * Fetches a single document by its database ID.
     * Throws a RuntimeException (404-style) if not found.
     *
     * @param id The document's primary key
     * @return   The Document entity
     */
    public Document getDocumentById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));
    }

    /**
     * Marks a document as VERIFIED by an admin.
     * The admin should have already viewed the file before calling this.
     *
     * Status transition: UPLOADED → VERIFIED
     *
     * @param id The document's primary key
     * @return   The updated Document entity with status = "VERIFIED"
     */
    public Document verifyDocument(Long id) {
        Document document = getDocumentById(id);
        document.setStatus("VERIFIED"); // update status in memory
        return repository.save(document); // persist the change to MySQL
    }

    /**
     * Checks completeness of the 3 mandatory documents for a loan application.
     *
     * Example return value:
     * {
     *   "AADHAAR":             "UPLOADED",
     *   "PAN_CARD":            "MISSING",
     *   "INCOME_CERTIFICATE":  "MISSING"
     * }
     *
     * This helps the applicant know what they still need to upload,
     * and the admin know whether the application is document-complete.
     *
     * @param applicationId The loan application to check
     * @return Map of document type → "UPLOADED" or "MISSING"
     */
    public Map<String, String> getDocumentCompleteness(Long applicationId) {
        List<Document> uploaded = repository.findByApplicationId(applicationId);
        Map<String, String> statusMap = new HashMap<>();

        // Initialize all 3 types as MISSING
        for (DocumentType type : DocumentType.values()) {
            statusMap.put(type.name(), "MISSING");
        }

        // Override to UPLOADED for each type that has a record in the DB
        for (Document doc : uploaded) {
            statusMap.put(doc.getType(), "UPLOADED");
        }

        return statusMap;
    }

    /**
     * Returns true only when all 3 mandatory document types have been uploaded.
     * Used after each upload to decide the response message shown to the applicant.
     *
     * @param applicationId The loan application to check
     * @return true if AADHAAR, PAN_CARD, and INCOME_CERTIFICATE are all uploaded
     */
    public boolean areAllDocumentsUploaded(Long applicationId) {
        List<Document> uploaded = repository.findByApplicationId(applicationId);
        // Collect just the type names of uploaded documents
        List<String> uploadedTypes = uploaded.stream().map(Document::getType).collect(Collectors.toList());
        // Check that every enum value has a matching uploaded record
        return Arrays.stream(DocumentType.values())
                .allMatch(type -> uploadedTypes.contains(type.name()));
    }
}
