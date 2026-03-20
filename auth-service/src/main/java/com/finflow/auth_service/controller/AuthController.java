package com.finflow.auth_service.controller;

import com.finflow.auth_service.dto.AuthRequest;
import com.finflow.auth_service.dto.AuthResponse;
import com.finflow.auth_service.entity.User;
import com.finflow.auth_service.repository.UserRepository;
import com.finflow.auth_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
public class AuthController {
    
    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> addNewUser(@RequestBody User user) {
        try {
            if(userRepository.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
            User savedUser = service.saveUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> getToken(@RequestBody AuthRequest authRequest) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );
        if (authenticate.isAuthenticated()) {
            String token = service.generateToken(authRequest.getEmail());
            User user = userRepository.findByEmail(authRequest.getEmail()).orElseThrow();
            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getRole()));
        } else {
            return ResponseEntity.status(401).body("Invalid access");
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestParam("token") String token) {
        service.validateToken(token);
        return ResponseEntity.ok("Token is valid");
    }
}
