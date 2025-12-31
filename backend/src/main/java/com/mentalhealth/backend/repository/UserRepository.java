package com.mentalhealth.backend.repository;

import com.mentalhealth.backend.model.User;
import com.mentalhealth.backend.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);

    // Find all users by role enum
    List<User> findByRole(UserRole role);

}
