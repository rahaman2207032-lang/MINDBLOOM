package com.mentalhealth.backend.controller;
import com.mentalhealth.backend.dto.LoginRequest;
import com.mentalhealth.backend.dto.LoginResponse;
import com.mentalhealth.backend.model.User;
import com.mentalhealth.backend.model.Instructor;
import com.mentalhealth.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("=== LOGIN REQUEST RECEIVED ===");
        System.out.println("Raw request object: " + request);
        System.out.println("Username: " + request.getUsername());
        System.out.println("Password received: " + (request.getPassword() != null ? "[" + request.getPassword().length() + " chars]" : "null"));
        System.out.println("Role: " + request.getRole());
        System.out.println("==============================");
        // Validate input
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            System.out.println("❌ Login FAILED - Username is null or empty");
            return ResponseEntity.badRequest().body("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            System.out.println("❌ Login FAILED - Password is null or empty");
            return ResponseEntity.badRequest().body("Password is required");
        }
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            System.out.println("❌ Login FAILED - Role is null or empty");
            return ResponseEntity.badRequest().body("Role is required");
        }
        boolean success = authService.login(
                request.getUsername(),
                request.getPassword(),
                request.getRole()
        );
        if (success) {
            System.out.println("✅ Login SUCCESSFUL for user: " + request.getUsername());
            // Get user/instructor details to return userId
            if ("USER".equalsIgnoreCase(request.getRole())) {
                User user = authService.getUserByUsername(request.getUsername());
                if (user != null) {
                    LoginResponse response = new LoginResponse(
                            user.getId(),
                            user.getUsername(),
                            "USER",
                            "Login successful"
                    );
                    return ResponseEntity.ok(response);
                }
            } else if ("INSTRUCTOR".equalsIgnoreCase(request.getRole())) {
                Instructor instructor = authService.getInstructorByUsername(request.getUsername());
                if (instructor != null) {
                    LoginResponse response = new LoginResponse(
                            instructor.getId(),
                            instructor.getUsername(),
                            "INSTRUCTOR",
                            "Login successful"
                    );
                    return ResponseEntity.ok(response);
                }
            }
            // Fallback if user/instructor not found after successful login
            return ResponseEntity.ok("Login successful");
        } else {
            System.out.println("❌ Login FAILED for user: " + request.getUsername());
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
