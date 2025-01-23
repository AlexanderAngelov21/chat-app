package com.example.chatapp.controller;

import com.example.chatapp.dto.LoginRequest;
import com.example.chatapp.dto.LoginResponse;
import com.example.chatapp.model.User;
import com.example.chatapp.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsernameAndIsActiveTrue(loginRequest.getUsername());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body(new LoginResponse("Invalid username or password", null, null));
        }

        User user = userOptional.get();


        if (!user.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(401).body(new LoginResponse("Invalid username or password", null, null));
        }

        // Successful login
        return ResponseEntity.ok(new LoginResponse("Login successful", user.getId(), user.getUsername()));
    }
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User newUser) {
        Optional<User> existingUser = userRepository.findByUsernameAndIsActiveTrue(newUser.getUsername());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(409).body("Username already exists.");
        }
        if (userRepository.findByEmailAndIsActiveTrue(newUser.getEmail()).isPresent()) {
            return ResponseEntity.status(409).body("Email already exists.");
        }
        newUser.setActive(true);
        userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully.");
    }

}
