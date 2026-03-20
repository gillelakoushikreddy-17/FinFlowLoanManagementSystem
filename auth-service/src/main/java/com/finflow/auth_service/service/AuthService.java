package com.finflow.auth_service.service;

import com.finflow.auth_service.entity.User;
import com.finflow.auth_service.repository.UserRepository;
import com.finflow.auth_service.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // default role if none provided
        if(user.getRole() == null || user.getRole().isEmpty()){
            user.setRole("APPLICANT");
        }
        return repository.save(user);
    }

    public String generateToken(String email) {
        Optional<User> userOptional = repository.findByEmail(email);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            return jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
        }
        throw new RuntimeException("User not found");
    }

    public void validateToken(String token) {
        jwtUtil.validateToken(token);
    }
}
