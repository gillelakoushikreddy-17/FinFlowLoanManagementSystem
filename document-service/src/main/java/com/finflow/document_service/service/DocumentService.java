package com.finflow.document_service.service;

import com.finflow.document_service.entity.Document;
import com.finflow.document_service.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository repository;

    public Document uploadDocument(Long applicantId, Long applicationId, String type, String fileName, String filePath) {
        Document document = new Document(null, applicantId, applicationId, type, fileName, filePath, "UPLOADED", LocalDateTime.now());
        return repository.save(document);
    }

    public List<Document> getDocumentsForApplication(Long applicationId) {
        return repository.findByApplicationId(applicationId);
    }
}
