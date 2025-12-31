package com.mentalhealth.backend.service;

import com.mentalhealth.backend.model.User;
import com.mentalhealth.backend.model.UserRole;
import com.mentalhealth.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User user) {
        System.out.println("Registering USER: " + user.getUsername());
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        System.out.println("Password encoded successfully");
        user.setPassword(encodedPassword);
        user.setRole(UserRole.USER);
        User savedUser = userRepository.save(user);
        System.out.println("USER saved with ID: " + savedUser.getId());
        return savedUser;
    }
}




