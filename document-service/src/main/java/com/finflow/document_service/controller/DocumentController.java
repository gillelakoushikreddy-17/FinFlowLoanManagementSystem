package com.finflow.document_service.controller;

import com.finflow.document_service.entity.Document;
import com.finflow.document_service.entity.DocumentType;
import com.finflow.document_service.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

/**
 * DocumentController handles all HTTP requests for the document-service microservice.
 *
 * Accessible via the API Gateway at: /gateway/documents/**
 * Visible in Swagger UI under the "Document Service" definition.
 *
 * Document upload workflow:
 *  1. Applicant selects document type (AADHAAR / PAN_CARD / INCOME_CERTIFICATE) from dropdown
 *  2. Applicant uploads the file (image or PDF)
 *  3. File is saved to the upload directory on the server (Docker volume in production)
 *  4. Metadata (type, path, status=UPLOADED) is saved to MySQL
 *  5. Response tells applicant how many more documents are still needed
 *
 * Admin review workflow:
 *  1. Admin lists documents via GET /application/{id}
 *  2. Admin views file via GET /{id}/download (opens in browser)
 *  3. Admin verifies document via PUT /{id}/verify (status: UPLOADED → VERIFIED)
 */
@Tag(name = "Document Service", description = "Upload and view loan application documents")
@RestController
@RequestMapping("") // Base path — routing prefix /gateway/documents is added by the API Gateway
public class DocumentController {

    @Autowired
    private DocumentService service;

    /**
     * Upload directory configuration:
     * - In Docker: reads the UPLOAD_DIR environment variable (set to /app/uploads/ in docker-compose.yml)
     *   Files are stored in a named Docker volume (finflow-uploads) for persistence across restarts.
     * - Locally (no Docker): defaults to the user's home directory + /finflow-uploads/
     *   e.g. C:/Users/HP/finflow-uploads/ on Windows
     *
     * Using NIO (java.nio.file) instead of java.io.File.transferTo() to avoid
     * Tomcat working-directory path resolution issues on Windows.
     */
    private final String UPLOAD_DIR = System.getenv("UPLOAD_DIR") != null
            ? System.getenv("UPLOAD_DIR")       // Docker: /app/uploads/
            : System.getProperty("user.home") + "/finflow-uploads/"; // Local fallback

    /**
     * Handles multipart file upload for one of the 3 mandatory document types.
     *
     * 'type' parameter uses the {@link DocumentType} enum — Swagger renders it as a dropdown,
     * so the user selects from the list instead of typing, preventing spelling mistakes.
     *
     * Process:
     *  1. Create upload directory if it doesn't exist
     *  2. Save file with a timestamp prefix (avoids name collisions)
     *  3. Save document metadata to DB via DocumentService
     *  4. Check if all 3 mandatory documents are now uploaded
     *  5. Return document details + a helpful status message
     *
     * @param applicantId   The applicant's user ID (from "Applicant-Id" header)
     * @param applicationId The loan application this document belongs to
     * @param type          AADHAAR, PAN_CARD, or INCOME_CERTIFICATE
     * @param file          The actual file bytes (image or PDF)
     * @return 200 OK with document details and completeness message,
     *         400 BAD REQUEST if duplicate type uploaded,
     *         500 INTERNAL ERROR if file system write fails
     */
    @Operation(summary = "Upload a required document",
            description = "Select document type from dropdown (AADHAAR, PAN_CARD, INCOME_CERTIFICATE) and upload file. All 3 are mandatory.")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadDocument(
            @RequestHeader("Applicant-Id") Long applicantId,       // Who is uploading
            @RequestParam("applicationId") Long applicationId,      // Which loan application
            @RequestParam("type") DocumentType type,                // Type of document (enum dropdown)
            @RequestParam("file") MultipartFile file) {             // The actual file
        try {
            // Step 1: Resolve UPLOAD_DIR to an absolute path and create it if missing
            // toAbsolutePath() ensures Docker won't prepend Tomcat's working directory
            Path uploadDirPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
            Files.createDirectories(uploadDirPath); // creates the folder if it doesn't exist

            // Step 2: Prefix filename with current timestamp to prevent collisions
            // e.g. "1234567890123_aadhaar_scan.pdf"
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path destination = uploadDirPath.resolve(fileName);

            // Step 3: Copy file bytes to disk using NIO (safe on all platforms)
            // REPLACE_EXISTING: overwrites if same name exists (rare but safe)
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            String filePath = destination.toString(); // absolute path stored in DB

            // Step 4: Save document metadata to MySQL via service layer
            // Service will throw IllegalStateException if same type already uploaded for this application
            Document document = service.uploadDocument(applicantId, applicationId, type, file.getOriginalFilename(), filePath);

            // Step 5: Check if all 3 mandatory documents are now complete
            boolean allComplete = service.areAllDocumentsUploaded(applicationId);
            if (allComplete) {
                // All 3 uploaded — application is document-complete
                return ResponseEntity.ok(Map.of(
                        "document", document,
                        "message", "All 3 required documents uploaded! Application is document-complete."
                ));
            }

            // Not yet complete — tell the applicant how many are still missing
            Map<String, String> completeness = service.getDocumentCompleteness(applicationId);
            long missing = completeness.values().stream().filter(s -> s.equals("MISSING")).count();
            return ResponseEntity.ok(Map.of(
                    "document", document,
                    "message", missing + " more document(s) still required. Status: " + completeness
            ));

        } catch (IllegalStateException e) {
            // Duplicate document type — return 400 with a clear error message
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            // File system error — log and return 500
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }

    /**
     * Returns a map showing which of the 3 mandatory documents are uploaded vs missing.
     *
     * Example response:
     * {
     *   "AADHAAR": "UPLOADED",
     *   "PAN_CARD": "MISSING",
     *   "INCOME_CERTIFICATE": "MISSING"
     * }
     *
     * @param applicationId The loan application to check
     */
    @Operation(summary = "Check document completeness",
            description = "Shows UPLOADED or MISSING status for each of the 3 required document types")
    @GetMapping("/application/{applicationId}/completeness")
    public ResponseEntity<Map<String, String>> getDocumentCompleteness(@PathVariable Long applicationId) {
        return ResponseEntity.ok(service.getDocumentCompleteness(applicationId));
    }

    /**
     * Returns metadata for all documents uploaded for a specific loan application.
     * Used by the admin to see which documents exist before verifying them.
     *
     * @param applicationId The loan application ID
     * @return List of Document entities with id, type, fileName, status, uploadedAt
     */
    @Operation(summary = "List documents for an application")
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<List<Document>> getDocumentsForApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(service.getDocumentsForApplication(applicationId));
    }

