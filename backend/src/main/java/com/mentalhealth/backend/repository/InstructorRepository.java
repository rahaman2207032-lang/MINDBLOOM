package com.mentalhealth.backend.repository;

import com.mentalhealth.backend.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor,Long> {
    Optional<Instructor> findByUsername(String username);
}
