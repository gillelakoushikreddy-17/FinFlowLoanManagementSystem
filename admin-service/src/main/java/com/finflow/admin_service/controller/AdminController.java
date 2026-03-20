package com.finflow.admin_service.controller;

import com.finflow.admin_service.entity.Decision;
import com.finflow.admin_service.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
public class AdminController {

    @Autowired
    private AdminService service;

    @GetMapping("/applications")
    public ResponseEntity<Object[]> getApplicationQueue() {
        return ResponseEntity.ok(service.getAllApplications());
    }

    @PutMapping("/documents/{id}/verify")
    public ResponseEntity<?> verifyDocument(@PathVariable Long id) {
        return ResponseEntity.ok(service.verifyDocument(id));
    }

    @PostMapping("/applications/{id}/decision")
    public ResponseEntity<Decision> makeDecision(
            @RequestHeader("X-User-Id") Long adminId,
            @PathVariable Long id, 
            @RequestParam("decisionType") String decisionType,
            @RequestParam("remarks") String remarks) {
        return ResponseEntity.ok(service.makeDecision(id, adminId, decisionType, remarks));
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        return ResponseEntity.ok(service.getReports());
    }

    @GetMapping("/users")
    public ResponseEntity<Object[]> getUsers() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Object updatedUser) {
        // Forward update request to auth-service
        return ResponseEntity.ok("User updated");
    }
}