    /**
     * Changes a document's status from UPLOADED to VERIFIED.
     * Called by the admin after visually inspecting the file via GET /{id}/download.
     *
     * This endpoint is also proxied by admin-service at PUT /gateway/admin/documents/{id}/verify
     * for convenience in the Admin Swagger tab.
     *
     * @param id The document's database ID
     */
    @Operation(summary = "Verify a document (Admin only)",
            description = "Changes the document status from UPLOADED to VERIFIED. Call this after reviewing the file.")
    @PutMapping("/{id}/verify")
    public ResponseEntity<Document> verifyDocument(@PathVariable Long id) {
        return ResponseEntity.ok(service.verifyDocument(id));
    }

    /**
     * Streams the actual document file back to the browser/client.
     *
     * Content-Type is set dynamically based on extension:
     *   .pdf  → application/pdf      (PDFs open inline in the browser)
     *   .jpg/.jpeg → image/jpeg      (Images display inline)
     *   .png  → image/png
     *   other → application/octet-stream (triggers download)
     *
     * Content-Disposition is set to "inline" so the browser opens the file
     * instead of downloading it.
     *
     * Returns 404 if the file path stored in the DB no longer exists on disk
     * (e.g. files uploaded before the Docker volume fix).
     *
     * @param id The document's database ID
     */
    @Operation(summary = "Open / Download a document",
            description = "Streams the actual file for admin to view (PDFs open inline in browser)")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        // Step 1: Get document metadata from DB to find the file path
        Document document = service.getDocumentById(id);
        File file = new File(document.getFilePath()); // reconstruct file from stored path

        // Step 2: Check if file actually exists on disk
        if (!file.exists()) {
            return ResponseEntity.notFound().build(); // 404 — file missing from disk
        }

        // Step 3: Wrap file as a Spring Resource for streaming
        Resource resource = new FileSystemResource(file);

        // Step 4: Determine content type based on file extension
        String contentType = "application/octet-stream"; // default — triggers download
        String fileName = document.getFileName().toLowerCase();
        if (fileName.endsWith(".pdf")) {
            contentType = "application/pdf";           // opens in browser PDF viewer
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            contentType = "image/jpeg";                // displays as image in browser
        } else if (fileName.endsWith(".png")) {
            contentType = "image/png";
        }

        // Step 5: Stream the file with correct headers
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }
}
