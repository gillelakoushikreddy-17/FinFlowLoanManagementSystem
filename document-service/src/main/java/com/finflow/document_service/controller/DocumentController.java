package com.finflow.document_service.controller;

import com.finflow.document_service.entity.Document;
import com.finflow.document_service.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("")
public class DocumentController {

    @Autowired
    private DocumentService service;

    // Simulate file storage directory
    private final String UPLOAD_DIR = "C:/temp/uploads/";

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(
            @RequestHeader("X-User-Id") Long applicantId,
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file) {
        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();
            
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = UPLOAD_DIR + fileName;
            
            file.transferTo(new File(filePath));
            
            Document document = service.uploadDocument(applicantId, applicationId, type, file.getOriginalFilename(), filePath);
            return ResponseEntity.ok(document);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
}
