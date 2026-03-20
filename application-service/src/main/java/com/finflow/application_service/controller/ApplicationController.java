package com.finflow.application_service.controller;

import com.finflow.application_service.entity.LoanApplication;
import com.finflow.application_service.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("")
public class ApplicationController {

    @Autowired
    private ApplicationService service;

    @GetMapping("/my")
    public ResponseEntity<List<LoanApplication>> getMyApplications(@RequestHeader("X-User-Id") Long applicantId) {
        return ResponseEntity.ok(service.getMyApplications(applicantId));
    }

    @PostMapping
    public ResponseEntity<LoanApplication> createDraft(@RequestHeader("X-User-Id") Long applicantId, @RequestBody LoanApplication application) {
        return ResponseEntity.ok(service.createDraft(applicantId, application));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanApplication> updateApplication(@PathVariable Long id, @RequestBody LoanApplication application) {
        return ResponseEntity.ok(service.updateApplication(id, application));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<LoanApplication> submitApplication(@PathVariable Long id) {
        return ResponseEntity.ok(service.submitApplication(id));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<?> getStatus(@PathVariable Long id) {
        LoanApplication app = service.getApplication(id);
        return ResponseEntity.ok(Map.of("status", app.getStatus()));
    }
}
