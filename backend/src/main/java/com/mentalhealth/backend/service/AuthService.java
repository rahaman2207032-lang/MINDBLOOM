package com.mentalhealth.backend.service;
import com.mentalhealth.backend.model.User;
import com.mentalhealth.backend.model.Instructor;
import com.mentalhealth.backend.repository.InstructorRepository;
import com.mentalhealth.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final InstructorRepository instructorRepository;
    private final PasswordEncoder passwordEncoder;
    public AuthService(UserRepository userRepository,
                       InstructorRepository instructorRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.instructorRepository = instructorRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public boolean login(String username, String password, String role) {
        System.out.println("Login attempt - Username: " + username + ", Role: " + role);
        if ("USER".equalsIgnoreCase(role)) {
            return userRepository.findByUsername(username)
                    .map(user -> {
                        boolean matches = passwordEncoder.matches(password, user.getPassword());
                        System.out.println("USER found. Password matches: " + matches);
                        return matches;
                    })
                    .orElseGet(() -> {
                        System.out.println("USER not found in database");
                        return false;
                    });
        } else if ("INSTRUCTOR".equalsIgnoreCase(role)) {
            return instructorRepository.findByUsername(username)
                    .map(inst -> {
                        boolean matches = passwordEncoder.matches(password, inst.getPassword());
                        System.out.println("INSTRUCTOR found. Password matches: " + matches);
                        return matches;
                    })
                    .orElseGet(() -> {
                        System.out.println("INSTRUCTOR not found in database");
                        return false;
                    });
        }
        System.out.println("Invalid role provided: " + role);
        return false;
    }
    // New method to get User with ID
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    // New method to get Instructor with ID
    public Instructor getInstructorByUsername(String username) {
        return instructorRepository.findByUsername(username).orElse(null);
    }
}
