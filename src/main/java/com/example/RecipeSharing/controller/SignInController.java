package com.example.RecipeSharing.controller;

import com.example.RecipeSharing.model.Users;
import com.example.RecipeSharing.payloads.JwtResponse;
import com.example.RecipeSharing.payloads.LoginRequest;
import com.example.RecipeSharing.repository.UserRepository;
import com.example.RecipeSharing.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class SignInController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){
        Users user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()-> new RuntimeException("Error : User not found, Kindly Signup"));

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            return  ResponseEntity.badRequest().body("Invalid Password");
        }

        String token = jwtUtils.generateJwtToken(user);
        return ResponseEntity.ok(new JwtResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
    }
}
