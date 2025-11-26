package com.example.RecipeSharing.controller;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.RecipeSharing.model.Users;
import com.example.RecipeSharing.payloads.SignUpRequestDTO;
import com.example.RecipeSharing.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class SignUpController {

    private static final Logger logger = LoggerFactory.getLogger(SignUpController.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequestDTO signUpRequestDTO){
        logger.info("=== USER SIGNUP REQUEST ===");
        logger.info("Email: {}", signUpRequestDTO.getEmail());
        logger.info("Username: {}", signUpRequestDTO.getUsername());

        try {
            // Check if email already exists
            if(Boolean.TRUE.equals(userRepository.existsByEmail(signUpRequestDTO.getEmail()))){
                logger.warn("Signup failed - Email already exists: {}", signUpRequestDTO.getEmail());
                return ResponseEntity.badRequest().body("Email Already Exists");
            }
            
            // Check if username already exists
            if(Boolean.TRUE.equals(userRepository.existsByUsername(signUpRequestDTO.getUsername()))){
                logger.warn("Signup failed - Username already exists: {}", signUpRequestDTO.getUsername());
                return ResponseEntity.badRequest().body("Username Already exists, pls change the username");
            }
            
            // Create new user
            Users users = new Users();
            String hashedPassword = passwordEncoder.encode(signUpRequestDTO.getPassword());
            logger.debug("Password hashed successfully");
            
            users.setEmail(signUpRequestDTO.getEmail());
            users.setUsername(signUpRequestDTO.getUsername());
            users.setPassword(hashedPassword);
            users.setRole(Collections.singleton("USER"));
            
            Users savedUser = userRepository.save(users);
            logger.info("User registered successfully with ID: {}", savedUser.getId());
            return ResponseEntity.ok("User Registered Successfully");
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            throw e;
        }
    }
}
