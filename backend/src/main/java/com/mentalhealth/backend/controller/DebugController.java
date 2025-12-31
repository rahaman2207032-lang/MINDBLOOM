package com.mentalhealth.backend.controller;

import com.mentalhealth.backend.model.Instructor;
import com.mentalhealth.backend.model.User;
import com.mentalhealth.backend.repository.InstructorRepository;
import com.mentalhealth.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final UserRepository userRepository;
    private final InstructorRepository instructorRepository;
    private final PasswordEncoder passwordEncoder;

    public DebugController(UserRepository userRepository,
                          InstructorRepository instructorRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.instructorRepository = instructorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/test-password")
    public Map<String, Object> testPassword(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role) {

        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("role", role);

        if ("USER".equalsIgnoreCase(role)) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                result.put("userFound", true);
                result.put("storedPassword", user.getPassword());
                result.put("passwordMatches", passwordEncoder.matches(password, user.getPassword()));
                result.put("passwordStartsWith$2a", user.getPassword().startsWith("$2a$"));
            } else {
                result.put("userFound", false);
            }
        } else if ("INSTRUCTOR".equalsIgnoreCase(role)) {
            Instructor instructor = instructorRepository.findByUsername(username).orElse(null);
            if (instructor != null) {
                result.put("instructorFound", true);
                result.put("storedPassword", instructor.getPassword());
                result.put("passwordMatches", passwordEncoder.matches(password, instructor.getPassword()));
                result.put("passwordStartsWith$2a", instructor.getPassword().startsWith("$2a$"));
            } else {
                result.put("instructorFound", false);
            }
        }

        return result;
    }

    @GetMapping("/list-users")
    public Map<String, Object> listUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalUsers", userRepository.count());
        result.put("totalInstructors", instructorRepository.count());

        userRepository.findAll().forEach(user ->
            System.out.println("USER: " + user.getUsername() + " | Password: " + user.getPassword())
        );

        instructorRepository.findAll().forEach(instructor ->
            System.out.println("INSTRUCTOR: " + instructor.getUsername() + " | Password: " + instructor.getPassword())
        );

        return result;
    }

    @DeleteMapping("/clear-all")
    public Map<String, String> clearAll() {
        userRepository.deleteAll();
        instructorRepository.deleteAll();
        return Map.of("message", "All users and instructors deleted");
    }
}

