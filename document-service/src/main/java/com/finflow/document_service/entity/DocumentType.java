package com.finflow.document_service.entity;

/**
 * Enumeration of the 3 mandatory document types required for every loan application.
 * Using an enum ensures the Swagger UI shows a dropdown instead of a free-text field,
 * completely preventing spelling mistakes.
 */
public enum DocumentType {
    AADHAAR,
    PAN_CARD,
    INCOME_CERTIFICATE
}
