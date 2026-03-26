package com.finflow.document_service.repository;

import com.finflow.document_service.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByApplicationId(Long applicationId);
    List<Document> findByApplicantId(Long applicantId);
    Optional<Document> findByApplicationIdAndType(Long applicationId, String type);
}
